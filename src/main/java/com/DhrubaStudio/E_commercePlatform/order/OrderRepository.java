package com.DhrubaStudio.E_commercePlatform.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByStatusAndOrderDateBefore(Order.OrderStatus status, LocalDateTime cutoffTime);
}
