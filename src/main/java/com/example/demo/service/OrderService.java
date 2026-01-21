package com.example.demo.service;

import com.example.demo.domain.Order;
import com.example.demo.vo.CreateOrderDTO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {

    Order createOrder(Integer userId,Order order);

//    @Transactional(rollbackFor = Exception.class) // 任何异常都回滚
//    Order createOrder(Integer userId, Order order);


    List<Order> listUserOrders(Integer userId);

    Order getOrderDetail(Integer orderId);

    PageResponseVO<Order> listOrdersByPage(PageRequestVO pageRequest);

    List<Order> listAllOrders();

    void updateOrderStatus(Integer orderId, Integer status, String adminRemark);
}


