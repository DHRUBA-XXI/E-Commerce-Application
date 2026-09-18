package com.DhrubaStudio.E_commercePlatform.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM Order o " + "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.product " + "WHERE o.user.email = :email")
    List<Order> findCompleteOrderHistoryByEmail(@Param("email") String email);

    List<Order> findByStatusAndOrderDateBefore(Order.OrderStatus status, LocalDateTime cutoffTime);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    List<Order> findByStatus(Order.OrderStatus status);
}
