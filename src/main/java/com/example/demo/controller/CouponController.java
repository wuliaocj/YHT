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
    @RequestMapping("/api/admin/coupon")
    public static class AdminCouponController {
        private final CouponService couponService;

        public AdminCouponController(CouponService couponService) {
            this.couponService = couponService;
        }

        @GetMapping("/list")
        public HttpResult list() {
            List<Coupon> list = couponService.listAll();
            return HttpResult.ok(list);
        }

        @PostMapping("/save")
        public HttpResult save(@RequestBody Coupon coupon) {
            Coupon saved = couponService.save(coupon);
            return HttpResult.ok(saved);
        }

        @PostMapping("/delete/{id}")
        public HttpResult delete(@PathVariable Integer id) {
            couponService.delete(id);
            return HttpResult.ok("删除成功");
        }
    }

    // 用户接口
    @RequestMapping("/api/coupon")
    public static class UserCouponController {
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
}
