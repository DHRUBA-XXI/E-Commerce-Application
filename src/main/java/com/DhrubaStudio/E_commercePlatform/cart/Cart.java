package com.DhrubaStudio.E_commercePlatform.cart;


import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

@RedisHash(value = "Cart", timeToLive = 259200)
public class Cart {

    @Id
    private String id;

    private List<CartItem> items = new ArrayList<CartItem>();

    public Cart() {}

    public Cart(String id) {
        this.id = id;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
