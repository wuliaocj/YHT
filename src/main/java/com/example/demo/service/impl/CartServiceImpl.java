package com.example.demo.service.impl;

import com.example.demo.domain.Cart;
import com.example.demo.domain.Product;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartServiceImpl(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    @Override
    public List<Cart> listUserCart(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void addToCart(Integer userId, Integer productId, Integer quantity, String selectedSpecs) {
        Cart exist = cartMapper.selectByUserAndProduct(userId, productId);
        Product product = productMapper.selectById(productId);
        BigDecimal price = product.getBasePrice().multiply(BigDecimal.valueOf(quantity));

        if (exist == null) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelectedSpecs(selectedSpecs);
            cart.setTotalPrice(price);
            cart.setIsSelected(1);
            cartMapper.insert(cart);
        } else {
            exist.setQuantity(exist.getQuantity() + quantity);
            exist.setSelectedSpecs(selectedSpecs);
            exist.setTotalPrice(product.getBasePrice().multiply(BigDecimal.valueOf(exist.getQuantity())));
            cartMapper.update(exist);
        }
    }

    @Override
    @Transactional
    public void updateCartItem(Integer cartId, Integer quantity, String selectedSpecs, Boolean selected) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            return;
        }
        Product product = productMapper.selectById(cart.getProductId());
        cart.setQuantity(quantity);
        cart.setSelectedSpecs(selectedSpecs);
        cart.setTotalPrice(product.getBasePrice().multiply(BigDecimal.valueOf(quantity)));
        if (selected != null) {
            cart.setIsSelected(selected ? 1 : 0);
        }
        cartMapper.update(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Integer cartId) {
        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void clearUserCart(Integer userId) {
        cartMapper.deleteByUserId(userId);
    }
}


