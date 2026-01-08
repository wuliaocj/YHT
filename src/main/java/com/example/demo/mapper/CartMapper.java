package com.example.demo.mapper;

import com.example.demo.domain.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    Cart selectById(@Param("id") Integer id);

    List<Cart> selectByUserId(@Param("userId") Integer userId);

    Cart selectByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int insert(Cart cart);

    int update(Cart cart);

    int deleteById(@Param("id") Integer id);

    int deleteByUserId(@Param("userId") Integer userId);
}


