package com.example.demo.service;

import com.example.demo.domain.Cart;
import com.example.demo.vo.AddCartVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {

    List<Cart> listUserCart(Integer userId);

    AddCartVO addToCart(AddCartVO addCartVO) throws JsonProcessingException;

    void updateCartItem(Integer cartId, Integer quantity, String selectedSpecs, Boolean selected);

    void removeCartItem(Integer cartId);

    void clearUserCart(Integer userId);
}


