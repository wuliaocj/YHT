package com.example.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 加入购物车请求VO
 * 接收前端提交的「用户+商品+规格+数量」参数
 */
@Data
@Schema(description = "加入购物车请求参数")
public class AddCartVO {

    /**
     * 用户ID（必填）
     * 用于隔离不同用户的购物车，防止越权操作
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    /**
     * 商品ID（必填）
     * 关联商品表，确定要加入购物车的商品
     */
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "10")
    private Integer productId;

    /**
     * 购买数量（必填，最小为1）
     * 避免用户传入0或负数数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量不能小于1")
    @Schema(description = "购买数量", example = "2")
    private Integer quantity;

    /**
     * 杯型规格ID（必填）
     * 奶茶核心规格，如大杯/中杯/小杯，关联规格表
     */
    @NotNull(message = "杯型规格不能为空")
    @Schema(description = "杯型规格ID", example = "20")
    private Long cupSpecId;

    /**
     * 口味规格ID（必填）
     * 如少糖/正常糖，默认全糖
     */
    @NotNull(message = "口味规格不能为空")
    @Schema(description = "口味规格ID", example = "[23,24]")
    private Long tasteSpecId;

    /**
     * 温度规格ID（必填）
     * 如去冰/常温，默认常温
     */
    @NotNull(message = "温度规格不能为空")
    @Schema(description = "温度规格ID", example = "[23,24]")
    private Long temperatureSpecId;

    /**
     * 小料规格ID列表（可选）
     * 如珍珠/椰果/布丁，支持多选，无则传空列表
     */
    @Schema(description = "小料规格ID列表", example = "[40,41]")
    private List<Long> toppingSpecIds;

    /**
     * 是否默认选中（可选，默认1=选中）
     * 前端可指定加入购物车后是否选中，便于后续结算
     */
    @Schema(description = "是否选中（1=选中，0=未选中）", example = "1")
    private Integer isSelected = 1;
}
