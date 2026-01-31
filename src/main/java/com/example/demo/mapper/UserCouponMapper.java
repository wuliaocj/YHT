package com.example.demo.mapper;

import com.example.demo.domain.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCouponMapper {

    UserCoupon selectById(@Param("id") Integer id);

    List<UserCoupon> selectByUserId(@Param("userId") Integer userId);

    List<UserCoupon> selectAvailableByUserId(@Param("userId") Integer userId);

    UserCoupon selectByCouponCode(@Param("couponCode") String couponCode);

    UserCoupon selectByUserIdAndCouponId(@Param("userId") Integer userId, @Param("couponId") Integer couponId);

    int countByUserIdAndCouponId(@Param("userId") Integer userId, @Param("couponId") Integer couponId);

    int insert(UserCoupon userCoupon);

    int update(UserCoupon userCoupon);

    int delete(@Param("id") Integer id);
}
