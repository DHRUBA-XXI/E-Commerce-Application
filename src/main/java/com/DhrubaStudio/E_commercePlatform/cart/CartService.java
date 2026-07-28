package com.DhrubaStudio.E_commercePlatform.cart;

import com.DhrubaStudio.E_commercePlatform.cart.dto.CartItemDTO;
import com.DhrubaStudio.E_commercePlatform.cart.dto.CartResponseDTO;
import com.DhrubaStudio.E_commercePlatform.inventory.Product;
import com.DhrubaStudio.E_commercePlatform.inventory.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return cartRepository.findById(userEmail).orElse(new Cart(userEmail));
    }

    public Cart addItemToCart(String userEmail, Long productId, Integer quantity) {
        Cart cart = getCart(userEmail);

        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)).findFirst();

        if(existingCartItem.isPresent()) {
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + quantity);
        } else {
            cart.getItems().add(new CartItem(productId, quantity));
        }
        return cartRepository.save(cart);
    }

    public void clearCart(String userEmail) {
        cartRepository.deleteById(userEmail);
    }

    public CartResponseDTO getCartResponse(String userEmail) {
        Cart cart = getCart(userEmail);

        List<CartItemDTO> itemDTOs = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));

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

        return new CartResponseDTO(itemDTOs, cartTotal);
    }

}
