package com.example.demo.mapper;

import com.example.demo.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    int insertBatch(@Param("items") List<OrderItem> items);
}


