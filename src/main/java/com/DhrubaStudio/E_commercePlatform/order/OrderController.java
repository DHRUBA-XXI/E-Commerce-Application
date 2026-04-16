package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.order.dto.OrderRequestDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

     private final OrderService orderService;

     @Autowired
     public OrderController(OrderService orderService) {
         this.orderService = orderService;
     }

     @PostMapping("/checkout")
     public ResponseEntity<?> checkout(@RequestBody OrderRequestDTO orderRequestDTO) {

         Order savedOrder = orderService.processCheckout(orderRequestDTO);
         String message = "Checkout successful. Order ID: "+ savedOrder.getId() +
                     " .Total Amount: "+ savedOrder.getTotalAmount();

         return new ResponseEntity<>(message, HttpStatus.CREATED);
     }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrderHistory(@PathVariable Long userId) {

         List<OrderResponseDTO> history = orderService.getOrderHistory(userId);

         return new ResponseEntity<>(history, HttpStatus.OK);
    }

}
