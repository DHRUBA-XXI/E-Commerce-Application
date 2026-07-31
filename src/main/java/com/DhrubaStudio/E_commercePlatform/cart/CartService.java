package com.DhrubaStudio.E_commercePlatform.cart;
import com.DhrubaStudio.E_commercePlatform.cart.dto.CartItemDTO;
import com.DhrubaStudio.E_commercePlatform.cart.dto.CartResponseDTO;
import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository =  productRepository;
    }

    public Cart getCart(String userEmail) {
        log.debug("Fetching cart for user: {}", userEmail);
        return cartRepository.findById(userEmail).orElseGet(() -> {
            log.debug("No existing cart found for user: {}. Creating a new one.", userEmail);
            return new Cart(userEmail);
        });
    }

    public Cart addItemToCart(String userEmail, Long productId, Integer quantity) {
        log.info("Adding item to cart for user: {}. Product ID: {}, Quantity: {}", userEmail, productId, quantity);

        Cart cart = getCart(userEmail);
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)).findFirst();

        if(existingCartItem.isPresent()) {
            log.debug("Product already exists in cart. Updating quantity.");
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + quantity);
        } else {
            log.debug("Adding new product to cart.");
            cart.getItems().add(new CartItem(productId, quantity));
        }

        return cartRepository.save(cart);
    }

    public void clearCart(String userEmail) {
        log.info("Clearing cart for user: {}", userEmail);
        cartRepository.deleteById(userEmail);
    }

    public CartResponseDTO getCartResponse(String userEmail) {
        log.debug("Generating detailed CartResponseDTO for user: {}", userEmail);

        Cart cart = getCart(userEmail);
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> {
                        log.error("Failed to generate cart response. Product ID {} not found in inventory.", item.getProductId());
                        return new IllegalArgumentException("Product not found: " + item.getProductId());
                    });

            BigDecimal subTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            cartTotal = cartTotal.add(subTotal);

            itemDTOs.add(new CartItemDTO(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity(),
                    subTotal
            ));
        }

        log.debug("Calculated cart total for user {}: Rs {}", userEmail, cartTotal);
        return new CartResponseDTO(itemDTOs, cartTotal);
    }
}