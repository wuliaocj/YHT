package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.WxMiniCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序工具接口
 */
@Slf4j
@RestController
@RequestMapping("/api/mini")
@Tag(name = "小程序工具")
@RequiredArgsConstructor
public class MiniProgramController {

    private final WxMiniCodeService wxMiniCodeService;

    /**
     * 生成商品小程序码
     * @param productId 商品ID
     * @return 小程序码（Base64图片）
     */
    @GetMapping("/code/product/{productId}")
    @Operation(summary = "生成商品小程序码")
    public HttpResult generateProductCode(@PathVariable Long productId) {
        try {
            String code = wxMiniCodeService.generateProductCode(productId);
            if (code == null) {
                return HttpResult.error("生成小程序码失败");
            }
            return HttpResult.ok(code);
        } catch (Exception e) {
            log.error("生成商品小程序码失败：", e);
            return HttpResult.error("生成小程序码失败");
        }
    }

    /**
     * 生成订单小程序码
     * @param orderId 订单ID
     * @return 小程序码（Base64图片）
     */
    @GetMapping("/code/order/{orderId}")
    @Operation(summary = "生成订单小程序码")
    public HttpResult generateOrderCode(@PathVariable Long orderId) {
        try {
            String code = wxMiniCodeService.generateOrderCode(orderId);
            if (code == null) {
                return HttpResult.error("生成小程序码失败");
            }
            return HttpResult.ok(code);
        } catch (Exception e) {
            log.error("生成订单小程序码失败：", e);
            return HttpResult.error("生成小程序码失败");
        }
    }

    /**
     * 生成自定义小程序码
     * @param scene 场景参数
     * @param page 页面路径
     * @return 小程序码（Base64图片）
     */
    @GetMapping("/code/custom")
    @Operation(summary = "生成自定义小程序码")
    public HttpResult generateCustomCode(
            @RequestParam String scene,
            @RequestParam(defaultValue = "") String page) {
        try {
            String code = wxMiniCodeService.generateMiniCode(scene, page);
            if (code == null) {
                return HttpResult.error("生成小程序码失败");
            }
            return HttpResult.ok(code);
        } catch (Exception e) {
            log.error("生成自定义小程序码失败：", e);
            return HttpResult.error("生成小程序码失败");
        }
    }
}
