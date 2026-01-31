package com.example.demo.service;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.UserCoupon;

import java.util.List;

public interface CouponService {

    List<Coupon> listAll();

    Coupon save(Coupon coupon);

    void delete(Integer id);
    
    // 用户相关方法
    List<Coupon> listAvailableCoupons(Integer userId);
    
    List<UserCoupon> listUserCoupons(Integer userId);
    
    List<UserCoupon> listUserAvailableCoupons(Integer userId);
    
    boolean receiveCoupon(Integer userId, Integer couponId);
    
    UserCoupon getCouponByCode(String couponCode);
    
    boolean useCoupon(Integer userId, Integer couponId, Integer orderId);
    
    Coupon getCouponById(Integer couponId);
}
