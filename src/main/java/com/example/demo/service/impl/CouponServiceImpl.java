package com.example.demo.service.impl;

import com.example.demo.domain.Coupon;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.service.CouponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;

    public CouponServiceImpl(CouponMapper couponMapper) {
        this.couponMapper = couponMapper;
    }

    @Override
    public List<Coupon> listAll() {
        return couponMapper.selectAll();
    }

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) {
            couponMapper.insert(coupon);
        } else {
            couponMapper.update(coupon);
        }
        return coupon;
    }

    @Override
    public void delete(Integer id) {
        couponMapper.delete(id);
    }
}
