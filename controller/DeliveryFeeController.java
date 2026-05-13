package com.example.demo.controller;

import com.example.demo.domain.OperationLog;
import com.example.demo.http.HttpResult;
import com.example.demo.service.DeliveryFeeService;
import com.example.demo.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/delivery-fee")
@RequiredArgsConstructor
@Validated
public class DeliveryFeeController {

    private final DeliveryFeeService deliveryFeeService;
    private final LogService logService;

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);
            logService.recordOperationLog(operationLog);
        } catch (Exception e) {
            DeliveryFeeController.log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    /**
     * 获取配送费配置
     * @return 配送费配置（deliveryFee: 配送费, maxDistance: 最远配送距离）
     */
    @GetMapping("/config")
    public HttpResult getConfig() {
        try {
            BigDecimal deliveryFee = deliveryFeeService.getDeliveryFee();
            BigDecimal maxDistance = deliveryFeeService.getMaxDeliveryDistance();
            log.info("获取配送费配置成功，配送费：{}，最远距离：{}", deliveryFee, maxDistance);
            return HttpResult.ok(Map.of(
                "deliveryFee", deliveryFee,
                "maxDistance", maxDistance
            ));
        } catch (Exception e) {
            log.error("获取配送费配置失败：{}", e.getMessage());
            return HttpResult.error("获取配送费配置失败：" + e.getMessage());
        }
    }

    /**
     * 更新配送费配置
     * @param request 配置参数（deliveryFee: 配送费, maxDistance: 最远配送距离）
     * @return 更新结果
     */
    @PutMapping("/config")
    public HttpResult updateConfig(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal deliveryFee = new BigDecimal(request.get("deliveryFee").toString());
            BigDecimal maxDistance = new BigDecimal(request.get("maxDistance").toString());
            deliveryFeeService.updateConfig(deliveryFee, maxDistance);
            log.info("更新配送费配置成功，配送费：{}，最远距离：{}", deliveryFee, maxDistance);
            recordOperationLog("配送费管理", "更新配送费配置", 1, null);
            return HttpResult.ok("更新配送费配置成功");
        } catch (Exception e) {
            recordOperationLog("配送费管理", "更新配送费配置", 0, e.getMessage());
            log.error("更新配送费配置失败：{}", e.getMessage());
            return HttpResult.error("更新配送费配置失败：" + e.getMessage());
        }
    }
}
