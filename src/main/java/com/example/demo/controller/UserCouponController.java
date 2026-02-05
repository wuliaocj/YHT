package com.example.demo.controller;

import com.example.demo.domain.Coupon;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupon")
public class UserCouponController {
    private final CouponService couponService;

    public UserCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/available/{userId}")
    public HttpResult listAvailableCoupons(@PathVariable Integer userId) {
        List<Coupon> list = couponService.listAvailableCoupons(userId);
        return HttpResult.ok(list);
    }

    @GetMapping("/user/{userId}")
    public HttpResult listUserCoupons(@PathVariable Integer userId) {
        return HttpResult.ok(couponService.listUserCoupons(userId));
    }

    @GetMapping("/user/available/{userId}")
    public HttpResult listUserAvailableCoupons(@PathVariable Integer userId) {
        return HttpResult.ok(couponService.listUserAvailableCoupons(userId));
    }

    @PostMapping("/receive")
    public HttpResult receiveCoupon(@RequestParam Integer userId, @RequestParam Integer couponId) {
        try {
            couponService.receiveCoupon(userId, couponId);
            return HttpResult.ok("领取成功");
        } catch (Exception e) {
            return HttpResult.error(e.getMessage());
        }
    }
}
