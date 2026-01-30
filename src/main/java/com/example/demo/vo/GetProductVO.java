package com.example.demo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询商品返回VO
 */
@Data
public class GetProductVO {
    /** 商品ID */
    private Long productId;
    /** 商品名 */
    private String name;
    /** 杯型规格列表（type=cup_type，状态=1） */
    private List<ProductSpecVO> cupTypeList;
    /** 小料规格列表（type=topping，状态=1） */
    private List<ProductSpecVO> toppingList;
    /** 温度规格列表（type=temperature，状态=1） */
    private List<ProductSpecVO> temperatureList;
    /** 口味规格列表（type=taste，状态=1） */
    private List<ProductSpecVO> tasteList;
    /** 商品描述 */
    private String description;
    /** 商品详情 */
    private String detail;
    /** 主图（数据库字段main_image，驼峰mainImage） */
    private String mainImage;
    /** 基础价格 */
    private BigDecimal basePrice;
    /** 原价 */
    private BigDecimal originalPrice;
    /** 销量 */
    private Integer salesVolume;
    /** 是否热门（1=是，0=否） */
    private Integer isHot;
    /** 是否新品（1=是，0=否） */
    private Integer isNew;
    /** 是否推荐（1=是，0=否） */
    private Integer isRecommend;
    /** 商品状态（1=启用，0=禁用） */
    private Integer status;
    /** 序列号（排序值，对应sort_order） */
    private Integer sortOrder;
    /** 创建时间 */
    private LocalDateTime createTime;
}
