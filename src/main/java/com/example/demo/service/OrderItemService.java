package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {

    List<OrderItem> getItemsByOrderNo(String orderNo);

    List<OrderItem> getItemsByOrderId(Integer orderId);

}
