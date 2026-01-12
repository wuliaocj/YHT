package com.example.demo.service.impl;

import com.example.demo.domain.Cart;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;


    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
    }

    @Override
    @Transactional
    public Order createOrder(Integer userId, Order orderParam) {
//        // 1. 查询用户购物车商品，若无则返回null
//        List<Cart> cartItems = cartService.listUserCart(userId);
//        if (cartItems.isEmpty()) {
//            return null;
//        }
//
//        // 2. 计算商品总价
//        BigDecimal totalAmount = cartItems.stream()
//                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // 3. 计算优惠金额（优惠券/满减等）
//        BigDecimal discountAmount = couponService.calculateDiscount(userId, totalAmount);
//
//        // 4. 组装订单对象
//        Order order = new Order();
//        order.setUserId(userId);
//        order.setOrderNo(generateOrderNo()); // 生成唯一订单编号
//        order.setTotalAmount(totalAmount);
//        order.setDiscountAmount(discountAmount);
//        order.setDeliveryFee(orderParam.getDeliveryFee());
//        // 实际支付金额 = 总价 - 优惠 + 配送费
//        order.setActualAmount(totalAmount.subtract(discountAmount).add(orderParam.getDeliveryFee()));
//        order.setPaymentMethod(orderParam.getPaymentMethod());
//        order.setPaymentStatus(0); // 初始未支付
//        order.setOrderStatus(0); // 初始待支付
//        order.setOrderType(orderParam.getOrderType());
//        order.setTakeCode(generateTakeCode()); // 生成取餐码
//        order.setEstimatedTime(LocalDateTime.now().plusMinutes(10)); // 预计10分钟完成
//        order.setUserRemark(orderParam.getUserRemark());
//        order.setCreateTime(LocalDateTime.now());
//        order.setUpdateTime(LocalDateTime.now());
//
//        // 5. 保存订单到数据库
//        orderMapper.insert(order);

        /**
         * 暂时使用
         */
        Order order = new Order();
        // 6. 清空用户购物车
//        cartService.clearUserCart(userId);
        return order;
    }


    @Override
    public List<Order> listUserOrders(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public Order getOrderDetail(Integer orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public List<Order> listAllOrders() {
        return orderMapper.selectAll();
    }

    @Override
    public void updateOrderStatus(Integer orderId, Integer status, String adminRemark) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setOrderStatus(status);
        if (adminRemark != null) {
            order.setAdminRemark(adminRemark);
        }
        if (status == 3) {
            order.setCompleteTime(LocalDateTime.now());
        }
        orderMapper.update(order);
    }
}


