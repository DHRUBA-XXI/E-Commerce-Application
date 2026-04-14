package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderItemRequestDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderRequestDTO;
import com.DhrubaStudio.E_commercePlatform.user.User;
import com.DhrubaStudio.E_commercePlatform.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order processCheckout(OrderRequestDTO request) {

        // 1. Verify the User exists
        User buyer = userRepository.findById(request.getUserId()).orElse(null);
        if(buyer == null){
            throw new IllegalArgumentException("User ID: " + request.getUserId() + " not found.");
        }

        Order order = new Order();
        order.setUser(buyer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PROCESSING);
        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        for(OrderItemRequestDTO item : request.getItems()) {
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
        return orderRepository.save(order);
    }
}