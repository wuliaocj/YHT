package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    Order selectById(@Param("id") Integer id);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    List<Order> selectByUserId(@Param("userId") Integer userId);

    List<Order> selectAll();

    int update(Order order);

    int insert(Order order);
    
    Order selectByTakeCode(@Param("takeCode") String takeCode);

    /**
     * 分页查询订单
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 订单列表
     */
    List<Order> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);
}


