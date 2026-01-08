package com.example.demo.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格选项表
 */
@Data
public class SpecOption {

    private Integer id;
    private Integer productId;
    private String specType;
    private String specName;
    private String specValue;
    private BigDecimal extraPrice;
    private Integer isDefault;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}


