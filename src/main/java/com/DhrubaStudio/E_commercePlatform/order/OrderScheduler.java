package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderScheduler(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelUnpaidOrders() {
        log.debug("Running scheduled task: cancelUnpaidOrders...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(5);
        List<Order> expiredOrders = orderRepository.findByStatusAndOrderDateBefore(
                Order.OrderStatus.PENDING, cutoffTime);

        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired pending orders. Canceling and restoring inventory...", expiredOrders.size());

            for (Order order : expiredOrders) {
                order.setStatus(Order.OrderStatus.CANCELLED);

                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }

                orderRepository.save(order);
                log.info("Order ID {} successfully cancelled due to payment timeout.", order.getId());
            }
        }
    }
}