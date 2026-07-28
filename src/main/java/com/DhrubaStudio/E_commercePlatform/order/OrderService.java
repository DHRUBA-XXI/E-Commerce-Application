package com.DhrubaStudio.E_commercePlatform.order;
import com.DhrubaStudio.E_commercePlatform.cart.Cart;
import com.DhrubaStudio.E_commercePlatform.cart.CartItem;
import com.DhrubaStudio.E_commercePlatform.cart.CartService;
import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderItemResponseDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderResponseDTO;
import com.DhrubaStudio.E_commercePlatform.user.User;
import com.DhrubaStudio.E_commercePlatform.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Autowired
    public OrderService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        CartService cartService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order processCheckout(String email) {

        User buyer = userRepository.findByEmail(email).orElse(null);
        if(buyer == null){
            throw new IllegalArgumentException("User: " + email  + " not found.");
        }
        if(!buyer.getCustomerProfile().isProfileComplete()) {
            throw new IllegalStateException("User: " + email + " profile data is incomplete.");
        }

        Cart cart = cartService.getCart(email);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot process checkout. Your cart is empty.");
        }

        Order order = new Order();
        order.setUser(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PROCESSING);

        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        for(CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if(product == null){
                throw new IllegalArgumentException("Product ID: " + item.getProductId() + " not found.");
            }

            if (product.getStockQuantity() < item.getQuantity()) {
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

        return savedOrder;
    }

    public List<OrderResponseDTO> getOrderHistory(String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null){
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
        return responseList;
    }
}