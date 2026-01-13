package com.example.demo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.demo.domain.ProductSpecPrice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.util.List;

/**
 * 添加商品请求 VO
 */
@Data
public class AddProductVO {




    /**
     * 商品名称（必填）
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 分类 ID（必填，关联分类表）
     */
    @NotNull(message = "商品分类不能为空")
    private Integer categoryId;

    /**
     * 基础价格（必填，默认中杯无小料价格）
     */
    @NotNull(message = "基础价格不能为空")
    @PositiveOrZero(message = "基础价格不能为负数")
    private Double basePrice;

    /**
     * 原价
     */
    @NotNull(message = "基础价格不能为空")
    @PositiveOrZero(message = "基础价格不能为负数")
    private Double originalPrice;


    /**
     * 排序序号
     */
    private int sort_order = 0;

    /**
     * 商品主图（必填）
     */
    @NotBlank(message = "商品主图不能为空")
    private String mainImage;

    /**
     * 商品描述（可选）
     */
    private String description;

    private String detail;

    /**
     * 商品状态（1=启用，0=禁用，默认启用）
     */
    private Integer status = 1;


    private Integer isHot = 1;
    private Integer isNew = 1;
    private Integer isRecommend = 1;


    /**
     * 杯型规格列表（必填，至少包含一种杯型）
     */
    @NotNull(message = "杯型规格不能为空")
    private List<ProductSpecPrice> cupTypeList;

    /**
     * 小料规格列表（可选，无小料则传空列表）
     */
    private List<ProductSpecPrice> toppingList;


}
