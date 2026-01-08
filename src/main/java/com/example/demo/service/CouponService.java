package com.example.demo.service;

import com.example.demo.domain.Coupon;

import java.util.List;

public interface CouponService {

    List<Coupon> listAll();

    Coupon save(Coupon coupon);

    void delete(Integer id);
}
