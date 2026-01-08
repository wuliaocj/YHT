package com.example.demo.controller;

import com.example.demo.domain.Order;
import com.example.demo.http.HttpResult;
import com.example.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public HttpResult create(@RequestParam Integer userId) {
        Order order = orderService.createOrder(userId);
        if (order == null) {
            return HttpResult.error("没有可结算的购物车商品");
        }
        return HttpResult.ok(order);
    }

    @GetMapping("/list/{userId}")
    public HttpResult list(@PathVariable Integer userId) {
        List<Order> list = orderService.listUserOrders(userId);
        return HttpResult.ok(list);
    }

    @GetMapping("/{orderId}")
    public HttpResult detail(@PathVariable Integer orderId) {
        Order order = orderService.getOrderDetail(orderId);
        if (order == null) {
            return HttpResult.error("订单不存在");
        }
        return HttpResult.ok(order);
    }

    // 管理后台接口
    @GetMapping("/admin/order/list")
    public HttpResult adminListOrders() {
        return HttpResult.ok(orderService.listAllOrders());
    }

    @PostMapping("/admin/order/update")
    public HttpResult adminUpdateOrder(@RequestBody Map<String, Object> request) {
        Integer orderId = (Integer) request.get("orderId");
        Integer status = (Integer) request.get("status");
        String adminRemark = (String) request.get("adminRemark");
        orderService.updateOrderStatus(orderId, status, adminRemark);
        return HttpResult.ok("更新成功");
    }
}


