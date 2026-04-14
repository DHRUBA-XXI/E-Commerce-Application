package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.order.dto.OrderRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
         try{
             Order savedOrder = orderService.processCheckout(orderRequestDTO);
             String message = "Checkout successful. Order ID: "+ savedOrder.getId() +
                     " .Total Amount: "+ savedOrder.getTotalAmount();
             return new ResponseEntity<>(message, HttpStatus.CREATED);
         } catch(IllegalArgumentException e){
             return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
     }

}
