package com.example.demo.controller;

import com.example.demo.annotation.RequiresPermission;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.RefundRecord;
import com.example.demo.http.HttpResult;
import com.example.demo.service.LogService;
import com.example.demo.service.RefundService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/refund")
@RequiredArgsConstructor
@Tag(name = "退款管理")
public class RefundAdminController {

    private final RefundService refundService;
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
            RefundAdminController.log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "退款列表")
    public HttpResult list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "退款状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付方式") @RequestParam(required = false) Integer paymentMethod) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);

            var refundPage = refundService.listRefunds(pageRequest, status, paymentMethod);

            PageResponseVO<RefundRecord> response = new PageResponseVO<>();
            response.setRecords(refundPage.getRecords());
            response.setTotal(refundPage.getTotal());
            response.setPageNum((int) refundPage.getCurrent());
            response.setPageSize((int) refundPage.getSize());

            recordOperationLog("退款管理", "查询退款列表", 1, null);
            return HttpResult.ok(response);
        } catch (Exception e) {
            recordOperationLog("退款管理", "查询退款列表", 0, e.getMessage());
            log.error("查询退款列表失败：", e);
            return HttpResult.error("查询退款列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{refundId}")
    @Operation(summary = "退款详情")
    public HttpResult detail(@PathVariable Integer refundId) {
        try {
            RefundRecord refundRecord = refundService.getRefundById(refundId);
            if (refundRecord == null) {
                recordOperationLog("退款管理", "查询退款详情", 0, "退款记录不存在");
                return HttpResult.error("退款记录不存在");
            }
            recordOperationLog("退款管理", "查询退款详情", 1, null);
            return HttpResult.ok(refundRecord);
        } catch (Exception e) {
            recordOperationLog("退款管理", "查询退款详情", 0, e.getMessage());
            log.error("查询退款详情失败：", e);
            return HttpResult.error("查询退款详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/audit")
    @Operation(summary = "审核退款")
    public HttpResult audit(@RequestBody Map<String, Object> request) {
        try {
            Integer refundId = (Integer) request.get("refundId");
            Integer adminId = (Integer) request.get("adminId");
            Boolean approved = (Boolean) request.get("approved");
            String reason = (String) request.get("reason");

            if (refundId == null) {
                recordOperationLog("退款管理", "审核退款", 0, "退款ID不能为空");
                return HttpResult.error("退款ID不能为空");
            }
            if (approved == null) {
                recordOperationLog("退款管理", "审核退款", 0, "审核结果不能为空");
                return HttpResult.error("审核结果不能为空");
            }

            Map<String, Object> result = refundService.auditRefund(refundId, adminId, approved, reason);
            recordOperationLog("退款管理", "审核退款", 1, null);
            return HttpResult.ok("审核成功", result);
        } catch (Exception e) {
            recordOperationLog("退款管理", "审核退款", 0, e.getMessage());
            log.error("审核退款失败：", e);
            return HttpResult.error("审核退款失败：" + e.getMessage());
        }
    }
}
