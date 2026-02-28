package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.DeliveryFeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/delivery-fee")
@RequiredArgsConstructor
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;

    /**
     * 计算配送费
     * @param request 请求参数（distance, weight, orderAmount）
     * @return 配送费计算结果
     */
    @PostMapping("/calculate")
    public HttpResult calculateDeliveryFee(@RequestBody Map<String, Object> request) {
        try {
            double distance = request.get("distance") != null ? Double.parseDouble(request.get("distance").toString()) : 0;
            double weight = request.get("weight") != null ? Double.parseDouble(request.get("weight").toString()) : 0;
            BigDecimal orderAmount = request.get("orderAmount") != null ? new BigDecimal(request.get("orderAmount").toString()) : BigDecimal.ZERO;

            BigDecimal deliveryFee = deliveryFeeService.calculateDeliveryFee(distance, weight, orderAmount);
            log.debug("计算配送费成功，距离：{}公里，重量：{}kg，订单金额：{}，配送费：{}", distance, weight, orderAmount, deliveryFee);
            return HttpResult.ok("配送费计算成功", deliveryFee);
        } catch (Exception e) {
            log.error("计算配送费失败：{}", e.getMessage());
            return HttpResult.error("计算配送费失败：" + e.getMessage());
        }
    }

    /**
     * 获取免费配送阈值
     * @return 免费配送阈值
     */
    @GetMapping("/free-threshold")
    public HttpResult getFreeDeliveryThreshold() {
        try {
            BigDecimal threshold = deliveryFeeService.getFreeDeliveryThreshold();
            log.debug("获取免费配送阈值成功：{}", threshold);
            return HttpResult.ok("获取免费配送阈值成功", threshold);
        } catch (Exception e) {
            log.error("获取免费配送阈值失败：{}", e.getMessage());
            return HttpResult.error("获取免费配送阈值失败：" + e.getMessage());
        }
    }

    /**
     * 更新配送费规则
     * @param request 配送费规则参数
     * @return 更新结果
     */
    @PutMapping("/update-rule")
    public HttpResult updateDeliveryFeeRule(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal baseFee = request.get("baseFee") != null ? new BigDecimal(request.get("baseFee").toString()) : null;
            BigDecimal distanceFee = request.get("distanceFee") != null ? new BigDecimal(request.get("distanceFee").toString()) : null;
            BigDecimal weightFee = request.get("weightFee") != null ? new BigDecimal(request.get("weightFee").toString()) : null;
            BigDecimal freeThreshold = request.get("freeThreshold") != null ? new BigDecimal(request.get("freeThreshold").toString()) : null;

            deliveryFeeService.updateDeliveryFeeRule(baseFee, distanceFee, weightFee, freeThreshold);
            log.info("更新配送费规则成功");
            return HttpResult.ok("更新配送费规则成功");
        } catch (Exception e) {
            log.error("更新配送费规则失败：{}", e.getMessage());
            return HttpResult.error("更新配送费规则失败：" + e.getMessage());
        }
    }
}
