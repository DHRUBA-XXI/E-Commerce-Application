package com.DhrubaStudio.E_commercePlatform.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDTO {

    private List<CartItemDTO> items;
    private BigDecimal cartTotal;

    public CartResponseDTO() {}

    public CartResponseDTO(List<CartItemDTO> items, BigDecimal cartTotal) {
        this.items = items;
        this.cartTotal = cartTotal;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }
    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
    public BigDecimal getCartTotal() {
        return cartTotal;
    }
    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }
}
