package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车表
 */
@Data
@TableName("Cart")
public class Cart {

    private Integer id;                // 购物车ID
    private Integer userId;            // 用户ID
    private Integer productId;         // 商品ID
    private Integer quantity;          // 购买数量
    private String selectedSpecs;      // 选中的商品规格（如"大杯/少糖/去冰"）
    private BigDecimal unitPrice;      // 商品单价（新增：必须有单价才能计算总价）
    private Integer isSelected;        // 是否选中（1=选中，0=未选中，结算时只算选中的）
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间

    /**
     * 计算该购物车项的总价（单价 × 数量）
     * 替代原有的totalPrice字段，避免冗余存储
     * @return 商品总价
     */
    public BigDecimal getTotalPrice() {
        // 空值校验：防止单价/数量为null导致空指针
        if (unitPrice == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        // 单价 × 数量 = 总价
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    public void setTotalPrice(BigDecimal multiply) {
        if (unitPrice == null || quantity == null || quantity <= 0) {
            this.unitPrice = multiply;
        }
    }

    // 【可选】如果需要保留totalPrice字段（不推荐，冗余），可改为：
    /*
    private BigDecimal totalPrice; // 冗余字段，建议删除
    public void setTotalPrice() {
        this.totalPrice = getTotalPrice(); // 调用上面的计算方法赋值
    }
    */
}
