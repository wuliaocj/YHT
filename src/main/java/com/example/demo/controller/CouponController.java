package com.example.demo.controller;

import com.example.demo.domain.Coupon;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
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
