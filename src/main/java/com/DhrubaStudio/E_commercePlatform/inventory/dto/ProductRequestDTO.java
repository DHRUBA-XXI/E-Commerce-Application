package com.DhrubaStudio.E_commercePlatform.inventory.dto;

import java.math.BigDecimal;

public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long categoryId;

    public ProductRequestDTO() {}

    public String getName() {
        return name; }

    public void setName(String name) {
        this.name = name; }

    public String getDescription() {
        return description; }

    public void setDescription(String description) {
        this.description = description; }

    public BigDecimal getPrice() {
        return price; }

    public void setPrice(BigDecimal price) {
        this.price = price; }

    public Integer getStockQuantity() {
        return stockQuantity; }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity; }

    public Long getCategoryId() {
        return categoryId; }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId; }
}
