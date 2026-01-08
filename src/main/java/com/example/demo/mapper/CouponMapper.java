package com.example.demo.mapper;

import com.example.demo.domain.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {

    Coupon selectById(@Param("id") Integer id);

    List<Coupon> selectAll();

    int insert(Coupon coupon);

    int update(Coupon coupon);

    int delete(@Param("id") Integer id);
}
