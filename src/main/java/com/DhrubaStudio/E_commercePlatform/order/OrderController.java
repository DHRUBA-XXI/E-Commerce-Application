package com.DhrubaStudio.E_commercePlatform.order;

import com.DhrubaStudio.E_commercePlatform.order.dto.OrderResponseDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.OrderStatusUpdateRequestDTO;
import com.DhrubaStudio.E_commercePlatform.order.dto.PaymentVerificationDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
     public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
         String email = userDetails.getUsername();

         Order savedOrder = orderService.processCheckout(email);
         String message = "Checkout successful. Order ID: "+ savedOrder.getId() +
                 "\nTotal amount: "+ savedOrder.getTotalAmount() +
                 "\nRazorpay Order ID: " + savedOrder.getRazorpayOrderId();

         return new ResponseEntity<>(message, HttpStatus.CREATED);
     }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDTO verificationDTO) {
        orderService.verifyRazorpayPayment(verificationDTO);
        return new ResponseEntity<>("Payment verified and confirmed for your order ! Receipt has been sent to your email.", HttpStatus.OK);
    }

    @GetMapping("/orderHistory")
    public ResponseEntity<?> getUserOrderHistory(@AuthenticationPrincipal UserDetails userDetails) {

         String email = userDetails.getUsername();
         List<OrderResponseDTO> history = orderService.getOrderHistory(email);

         return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody OrderStatusUpdateRequestDTO request) {
        orderService.updateOrderStatus(orderId, request.getStatus());
        return new ResponseEntity<>("Order ID: " + orderId + " , successfully updated to: " + request.getStatus().toUpperCase(), HttpStatus.OK);
    }

}
