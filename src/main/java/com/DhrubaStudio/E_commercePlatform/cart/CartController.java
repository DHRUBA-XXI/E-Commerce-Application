package com.DhrubaStudio.E_commercePlatform.cart;

import com.DhrubaStudio.E_commercePlatform.cart.dto.CartResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        CartResponseDTO cartResponse = cartService.getCartResponse(email);

        return new ResponseEntity<>(cartResponse, HttpStatus.OK);
    }

    @PostMapping("/addItem")
    public ResponseEntity<CartResponseDTO> addItemToCart(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        String email = userDetails.getUsername();

        cartService.addItemToCart(email, productId, quantity);

        CartResponseDTO cartResponse = cartService.getCartResponse(email);

        return new ResponseEntity<>(cartResponse, HttpStatus.OK);
    }

}
