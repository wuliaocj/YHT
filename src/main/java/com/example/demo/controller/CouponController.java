package com.example.demo.controller;

import com.example.demo.domain.Coupon;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // 管理员接口
    @GetMapping("/api/admin/coupon/list")
    public HttpResult adminList() {
        List<Coupon> list = couponService.listAll();
        return HttpResult.ok(list);
    }

    @PostMapping("/api/admin/coupon/save")
    public HttpResult adminSave(@RequestBody Coupon coupon) {
        Coupon saved = couponService.save(coupon);
        return HttpResult.ok(saved);
    }

    @PostMapping("/api/admin/coupon/delete/{id}")
    public HttpResult adminDelete(@PathVariable Integer id) {
        couponService.delete(id);
        return HttpResult.ok("删除成功");
    }

    // 用户接口
    @GetMapping("/api/coupon/available/{userId}")
    public HttpResult listAvailableCoupons(@PathVariable Integer userId) {
        List<Coupon> list = couponService.listAvailableCoupons(userId);
        return HttpResult.ok(list);
    }

    @GetMapping("/api/coupon/user/{userId}")
    public HttpResult listUserCoupons(@PathVariable Integer userId) {
        return HttpResult.ok(couponService.listUserCoupons(userId));
    }

    @GetMapping("/api/coupon/user/available/{userId}")
    public HttpResult listUserAvailableCoupons(@PathVariable Integer userId) {
        return HttpResult.ok(couponService.listUserAvailableCoupons(userId));
    }

    @PostMapping("/api/coupon/receive")
    public HttpResult receiveCoupon(@RequestParam Integer userId, @RequestParam Integer couponId) {
        try {
            couponService.receiveCoupon(userId, couponId);
            return HttpResult.ok("领取成功");
        } catch (Exception e) {
            return HttpResult.error(e.getMessage());
        }
    }
}
