package com.example.demo.service;

import com.example.demo.domain.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Integer userId,Order order);

    List<Order> listUserOrders(Integer userId);

    Order getOrderDetail(Integer orderId);

    List<Order> listAllOrders();

    void updateOrderStatus(Integer orderId, Integer status, String adminRemark);
}


