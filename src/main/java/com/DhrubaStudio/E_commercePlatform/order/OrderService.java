package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.cart.Cart;
import com.DhrubaStudio.E_commercePlatform.cart.CartItem;
import com.DhrubaStudio.E_commercePlatform.cart.CartService;
import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;
import com.DhrubaStudio.E_commercePlatform.notifications.EmailService;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderItemResponseDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderResponseDTO;
import com.DhrubaStudio.E_commercePlatform.user.User;
import com.DhrubaStudio.E_commercePlatform.user.UserRepository;
import com.DhrubaStudio.E_commercePlatform.order.dto.PaymentVerificationDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final EmailService emailService;

    private String razorpayKeyId;
    private String razorpayKeySecret;

    @Autowired
    public OrderService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        CartService cartService,
                        EmailService emailService,
                        @Value("${razorpay.key-id}") String razorpayKeyId,
                        @Value("${razorpay.key-secret}") String razorpayKeySecret
                        ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.emailService = emailService;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    @Transactional
    public Order processCheckout(String email) {
        log.info("Starting checkout process for user: {}", email);

        User buyer = userRepository.findByEmail(email).orElse(null);
        if(buyer == null){
            log.error("Checkout failed. User {} not found in database.", email);
            throw new IllegalArgumentException("User: " + email  + " not found.");
        }

        if(!buyer.getCustomerProfile().isProfileComplete()) {
            log.warn("Checkout blocked for user {}. Profile is incomplete.", email);
            throw new IllegalStateException("User: " + email + " profile data is incomplete.");
        }

        Cart cart = cartService.getCart(email);
        if (cart.getItems().isEmpty()) {
            log.warn("Checkout blocked for user {}. Cart is empty.", email);
            throw new IllegalArgumentException("Cannot process checkout. Your cart is empty.");
        }

        Order order = new Order();
        order.setUser(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        for(CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if(product == null){
                log.error("Checkout failed. Product ID {} not found.", item.getProductId());
                throw new IllegalArgumentException("Product ID: " + item.getProductId() + " not found.");
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                log.warn("Checkout failed. Insufficient stock for product ID {}. Requested: {}, Available: {}",
                        product.getId(), item.getQuantity(), product.getStockQuantity());
                throw new IllegalArgumentException("Insufficient stock for Product: " + product.getName() +
                        ". Available: " + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.addOrderItem(orderItem);

            BigDecimal subTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalOrderPrice = totalOrderPrice.add(subTotal);
        }

        try{
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId,razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", totalOrderPrice.multiply(new BigDecimal("100")).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            order.setRazorpayOrderId(razorpayOrder.get("id"));
            log.info("Razorpay order created successfully: {}", order.getRazorpayOrderId());

        } catch(Exception e) {
            log.error("Failed to create Razorpay Order", e);
            throw new RuntimeException("Could not initiate payment gateway.");
        }

        order.setTotalAmount(totalOrderPrice);
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(email);

        log.info("Checkout successful! Order ID {} created with status PENDING.", savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public void verifyRazorpayPayment(PaymentVerificationDTO request) {
        log.info("Verifying Razorpay payment for Order ID: {}", request.getRazorpayOrderId());

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (!isValid) {
                log.error("Payment verification failed! Invalid signature for Razorpay Order: {}", request.getRazorpayOrderId());
                throw new IllegalStateException("Payment verification failed. Invalid signature.");
            }

            Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found for Razorpay ID: " + request.getRazorpayOrderId()));

            if (order.getStatus() != Order.OrderStatus.PENDING) {
                log.warn("Payment already processed for local Order ID: {}", order.getId());
                return;
            }

            order.setStatus(Order.OrderStatus.PROCESSING);
            orderRepository.save(order);

            log.info("Payment confirmed successfully for local Order ID: {}. Sending receipt email.", order.getId());

            String userEmail = order.getUser().getEmail();
            String shippingAddress = order.getUser().getCustomerProfile().getShippingAddress();
            emailService.sendOrderConfirmationMail(userEmail, order.getId(), order.getTotalAmount(), shippingAddress);

        } catch (Exception e) {
            log.error("Exception occurred during payment verification", e);
            throw new RuntimeException("Payment verification encountered an error.");
        }
    }

    public List<OrderResponseDTO> getOrderHistory(String email) {
        log.debug("Fetching order history for user: {}", email);
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
            log.error("Failed to fetch order history. User {} not found.", email);
            throw new IllegalArgumentException("User: " + email + " not found.");
        }

        List<OrderResponseDTO> responseList = new ArrayList<>();
        List<Order> orders = orderRepository.findByUserId(user.getId());

        for (Order order : orders) {
            List<OrderItemResponseDTO> items = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
                itemDto.setProductId(orderItem.getProduct().getId());
                itemDto.setProductName(orderItem.getProduct().getName());
                itemDto.setQuantity(orderItem.getQuantity());
                itemDto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
                itemDto.setSubTotal(orderItem.getPriceAtPurchase().multiply(new BigDecimal(orderItem.getQuantity())));
                items.add(itemDto);
            }
            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(
                    order.getId(), order.getOrderDate(), order.getTotalAmount(),
                    order.getStatus().name(), items);
            responseList.add(orderResponseDTO);
        }

        log.debug("Returned {} historical orders for user: {}", responseList.size(), email);
        return responseList;
    }
}