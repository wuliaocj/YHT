package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    @TableId(type = IdType.AUTO)
    private Integer id;                // 购物车ID（保留）
    private Integer userId;            // 用户ID
    private Integer productId;         // 商品ID
    private String productName;        // 新增：商品名称（冗余，避免改名后展示异常）
    private String productImage;       // 新增：商品图片（冗余，减少关联查询）

    // 重点检查这个字段的注解和类型！
    @TableField("spec_ids") // 必须和数据库字段名完全一致（区分大小写，MySQL默认小写）
    private String specIds; // 类型必须是String，不能是Map/Object

    private Integer quantity;          // 购买数量
    private String selectedSpecs;      // 选中的商品规格（如"大杯/少糖/去冰"）
    private BigDecimal unitPrice;      // 商品单价（新增：必须有单价才能计算总价）
    private Integer isSelected;        // 是否选中（1=选中，0=未选中，结算时只算选中的）
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间

    // 新增totalPrice属性（关键修复）
    private BigDecimal totalPrice; // 推荐用BigDecimal处理金额，避免精度丢失


}
