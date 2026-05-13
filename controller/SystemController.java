package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统配置接口（常见问题、版本检查等）
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Tag(name = "系统配置")
public class SystemController {

    @Value("${spring.application.name:}")
    private String appName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 常见问题列表
     */
    private static final List<Map<String, String>> FAQ_LIST = Arrays.asList(
            Map.of("id", "1", "question", "如何下单？", "answer", "选择心仪的商品，点击加入购物车，填写收货地址后完成支付即可下单。"),
            Map.of("id", "2", "question", "如何支付？", "answer", "我们支持微信支付和支付宝支付，选择您常用的支付方式即可。"),
            Map.of("id", "3", "question", "如何查看订单？", "answer", "在\"我的\"-\"我的订单\"中可以查看所有订单状态。"),
            Map.of("id", "4", "question", "如何取消订单？", "answer", "未支付的订单可以在订单详情中点击取消，已支付的订单请联系客服处理。"),
            Map.of("id", "5", "question", "优惠券如何使用？", "answer", "下单时在确认订单页面可以选择使用优惠券，系统会自动抵扣相应金额。"),
            Map.of("id", "6", "question", "配送范围是什么？", "answer", "具体配送范围请查看小程序首页的配送说明，或联系客服咨询。"),
            Map.of("id", "7", "question", "如何联系客服？", "answer", "可以在\"我的\"-\"联系客服\"中提交问题，我们会尽快回复您。")
    );

    /**
     * 获取常见问题列表
     * @return FAQ列表
     */
    @GetMapping("/faq")
    @Operation(summary = "获取常见问题")
    public HttpResult getFaqList() {
        return HttpResult.ok(FAQ_LIST);
    }

    /**
     * 获取版本信息
     * @return 版本信息
     */
    @GetMapping("/version")
    @Operation(summary = "获取版本信息")
    public HttpResult getVersion() {
        Map<String, Object> versionInfo = new HashMap<>();
        versionInfo.put("appName", appName);
        versionInfo.put("serverPort", serverPort);
        versionInfo.put("minVersion", "1.0.0");
        versionInfo.put("latestVersion", "1.0.0");
        versionInfo.put("updateTime", "2024-01-01");
        versionInfo.put("updateContent", "初始版本");
        versionInfo.put("forceUpdate", false);
        return HttpResult.ok(versionInfo);
    }

    /**
     * 获取系统配置
     * @param key 配置键
     * @return 配置值
     */
    @GetMapping("/config/{key}")
    @Operation(summary = "获取系统配置")
    public HttpResult getConfig(@PathVariable String key) {
        // 可以扩展为从数据库读取
        Map<String, String> configs = new HashMap<>();
        configs.put("shopName", "茶饮小程序");
        configs.put("shopPhone", "400-888-8888");
        configs.put("shopAddress", "示例地址");
        configs.put("businessHours", "9:00-21:00");
        configs.put("deliveryFee", "5");
        configs.put("freeDeliveryAmount", "50");

        String value = configs.get(key);
        if (value == null) {
            return HttpResult.error("配置不存在");
        }
        return HttpResult.ok(value);
    }

    /**
     * 获取所有系统配置
     * @return 所有配置
     */
    @GetMapping("/configs")
    @Operation(summary = "获取所有系统配置")
    public HttpResult getAllConfigs() {
        Map<String, String> configs = new HashMap<>();
        configs.put("shopName", "茶饮小程序");
        configs.put("shopPhone", "400-888-8888");
        configs.put("shopAddress", "示例地址");
        configs.put("businessHours", "9:00-21:00");
        configs.put("deliveryFee", "5");
        configs.put("freeDeliveryAmount", "50");
        return HttpResult.ok(configs);
    }
}
