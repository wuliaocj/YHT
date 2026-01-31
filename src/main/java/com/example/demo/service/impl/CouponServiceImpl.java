package com.example.demo.service.impl;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.UserCoupon;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.mapper.UserCouponMapper;
import com.example.demo.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    public CouponServiceImpl(CouponMapper couponMapper, UserCouponMapper userCouponMapper) {
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
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

    @Override
    public List<Coupon> listAvailableCoupons(Integer userId) {
        return couponMapper.selectAll();
    }

    @Override
    public List<UserCoupon> listUserCoupons(Integer userId) {
        return userCouponMapper.selectByUserId(userId);
    }

    @Override
    public List<UserCoupon> listUserAvailableCoupons(Integer userId) {
        return userCouponMapper.selectAvailableByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveCoupon(Integer userId, Integer couponId) {
        // 1. 查询优惠券信息
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        // 2. 检查优惠券状态
        if (coupon.getStatus() != 1) {
            throw new RuntimeException("优惠券不可用");
        }

        // 3. 检查优惠券剩余数量
        if (coupon.getRemainingCount() <= 0) {
            throw new RuntimeException("优惠券已领完");
        }

        // 4. 检查用户领取数量限制
        int userCouponCount = userCouponMapper.countByUserIdAndCouponId(userId, couponId);
        if (userCouponCount >= coupon.getLimitPerUser()) {
            throw new RuntimeException("已达到领取上限");
        }

        // 5. 生成优惠券码
        String couponCode = generateCouponCode();

        // 6. 计算过期时间
        LocalDateTime expireTime;
        if (coupon.getValidityType() == 1) {
            // 固定时间范围
            expireTime = coupon.getEndTime().atTime(23, 59, 59);
        } else {
            // 领取后N天有效
            expireTime = LocalDateTime.now().plusDays(coupon.getValidDays());
        }

        // 7. 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponCode(couponCode);
        userCoupon.setStatus(1); // 未使用
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setExpireTime(expireTime);

        // 8. 保存用户优惠券
        userCouponMapper.insert(userCoupon);

        // 9. 更新优惠券剩余数量
        coupon.setRemainingCount(coupon.getRemainingCount() - 1);
        couponMapper.update(coupon);

        return true;
    }

    @Override
    public UserCoupon getCouponByCode(String couponCode) {
        return userCouponMapper.selectByCouponCode(couponCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useCoupon(Integer userId, Integer couponId, Integer orderId) {
        // 1. 查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectByUserIdAndCouponId(userId, couponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        // 2. 检查优惠券状态
        if (userCoupon.getStatus() != 1) {
            throw new RuntimeException("优惠券已使用");
        }

        // 3. 检查是否过期
        if (userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已过期");
        }

        // 4. 更新优惠券状态
        userCoupon.setStatus(2); // 已使用
        userCoupon.setUsedTime(LocalDateTime.now());
        userCoupon.setUsedOrderId(orderId);
        userCouponMapper.update(userCoupon);

        return true;
    }

    @Override
    public Coupon getCouponById(Integer couponId) {
        return couponMapper.selectById(couponId);
    }

    // 生成优惠券码
    private String generateCouponCode() {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
