package com.example.demo.controller;

import com.example.demo.domain.Cart;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public HttpResult list(@PathVariable Integer userId) {
        List<Cart> list = cartService.listUserCart(userId);
        return HttpResult.ok(list);
    }

    @PostMapping("/add")
    public HttpResult add(@RequestParam Integer userId,
                          @RequestParam Integer productId,
                          @RequestParam(defaultValue = "1") Integer quantity,
                          @RequestParam(required = false) String selectedSpecs) {
        cartService.addToCart(userId, productId, quantity, selectedSpecs);
        return HttpResult.ok();
    }

    @PostMapping("/update")
    public HttpResult update(@RequestParam Integer cartId,
                             @RequestParam Integer quantity,
                             @RequestParam(required = false) String selectedSpecs,
                             @RequestParam(required = false) Boolean selected) {
        cartService.updateCartItem(cartId, quantity, selectedSpecs, selected);
        return HttpResult.ok();
    }

    @PostMapping("/remove/{cartId}")
    public HttpResult remove(@PathVariable Integer cartId) {
        cartService.removeCartItem(cartId);
        return HttpResult.ok();
    }

    @PostMapping("/clear/{userId}")
    public HttpResult clear(@PathVariable Integer userId) {
        cartService.clearUserCart(userId);
        return HttpResult.ok();
    }
}


