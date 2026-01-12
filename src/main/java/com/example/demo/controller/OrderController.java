package com.example.demo.controller;

import com.example.demo.domain.Order;
import com.example.demo.http.HttpResult;
import com.example.demo.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * @param userId
     * @return
     */
    @PostMapping("/create")
    public HttpResult create(@RequestParam Integer userId, @RequestBody Order order) {
        // 1. 基础参数校验
        if (userId == null || userId <= 0) {
            return HttpResult.error("用户ID无效，请传入正整数");
        }
        if (order.getOrderType() == null || (order.getOrderType() != 1 && order.getOrderType() != 2)) {
            return HttpResult.error("订单类型无效，仅支持1=堂食、2=外卖");
        }
        if (order.getPaymentMethod() == null || (order.getPaymentMethod() < 1 || order.getPaymentMethod() > 3)) {
            return HttpResult.error("支付方式无效，仅支持1=微信、2=支付宝、3=现金");
        }

        // 2. 补全默认值（外卖默认配送费，堂食配送费=0）
        if (order.getOrderType() == 1) { // 堂食
            order.setDeliveryFee(BigDecimal.ZERO);
        } else if (order.getDeliveryFee() == null) { // 外卖未传配送费，设默认值
            order.setDeliveryFee(new BigDecimal("3.00"));
        }

        // 3. 调用Service创建订单
        Order createdOrder = orderService.createOrder(userId, order);
        if (createdOrder == null) {
            return HttpResult.error("没有可结算的购物车商品");
        }

        return HttpResult.ok(createdOrder);
    }

    /**
     * 根据用户ID获取订单
     * @param userId
     * @return
     */
    @GetMapping("/list/{userId}")
    public HttpResult list(@PathVariable Integer userId) {
        List<Order> list = orderService.listUserOrders(userId);
        return HttpResult.ok(list);
    }

    /**
     * 根据订单ID获取详情
     * @param orderId
     * @return
     */
    @GetMapping("/{orderId}")
    public HttpResult detail(@PathVariable Integer orderId) {
        Order order = orderService.getOrderDetail(orderId);
        if (order == null) {
            return HttpResult.error("订单不存在");
        }
        return HttpResult.ok(order);
    }

    /**
     * 管理员获取订单列表
     * @return
     */
    // 管理后台接口
    @GetMapping("/admin/order/list")
    public HttpResult adminListOrders() {
        return HttpResult.ok(orderService.listAllOrders());
    }

    /**
     * 管理员对订单更新
     * @param request
     * @return
     */
    @PostMapping("/admin/order/update")
    public HttpResult adminUpdateOrder(@RequestBody Map<String, Object> request) {
        Integer orderId = (Integer) request.get("orderId");
        Integer status = (Integer) request.get("status");
        String adminRemark = (String) request.get("adminRemark");
        orderService.updateOrderStatus(orderId, status, adminRemark);
        return HttpResult.ok("更新成功");
    }
}


