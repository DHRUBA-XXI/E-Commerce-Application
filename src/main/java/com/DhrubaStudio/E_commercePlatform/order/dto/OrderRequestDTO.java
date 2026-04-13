package com.DhrubaStudio.E_commercePlatform.order.dto;

import java.util.List;

public class OrderRequestDTO {

    private Long userId;
    private List<OrderItemRequestDTO> items;

    public OrderRequestDTO() {}

    public Long getUserId() {
        return userId; }

    public void setUserId(Long userId) {
        this.userId = userId; }

    public List<OrderItemRequestDTO> getItems() {
        return items; }

    public void setItems(List<OrderItemRequestDTO> items) {
        this.items = items; }
}
