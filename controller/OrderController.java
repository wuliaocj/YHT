package com.example.demo.controller;

import com.example.demo.domain.Address;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.domain.RefundRecord;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.service.LogService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.service.RefundService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.vo.OrderDetailVO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.annotation.RequiresPermission;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单管理")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefundService refundService;
    private final LogService logService;
    private final ProductSpecPriceService productSpecPriceService;

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);
            
            HttpServletRequest request = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null 
                ? ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest()
                : null;
            
            if (request != null) {
                operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
                operationLog.setIp(getClientIp(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
                
                // 获取当前登录管理员ID
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    String tokenValue = token.substring(7);
                    Integer adminId = jwtUtil.getAdminIdFromToken(tokenValue);
                    operationLog.setAdminId(adminId);
                }
            }
            
            logService.recordOperationLog(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 创建订单
     * @param order 订单信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public HttpResult create(@RequestBody Order order) {
        // 1. 强制从JWT获取当前登录用户ID，禁止从请求体传入（安全考虑）
        Integer userId = getCurrentUserId();
        if (userId == null) {
            log.warn("创建订单失败：用户未登录");
            return HttpResult.error("用户未登录，请先登录");
        }

        // 2. 基础参数校验
        if (order.getOrderType() == null || (order.getOrderType() != 1 && order.getOrderType() != 2)) {
            log.warn("创建订单失败：订单类型无效，orderType：{}", order.getOrderType());
            return HttpResult.error("订单类型无效，仅支持1=堂食、2=外卖");
        }
        if (order.getPaymentMethod() == null || (order.getPaymentMethod() < 1 || order.getPaymentMethod() > 3)) {
            log.warn("创建订单失败：支付方式无效，paymentMethod：{}", order.getPaymentMethod());
            return HttpResult.error("支付方式无效，仅支持1=微信、2=支付宝、3=现金");
        }
        // 外卖订单必须填写地址
        if (order.getOrderType() == 2 && (order.getAddressId() == null || order.getAddressId() <= 0)) {
            log.warn("创建订单失败：外卖订单必须填写地址，userId：{}", userId);
            return HttpResult.error("外卖订单必须填写地址");
        }

        // 3. 补全默认值（外卖默认配送费，堂食配送费=0）
        if (order.getOrderType() == 1) { // 堂食
            order.setDeliveryFee(BigDecimal.ZERO);
        } else if (order.getDeliveryFee() == null) { // 外卖未传配送费，设默认值
            order.setDeliveryFee(new BigDecimal("3.00"));
        }

        // 4. 调用Service创建订单
        Order createdOrder = orderService.createOrder(userId, order);
        if (createdOrder == null) {
            log.warn("创建订单失败：没有可结算的购物车商品，userId：{}", userId);
            return HttpResult.error("没有可结算的购物车商品");
        }

        log.info("创建订单成功，orderId：{}，userId：{}", createdOrder.getId(), userId);
        return HttpResult.ok(createdOrder);
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String openid = (String) authentication.getPrincipal();
            if (openid == null) {
                return null;
            }

            User user = userService.getUserByOpenid(openid);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }

    /**
     * 获取当前用户的订单列表
     * @return 订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取当前用户订单列表")
    public HttpResult list() {
        // 强制从JWT获取当前登录用户ID，禁止从请求参数传入
        Integer userId = getCurrentUserId();
        if (userId == null) {
            log.warn("查询订单列表失败：用户未登录");
            return HttpResult.error("用户未登录，请先登录");
        }
        List<OrderDetailVO> list = orderService.listUserOrderDetails(userId);
        log.info("查询用户订单列表，userId：{}，订单数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 根据用户ID获取订单列表（管理员接口）
     * @param userId 用户ID
     * @return 订单列表
     */
    @GetMapping("/list/{userId}")
    public HttpResult listByUserId(@PathVariable Integer userId) {
        if (userId == null) {
            log.warn("查询订单列表失败：用户ID不能为空");
            return HttpResult.error("用户ID不能为空");
        }
        List<Order> list = orderService.listUserOrders(userId);
        log.info("查询用户订单列表，userId：{}，订单数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 根据订单ID获取详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "获取订单详情")
    public HttpResult detail(
            @Parameter(description = "订单ID", example = "1") @PathVariable Integer orderId) {
        // 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        OrderDetailVO orderDetail = orderService.getOrderDetailVO(orderId);
        if (orderDetail == null) {
            log.warn("查询订单详情失败：订单不存在，orderId：{}", orderId);
            return HttpResult.error("订单不存在");
        }

        // 权限校验：只能查看自己的订单
        if (!userId.equals(orderDetail.getUserId())) {
            log.warn("查询订单详情失败：无权限，userId：{}，orderId：{}", userId, orderId);
            return HttpResult.error("无权查看该订单");
        }

        return HttpResult.ok(orderDetail);
    }

    /**
     * 管理员获取订单列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @param orderId 订单号
     * @return 分页订单列表
     */
    @GetMapping("/admin/order/list")
    @RequiresPermission(code = "order:read")
    public HttpResult adminListOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderId) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            PageResponseVO<Order> pageResponse = orderService.listOrdersByPage(pageRequest, orderId);
            log.info("管理员查询订单列表，页码：{}，每页大小：{}，总记录数：{}",
                    pageRequest.getPageNum(), pageRequest.getPageSize(), pageResponse.getTotal());
            recordOperationLog("订单管理", "查询订单列表", 1, null);
            return HttpResult.ok(pageResponse);
        } catch (Exception e) {
            log.error("查询订单列表失败：", e);
            recordOperationLog("订单管理", "查询订单列表", 0, e.getMessage());
            return HttpResult.error("查询订单列表失败：" + e.getMessage());
        }
    }

    /**
     * 管理员对订单更新
     * @param request 更新请求（包含orderId、status、adminRemark）
     * @return 更新结果
     */
    @PostMapping("/admin/order/update")
    @RequiresPermission(code = "order:manage")
    public HttpResult adminUpdateOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer orderId = (Integer) request.get("orderId");
            Integer status = (Integer) request.get("status");
            String adminRemark = (String) request.get("adminRemark");

            if (orderId == null) {
                recordOperationLog("订单管理", "更新订单", 0, "订单ID不能为空");
                return HttpResult.error("订单ID不能为空");
            }

            orderService.updateOrderStatus(orderId, status, adminRemark);
            log.info("管理员更新订单成功，orderId：{}，status：{}", orderId, status);
            recordOperationLog("订单管理", "更新订单", 1, null);
            return HttpResult.ok("更新成功");
        } catch (Exception e) {
            log.error("更新订单失败：", e);
            recordOperationLog("订单管理", "更新订单", 0, e.getMessage());
            return HttpResult.error("更新订单失败：" + e.getMessage());
        }
    }

    /**
     * 管理员取消订单（支持退款）
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 取消结果
     */
    @PostMapping("/admin/order/cancel/{orderId}")
    @RequiresPermission(code = "order:manage")
    public HttpResult adminCancelOrder(@PathVariable Integer orderId,
                                       @RequestParam(required = false) String reason) {
        try {
            if (orderId == null || orderId <= 0) {
                recordOperationLog("订单管理", "取消订单", 0, "订单ID无效");
                return HttpResult.error("订单ID无效");
            }
            orderService.adminCancelOrder(orderId, reason);
            log.info("管理员取消订单成功，orderId：{}", orderId);
            recordOperationLog("订单管理", "取消订单", 1, null);
            return HttpResult.ok("取消成功");
        } catch (Exception e) {
            log.error("取消订单失败：", e);
            recordOperationLog("订单管理", "取消订单", 0, e.getMessage());
            return HttpResult.error("取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 管理员主动退款
     * @param orderId 订单ID
     * @param reason 退款原因
     * @return 退款结果
     */
    @PostMapping("/admin/order/refund/{orderId}")
    @RequiresPermission(code = "order:manage")
    public HttpResult adminRefundOrder(@PathVariable Integer orderId,
                                       @RequestParam(required = false) String reason) {
        try {
            if (orderId == null || orderId <= 0) {
                recordOperationLog("订单管理", "管理员退款", 0, "订单ID无效");
                return HttpResult.error("订单ID无效");
            }
            
            Integer adminId = getCurrentUserId();
            if (adminId == null) {
                return HttpResult.error("管理员未登录");
            }
            
            RefundRecord refundRecord = refundService.adminCreateRefund(adminId, orderId, reason);
            log.info("管理员主动退款成功，orderId：{}，refundNo：{}", orderId, refundRecord.getRefundNo());
            recordOperationLog("订单管理", "管理员退款", 1, null);
            return HttpResult.ok("退款成功", refundRecord);
        } catch (Exception e) {
            log.error("管理员退款失败：", e);
            recordOperationLog("订单管理", "管理员退款", 0, e.getMessage());
            return HttpResult.error("退款失败：" + e.getMessage());
        }
    }

    /**
     * 获取待制作订单列表（制作中、待取餐）
     * @return 待制作订单列表
     */
    @GetMapping("/admin/order/pending")
    @RequiresPermission(code = "order:read")
    public HttpResult getPendingOrders() {
        try {
            List<Order> orders = orderService.getPendingOrders();
            List<OrderDetailVO> orderDetails = new ArrayList<>();
            
            for (Order order : orders) {
                OrderDetailVO vo = OrderDetailVO.fromOrder(order);
                List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
                vo.setOrderItems(orderItems);
                
                if (order.getAddressId() != null) {
                    Address address = addressMapper.selectById(order.getAddressId());
                    vo.setAddress(address);
                }
                
                orderDetails.add(vo);
            }
            
            log.info("查询待制作订单，数量：{}", orderDetails.size());
            recordOperationLog("订单管理", "查询待制作订单", 1, null);
            return HttpResult.ok(orderDetails);
        } catch (Exception e) {
            log.error("查询待制作订单失败：", e);
            recordOperationLog("订单管理", "查询待制作订单", 0, e.getMessage());
            return HttpResult.error("查询待制作订单失败：" + e.getMessage());
        }
    }

    /**
     * 快速重新下单
     * @param oldOrderId 历史订单ID
     * @return 新订单
     */
    @PostMapping("/reorder")
    public HttpResult reorder(@RequestParam Integer oldOrderId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                log.warn("快速重新下单失败：用户未登录");
                return HttpResult.error("用户未登录，请先登录");
            }

            // 基础参数校验
            if (oldOrderId == null || oldOrderId <= 0) {
                return HttpResult.error("历史订单ID无效，请传入正整数");
            }

            // 调用Service重新下单
            Order newOrder = orderService.reorder(userId, oldOrderId);
            log.info("用户{}快速重新下单成功，历史订单ID：{}，新订单ID：{}", userId, oldOrderId, newOrder.getId());
            return HttpResult.ok("重新下单成功", newOrder);
        } catch (Exception e) {
            log.error("快速重新下单失败：", e);
            return HttpResult.error("快速重新下单失败：" + e.getMessage());
        }
    }

    // /**
    //  * 验证取餐码
    //  * @param takeCode 取餐码
    //  * @return 验证结果
    //  */
    // @PostMapping("/validate-take-code")
    // public HttpResult validateTakeCode(@RequestParam String takeCode) {
    //     try {
    //         // 基础参数校验
    //         if (takeCode == null || takeCode.isEmpty()) {
    //             return HttpResult.error("取餐码不能为空");
    //         }

    //         // 调用Service验证取餐码
    //         Order order = orderService.validateTakeCode(takeCode);
    //         log.info("取餐码验证成功，takeCode：{}，orderId：{}", takeCode, order.getId());
    //         return HttpResult.ok("取餐码验证成功", order);
    //     } catch (Exception e) {
    //         log.error("取餐码验证失败：", e);
    //         return HttpResult.error("取餐码验证失败：" + e.getMessage());
    //     }
    // }

    /**
     * 用户取消订单（仅允许取消待付款订单）
     */
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "用户取消订单")
    public HttpResult cancelOrder(@PathVariable Integer orderId,
                                  @RequestParam(required = false) String reason) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录，请先登录");
            }
            if (orderId == null || orderId <= 0) {
                return HttpResult.error("订单ID无效");
            }
            orderService.cancelOrder(userId, orderId, reason);
            return HttpResult.ok("取消成功");
        } catch (Exception e) {
            log.error("取消订单失败：", e);
            return HttpResult.error("取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 用户确认收货/取餐
     */
    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "确认收货")
    public HttpResult confirmReceipt(@PathVariable Integer orderId) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录，请先登录");
            }
            if (orderId == null || orderId <= 0) {
                return HttpResult.error("订单ID无效");
            }
            orderService.confirmReceipt(userId, orderId);
            return HttpResult.ok("确认成功");
        } catch (Exception e) {
            log.error("确认收货失败：", e);
            return HttpResult.error("确认收货失败：" + e.getMessage());
        }
    }

    /**
     * 用户申请退款
     */
    @PostMapping("/refund/{orderId}")
    @Operation(summary = "申请退款")
    public HttpResult applyRefund(@PathVariable Integer orderId,
                                   @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录，请先登录");
            }
            if (orderId == null || orderId <= 0) {
                return HttpResult.error("订单ID无效");
            }
            RefundRecord refundRecord = refundService.applyRefund(userId, orderId, reason);
            return HttpResult.ok("退款申请已提交", refundRecord);
        } catch (Exception e) {
            log.error("申请退款失败：", e);
            return HttpResult.error("申请退款失败：" + e.getMessage());
        }
    }

    /**
     * 查询退款状态
     */
    @GetMapping("/refund/status/{orderId}")
    @Operation(summary = "查询退款状态")
    public HttpResult getRefundStatus(@PathVariable Integer orderId) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录，请先登录");
            }
            Order order = orderService.getOrderDetail(orderId);
            if (order == null) {
                return HttpResult.error("订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                return HttpResult.error("无权查看该订单");
            }
            Map<String, Object> refundStatus = refundService.queryRefundStatus(order.getOrderNo(), userId);
            return HttpResult.ok(refundStatus);
        } catch (Exception e) {
            log.error("查询退款状态失败：", e);
            return HttpResult.error("查询退款状态失败：" + e.getMessage());
        }
    }

    /**
     * 用户取消退款申请
     */
    @PostMapping("/refund/cancel/{refundId}")
    @Operation(summary = "取消退款申请")
    public HttpResult cancelRefund(@PathVariable Integer refundId) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录，请先登录");
            }
            RefundRecord refundRecord = refundService.cancelRefund(userId, refundId);
            return HttpResult.ok("退款已取消", refundRecord);
        } catch (Exception e) {
            log.error("取消退款失败：", e);
            return HttpResult.error("取消退款失败：" + e.getMessage());
        }
    }

    /**
     * 立即购买
     * @param dto 立即购买请求参数
     * @return 订单信息
     */
    @PostMapping("/buy-now")
    public HttpResult buyNow(@RequestBody com.example.demo.vo.BuyNowDTO dto) {
        try {
            Integer userId = getCurrentUserId();
            if (userId == null) {
                log.warn("立即购买失败：用户未登录");
                return HttpResult.error("用户未登录，请先登录");
            }

            if (dto.getProductId() == null || dto.getProductId() <= 0) {
                return HttpResult.error("商品ID无效，请传入正整数");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                return HttpResult.error("购买数量无效，请传入正整数");
            }
            if (dto.getOrderType() == null || (dto.getOrderType() != 1 && dto.getOrderType() != 2)) {
                return HttpResult.error("订单类型无效，仅支持1=堂食、2=外卖");
            }
            if (dto.getPaymentMethod() == null || (dto.getPaymentMethod() < 1 || dto.getPaymentMethod() > 3)) {
                return HttpResult.error("支付方式无效，仅支持1=微信、2=支付宝、3=现金");
            }
            if (dto.getOrderType() == 2 && (dto.getAddressId() == null || dto.getAddressId() <= 0)) {
                return HttpResult.error("外卖订单必须填写地址");
            }

            Order order = new Order();
            order.setOrderType(dto.getOrderType());
            order.setAddressId(dto.getAddressId());
            order.setPaymentMethod(dto.getPaymentMethod());
            order.setUserRemark(dto.getRemark());
            if (dto.getCouponId() != null && dto.getCouponId() > 0) {
                order.setCouponId(dto.getCouponId());
            }
            
            if (dto.getOrderType() == 1) {
                order.setDeliveryFee(BigDecimal.ZERO);
            } else if (dto.getDeliveryFee() != null) {
                order.setDeliveryFee(dto.getDeliveryFee());
            } else {
                order.setDeliveryFee(new BigDecimal("3.00"));
            }

            Order createdOrder = orderService.buyNow(userId, order, dto.getProductId(), dto.getQuantity(), dto.getSpecIds());
            log.info("立即购买成功，orderId：{}，userId：{}，productId：{}",
                    createdOrder.getId(), userId, dto.getProductId());
            return HttpResult.ok("立即购买成功", createdOrder);
        } catch (Exception e) {
            log.error("立即购买失败：", e);
            return HttpResult.error("立即购买失败：" + e.getMessage());
        }
    }

}
