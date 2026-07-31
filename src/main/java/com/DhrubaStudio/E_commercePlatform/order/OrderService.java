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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public OrderService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        CartService cartService,
                        EmailService emailService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.emailService = emailService;
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

        order.setTotalAmount(totalOrderPrice);
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(email);

        log.info("Checkout successful! Order ID {} created with status PENDING.", savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public void confirmOrderPayment(Long orderId) {
        log.info("Attempting to confirm payment for Order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Payment confirmation failed. Order ID {} not found.", orderId);
                    return new IllegalArgumentException("Order ID: " + orderId + " not found.");
                });

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            log.warn("Payment confirmation rejected. Order ID {} is in state: {}", orderId, order.getStatus());
            throw new IllegalStateException("Payment failed: Order is already processed or cancelled.");
        }

        order.setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(order);

        log.info("Payment confirmed successfully for Order ID: {}. Triggering confirmation email.", orderId);

        String userEmail = order.getUser().getEmail();
        String shippingAddress = order.getUser().getCustomerProfile().getShippingAddress();
        emailService.sendOrderConfirmationMail(userEmail, order.getId(), order.getTotalAmount(), shippingAddress);
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