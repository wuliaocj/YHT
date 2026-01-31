package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.config.OrderStatusEnum;
import com.example.demo.config.PaymentStatusEnum;
import com.example.demo.websocket.OrderWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.domain.Cart;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.CouponService;
import com.example.demo.service.UserPointService;
import com.example.demo.util.SnowflakeIdGenerator;
import com.example.demo.vo.CreateOrderDTO;
import com.example.demo.vo.OrderItemDTO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    // 注入依赖（确保CartMapper、OrderItemMapper已正确配置）
    @Autowired
    private  CartMapper cartMapper;
    @Autowired
    private  OrderMapper orderMapper;
    @Autowired
    private  OrderItemMapper orderItemMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired
    private CouponService couponService;
    @Autowired
    private UserPointService userPointService;


    /**
     * 核心：创建订单（加事务保证原子性）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 任何异常都回滚
    public Order createOrder(Integer userId, Order order) {
        // 步骤1：查询用户已选中的购物车商品（is_selected=1）
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getIsSelected, 1); // 只结算选中的商品
        List<Cart> cartList = cartMapper.selectList(cartWrapper);

        // 校验：无选中商品则返回null
        if (cartList == null || cartList.isEmpty()) {
            return null;
        }

        // 步骤2：生成唯一订单号（规则：时间戳+随机数，确保不重复）
        String orderNo = generateOrderNo();

        // 步骤3：计算订单金额
        // 3.1 计算商品总价（购物车商品单价*数量求和）
        BigDecimal productTotal = cartList.stream()
                .map(cart -> cart.getUnitPrice().multiply(new BigDecimal(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3.2 补全订单金额相关字段（使用标准舍入模式）
        BigDecimal discountAmount = order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount();
        BigDecimal deliveryFee = order.getDeliveryFee() == null ? BigDecimal.ZERO : order.getDeliveryFee();
        
        // 3.3 应用优惠券（如果有）
        Integer couponId = order.getCouponId();
        if (couponId != null) {
            // 这里可以添加优惠券使用逻辑
            // couponService.useCoupon(userId, couponId, orderId);
        }
        
        // 实际支付金额 = 商品总价 + 配送费 - 优惠金额（最小为0）
        // 使用银行家舍入法（RoundingMode.HALF_EVEN）确保精度
        BigDecimal actualAmount = productTotal.add(deliveryFee).subtract(discountAmount);
        actualAmount = actualAmount.setScale(2, RoundingMode.HALF_EVEN);
        
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        // 步骤4：组装订单主表数据
        Order newOrder = new Order();
        newOrder.setOrderNo(orderNo);
        newOrder.setUserId(userId);
        newOrder.setTotalAmount(productTotal); // 商品总价
        newOrder.setDiscountAmount(discountAmount); // 优惠金额
        newOrder.setDeliveryFee(deliveryFee); // 配送费
        newOrder.setActualAmount(actualAmount); // 实际支付金额
        newOrder.setPaymentMethod(order.getPaymentMethod()); // 支付方式
        newOrder.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode()); // 默认未支付
        newOrder.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode()); // 默认待付款
        newOrder.setOrderType(order.getOrderType()); // 订单类型（堂食/外卖）
        newOrder.setUserRemark(order.getUserRemark()); // 用户备注
        newOrder.setCreateTime(LocalDateTime.now());
        newOrder.setUpdateTime(LocalDateTime.now());

        // 步骤5：插入订单主表
        orderMapper.insert(newOrder);
        Integer orderId = newOrder.getId(); // 获取自增的订单ID

        // 步骤6：遍历购物车，插入订单详情表
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(cart.getProductName());
            orderItem.setProductImage(cart.getProductImage());
            orderItem.setSpecInfo(cart.getSelectedSpecs()); // 规格信息（如"中杯/三分糖"）
            orderItem.setUnitPrice(cart.getUnitPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(cart.getUnitPrice().multiply(new BigDecimal(cart.getQuantity())));
            orderItem.setCreateTime(LocalDateTime.now());
            // 插入订单详情
            orderItemMapper.insert(orderItem);
        }

        // 步骤7：清空用户已选中的购物车商品（结算后移除）
        cartMapper.delete(cartWrapper);

        // 步骤8：返回创建成功的订单
        return newOrder;
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
        
        // 记录旧状态
        Integer oldStatus = order.getOrderStatus();
        
        // 更新状态
        order.setOrderStatus(status);
        if (adminRemark != null) {
            order.setAdminRemark(adminRemark);
        }
        if (status == 3) {
            order.setCompleteTime(LocalDateTime.now());
        }
        
        // 当订单状态变为支付成功时，生成取餐码并添加积分
        if (status == 2 && oldStatus != 2) {
            String takeCode = generateTakeCode();
            order.setTakeCode(takeCode);
            
            // 添加积分
            try {
                userPointService.addPointByOrder(order.getUserId(), orderId, order.getActualAmount());
            } catch (Exception e) {
                log.error("添加积分失败：", e);
                // 积分添加失败不影响订单状态更新
            }
            
            // 使用优惠券（如果有）
            Integer couponId = order.getCouponId();
            if (couponId != null) {
                try {
                    couponService.useCoupon(order.getUserId(), couponId, orderId);
                } catch (Exception e) {
                    log.error("使用优惠券失败：", e);
                    // 优惠券使用失败不影响订单状态更新
                }
            }
        }
        
        orderMapper.update(order);
        
        // 发送订单状态变更通知
        sendOrderStatusNotification(order.getUserId(), orderId, status, getStatusMessage(status));
    }

    /**
     * 发送订单状态通知
     */
    private void sendOrderStatusNotification(Integer userId, Integer orderId, Integer status, String message) {
        try {
            // 调用WebSocket处理器发送通知
            OrderWebSocketHandler.sendOrderNotification(userId, orderId, String.valueOf(status), message);
        } catch (Exception e) {
            log.error("发送订单状态通知失败：", e);
            // 通知发送失败不影响订单状态更新
        }
    }

    /**
     * 获取状态对应的消息
     */
    private String getStatusMessage(Integer status) {
        switch (status) {
            case 0:
                return "订单已取消";
            case 1:
                return "订单已创建，等待支付";
            case 2:
                return "订单已支付，正在处理";
            case 3:
                return "订单已完成";
            case 4:
                return "订单已退款";
            default:
                return "订单状态已更新";
        }
    }

    @Override
    public PageResponseVO<Order> listOrdersByPage(PageRequestVO pageRequest) {
        // 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加排序条件
        if (pageRequest.getOrderBy() != null && !pageRequest.getOrderBy().isEmpty()) {
            switch (pageRequest.getOrderBy()) {
                case "createTime":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Order::getCreateTime);
                    break;
                case "actualAmount":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Order::getActualAmount);
                    break;
                case "orderStatus":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Order::getOrderStatus);
                    break;
                default:
                    queryWrapper.orderByDesc(Order::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(Order::getCreateTime);
        }
        
        // 执行分页查询
        List<Order> records = orderMapper.selectList(queryWrapper);
        Long total = orderMapper.selectCount(queryWrapper);
        
        // 手动分页（如果使用MyBatis-Plus的分页插件，可以直接使用）
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getPageSize();
        
        List<Order> pagedRecords;
        if (offset >= records.size()) {
            pagedRecords = new java.util.ArrayList<>();
        } else {
            int endIndex = Math.min(offset + limit, records.size());
            pagedRecords = records.subList(offset, endIndex);
        }
        
        return PageResponseVO.of(pagedRecords, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    // 生成订单号（使用雪花算法）
    private String generateOrderNo() {
        return snowflakeIdGenerator.generateOrderNo("ORD");
    }

    // 生成取餐码
    private String generateTakeCode() {
        int code = new Random().nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    /**
     * 快速重新下单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order reorder(Integer userId, Integer oldOrderId) {
        // 步骤1：查询历史订单详情
        Order oldOrder = orderMapper.selectById(oldOrderId);
        if (oldOrder == null) {
            throw new RuntimeException("历史订单不存在");
        }

        // 验证订单所属权
        if (!oldOrder.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }

        // 步骤2：查询历史订单的商品列表
        List<OrderItem> oldOrderItems = orderItemMapper.selectByOrderId(oldOrderId);
        if (oldOrderItems == null || oldOrderItems.isEmpty()) {
            throw new RuntimeException("历史订单无商品");
        }

        // 步骤3：生成新订单号
        String newOrderNo = generateOrderNo();

        // 步骤4：创建新订单
        Order newOrder = new Order();
        newOrder.setOrderNo(newOrderNo);
        newOrder.setUserId(userId);
        newOrder.setTotalAmount(oldOrder.getTotalAmount());
        newOrder.setDiscountAmount(oldOrder.getDiscountAmount());
        newOrder.setDeliveryFee(oldOrder.getDeliveryFee());
        newOrder.setActualAmount(oldOrder.getActualAmount());
        newOrder.setPaymentMethod(oldOrder.getPaymentMethod());
        newOrder.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode());
        newOrder.setOrderStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        newOrder.setOrderType(oldOrder.getOrderType());
        newOrder.setUserRemark(oldOrder.getUserRemark());
        newOrder.setCreateTime(LocalDateTime.now());
        newOrder.setUpdateTime(LocalDateTime.now());

        // 步骤5：插入新订单
        orderMapper.insert(newOrder);
        Integer newOrderId = newOrder.getId();

        // 步骤6：复制历史订单的商品到新订单
        for (OrderItem oldItem : oldOrderItems) {
            OrderItem newItem = new OrderItem();
            newItem.setOrderId(newOrderId);
            newItem.setOrderNo(newOrderNo);
            newItem.setProductId(oldItem.getProductId());
            newItem.setProductName(oldItem.getProductName());
            newItem.setProductImage(oldItem.getProductImage());
            newItem.setSpecInfo(oldItem.getSpecInfo());
            newItem.setUnitPrice(oldItem.getUnitPrice());
            newItem.setQuantity(oldItem.getQuantity());
            newItem.setTotalPrice(oldItem.getTotalPrice());
            newItem.setCreateTime(LocalDateTime.now());
            orderItemMapper.insert(newItem);
        }

        // 步骤7：返回新创建的订单
        return newOrder;
    }

    /**
     * 验证取餐码
     */
    @Override
    public Order validateTakeCode(String takeCode) {
        if (takeCode == null || takeCode.isEmpty()) {
            throw new RuntimeException("取餐码不能为空");
        }
        
        // 根据取餐码查询订单
        Order order = orderMapper.selectByTakeCode(takeCode);
        if (order == null) {
            throw new RuntimeException("取餐码无效");
        }
        
        // 验证订单状态是否为已完成
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("订单尚未完成，请等待");
        }
        
        return order;
    }
}