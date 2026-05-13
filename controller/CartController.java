package com.example.demo.controller;

import com.example.demo.domain.Cart;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CartService;
import com.example.demo.service.UserService;
import com.example.demo.vo.AddCartVO;
import com.example.demo.vo.UpdateCartVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * 获取当前用户的购物车列表
     * @return 购物车列表
     */
    @GetMapping("/list")
    public HttpResult list() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<Cart> list = cartService.listUserCart(userId);
        return HttpResult.ok(list);
    }

    /**
     * 根据用户ID获取购物车列表（管理员接口）
     * @param userId 用户ID
     * @return 购物车列表
     */
    @GetMapping("/list/{userId}")
    public HttpResult listByUserId(@PathVariable Integer userId) {
        List<Cart> list = cartService.listUserCart(userId);
        return HttpResult.ok(list);
    }

    /**
     * 添加商品到购物车
     * @param addCartVO 购物车添加参数
     * @return 添加结果
     */
    @PostMapping("/add")
    public HttpResult addToCart(@RequestBody AddCartVO addCartVO) throws JsonProcessingException {
        // 强制从JWT获取当前登录用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录，请先登录");
        }
        addCartVO.setUserId(userId);
        log.info("用户{}加入购物车成功，商品ID：{}", userId, addCartVO.getProductId());
        return HttpResult.ok("加入购物车成功", cartService.addToCart(addCartVO));
    }

    /**
     * 更新购物车商品
     * @param updateCartVO 更新购物车参数
     * @return 更新结果
     */
    @PostMapping("/update")
    public HttpResult update(@RequestBody UpdateCartVO updateCartVO) {
        if (updateCartVO == null || updateCartVO.getCartId() == null) {
            return HttpResult.error("参数错误");
        }
        cartService.updateCartItem(
            updateCartVO.getCartId(),
            updateCartVO.getQuantity(),
            updateCartVO.getSelectedSpecs(),
            updateCartVO.getSelected()
        );
        log.info("用户{}更新购物车成功，购物车ID：{}", getCurrentUserId(), updateCartVO.getCartId());
        return HttpResult.ok();
    }

    /**
     * 移除购物车商品
     * @param cartId 购物车ID
     * @return 移除结果
     */
    @PostMapping("/remove/{cartId}")
    public HttpResult remove(@PathVariable Integer cartId) {
        cartService.removeCartItem(cartId);
        log.info("用户{}移除购物车成功，商品ID：{}", getCurrentUserId(), cartId);
        return HttpResult.ok();
    }

    /**
     * 清空当前用户购物车
     * @return 清空结果
     */
    @PostMapping("/clear")
    public HttpResult clear() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        cartService.clearUserCart(userId);
        log.info("用户{}清空购物车成功", userId);
        return HttpResult.ok();
    }

    /**
     * 根据用户ID清空购物车（管理员接口）
     * @param userId 用户ID
     * @return 清空结果
     */
    @PostMapping("/clear/{userId}")
    public HttpResult clearByUserId(@PathVariable Integer userId) {
        cartService.clearUserCart(userId);
        log.info("管理员清空用户{}购物车成功", userId);
        return HttpResult.ok();
    }

    /**
     * 获取购物车商品数量
     * @return 购物车数量
     */
    @GetMapping("/count")
    public HttpResult getCartCount() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<Cart> list = cartService.listUserCart(userId);
        int count = list.stream().mapToInt(Cart::getQuantity).sum();
        return HttpResult.ok(count);
    }



    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String openid = (String) authentication.getPrincipal();
            if (openid == null) {
                return null;
            }

            User user = userService.getUserByOpenid(openid);
            log.info("获取当前用户ID成功：{}", user.getId());
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }
}
