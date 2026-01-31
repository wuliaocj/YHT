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

    /**
     * 快速重新下单
     * @param userId 用户ID
     * @param oldOrderId 历史订单ID
     * @return 新创建的订单
     */
    Order reorder(Integer userId, Integer oldOrderId);
    
    /**
     * 验证取餐码
     * @param takeCode 取餐码
     * @return 订单信息
     */
    Order validateTakeCode(String takeCode);
}


