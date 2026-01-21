package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.config.OrderStatusEnum;
import com.example.demo.config.PaymentStatusEnum;
import com.example.demo.domain.Cart;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.OrderItemService;
import com.example.demo.service.OrderService;
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

    // 注入依赖（确保CartMapper、OrderItemMapper已正确配置）
    @Autowired
    private  CartMapper cartMapper;
    @Autowired
    private  OrderMapper orderMapper;
    @Autowired
    private  OrderItemMapper orderItemMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;


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
        order.setOrderStatus(status);
        if (adminRemark != null) {
            order.setAdminRemark(adminRemark);
        }
        if (status == 3) {
            order.setCompleteTime(LocalDateTime.now());
        }
        orderMapper.update(order);
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
    }    // 生成取餐码
    private String generateTakeCode() {
        int code = new Random().nextInt(9000) + 1000;
        return String.valueOf(code);
    }
}