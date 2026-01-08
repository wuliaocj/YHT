package com.example.demo.service;

import com.example.demo.domain.Cart;

import java.util.List;

public interface CartService {

    List<Cart> listUserCart(Integer userId);

    void addToCart(Integer userId, Integer productId, Integer quantity, String selectedSpecs);

    void updateCartItem(Integer cartId, Integer quantity, String selectedSpecs, Boolean selected);

    void removeCartItem(Integer cartId);

    void clearUserCart(Integer userId);
}


