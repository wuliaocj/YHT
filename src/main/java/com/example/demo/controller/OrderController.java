package com.example.demo.controller;

import com.example.demo.domain.Order;
import com.example.demo.http.HttpResult;
import com.example.demo.service.OrderService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     * @param userId 用户ID
     * @param order 订单信息
     * @return 创建结果
     */
    @PostMapping("/create")
    public HttpResult create(@RequestParam Integer userId, @RequestBody Order order) {
        // 1. 基础参数校验
        if (userId == null || userId <= 0) {
            log.warn("创建订单失败：用户ID无效，userId：{}", userId);
            return HttpResult.error("用户ID无效，请传入正整数");
        }
        if (order.getOrderType() == null || (order.getOrderType() != 1 && order.getOrderType() != 2)) {
            log.warn("创建订单失败：订单类型无效，orderType：{}", order.getOrderType());
            return HttpResult.error("订单类型无效，仅支持1=堂食、2=外卖");
        }
        if (order.getPaymentMethod() == null || (order.getPaymentMethod() < 1 || order.getPaymentMethod() > 3)) {
            log.warn("创建订单失败：支付方式无效，paymentMethod：{}", order.getPaymentMethod());
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
            log.warn("创建订单失败：没有可结算的购物车商品，userId：{}", userId);
            return HttpResult.error("没有可结算的购物车商品");
        }

        log.info("创建订单成功，orderId：{}，userId：{}", createdOrder.getId(), userId);
        return HttpResult.ok(createdOrder);
    }

    /**
     * 根据用户ID获取订单
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/list/{userId}")
    public HttpResult list(@PathVariable Integer userId) {
        List<Order> list = orderService.listUserOrders(userId);
        log.debug("查询用户订单列表，userId：{}，订单数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 根据订单ID获取详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    public HttpResult detail(@PathVariable Integer orderId) {
        Order order = orderService.getOrderDetail(orderId);
        if (order == null) {
            log.warn("查询订单详情失败：订单不存在，orderId：{}", orderId);
            return HttpResult.error("订单不存在");
        }
        return HttpResult.ok(order);
    }

    /**
     * 管理员获取订单列表（分页）
     * @param pageRequest 分页请求参数
     * @return 分页订单列表
     */
    @GetMapping("/admin/order/list")
    public HttpResult adminListOrders(PageRequestVO pageRequest) {
        pageRequest.validate();
        PageResponseVO<Order> pageResponse = orderService.listOrdersByPage(pageRequest);
        log.debug("管理员查询订单列表，页码：{}，每页大小：{}，总记录数：{}",
                pageRequest.getPageNum(), pageRequest.getPageSize(), pageResponse.getTotal());
        return HttpResult.ok(pageResponse);
    }

    /**
     * 管理员对订单更新
     * @param request 更新请求（包含orderId、status、adminRemark）
     * @return 更新结果
     */
    @PostMapping("/admin/order/update")
    public HttpResult adminUpdateOrder(@RequestBody Map<String, Object> request) {
        Integer orderId = (Integer) request.get("orderId");
        Integer status = (Integer) request.get("status");
        String adminRemark = (String) request.get("adminRemark");

        if (orderId == null) {
            return HttpResult.error("订单ID不能为空");
        }

        orderService.updateOrderStatus(orderId, status, adminRemark);
        log.info("管理员更新订单成功，orderId：{}，status：{}", orderId, status);
        return HttpResult.ok("更新成功");
    }

}
