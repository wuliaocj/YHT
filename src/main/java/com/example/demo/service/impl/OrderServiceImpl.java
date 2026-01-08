package com.example.demo.service.impl;

import com.example.demo.domain.Cart;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.OrderService;
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
    public Order createOrder(Integer userId) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<Cart> selected = new ArrayList<>();
        for (Cart c : cartList) {
            if (c.getIsSelected() != null && c.getIsSelected() == 1) {
                selected.add(c);
            }
        }
        if (selected.isEmpty()) {
            return null;
        }

        BigDecimal totalAmount = selected.stream()
                .map(Cart::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setDeliveryFee(BigDecimal.ZERO);
        order.setActualAmount(totalAmount);
        order.setPaymentMethod(1);
        order.setPaymentStatus(0);
        order.setOrderStatus(0);
        order.setOrderType(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        List<OrderItem> items = new ArrayList<>();
        for (Cart c : selected) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setProductId(c.getProductId());
            item.setSpecInfo(c.getSelectedSpecs());
            item.setUnitPrice(c.getTotalPrice().divide(BigDecimal.valueOf(c.getQuantity())));
            item.setQuantity(c.getQuantity());
            item.setTotalPrice(c.getTotalPrice());
            items.add(item);
        }
        orderItemMapper.insertBatch(items);

        cartMapper.deleteByUserId(userId);

        return orderMapper.selectById(order.getId());
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


