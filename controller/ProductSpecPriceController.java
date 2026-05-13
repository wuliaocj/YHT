package com.example.demo.controller;

import com.example.demo.domain.OperationLog;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.http.HttpResult;
import com.example.demo.service.IdGeneratorService;
import com.example.demo.service.LogService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.vo.ProductSpecPriceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品规格加价表 前端控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/spec")
@Tag(name = "商品规格")
@RequiredArgsConstructor
@Validated
public class ProductSpecPriceController {

    private final ProductSpecPriceService productSpecPriceService;
    private final IdGeneratorService idGeneratorService;
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
            ProductSpecPriceController.log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    /**
     * 获取商品的规格列表（用户端）
     * @param productId 商品ID
     * @return 规格列表
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品规格")
    public HttpResult getProductSpecs(@PathVariable Long productId) {
        List<ProductSpecPrice> specs = productSpecPriceService.lambdaQuery()
                .eq(ProductSpecPrice::getProductId, productId)
                .orderByAsc(ProductSpecPrice::getSpecType)
                .list();
        return HttpResult.ok(specs);
    }

    /**
     * 获取商品的所有规格类型（用户端）
     * @param productId 商品ID
     * @return 规格类型列表
     */
    @GetMapping("/types/{productId}")
    @Operation(summary = "获取商品规格类型")
    public HttpResult getSpecTypes(@PathVariable Long productId) {
        List<ProductSpecPrice> specs = productSpecPriceService.lambdaQuery()
                .eq(ProductSpecPrice::getProductId, productId)
                .orderByAsc(ProductSpecPrice::getSpecType)
                .list();

        // 按规格类型分组
        List<String> specTypes = specs.stream()
                .map(ProductSpecPrice::getSpecType)
                .distinct()
                .toList();

        return HttpResult.ok(specTypes);
    }

    /**
     * 添加商品规格（管理员）
     * @param dto 商品规格信息
     * @return 操作结果
     */
    @PostMapping("/admin/add")
    @Operation(summary = "添加商品规格")
    public HttpResult add(@RequestBody @Validated ProductSpecPriceDTO dto) {
        try {
            ProductSpecPrice productSpecPrice = new ProductSpecPrice();
            productSpecPrice.setProductId(dto.getProductId());
            productSpecPrice.setSpecType(dto.getSpecType());
            productSpecPrice.setSpecName(dto.getSpecName());
            productSpecPrice.setPriceAdd(java.math.BigDecimal.valueOf(dto.getPrice()));

            Long id = idGeneratorService.generateSpecId(productSpecPrice.getProductId(), productSpecPrice.getSpecType());
            productSpecPrice.setId(id);

            LocalDateTime now = LocalDateTime.now();
            productSpecPrice.setCreateTime(now);
            productSpecPrice.setUpdateTime(now);

            boolean success = productSpecPriceService.save(productSpecPrice);
            if (success) {
                recordOperationLog("商品规格管理", "添加商品规格", 1, null);
                return HttpResult.ok("添加规格成功", productSpecPrice);
            } else {
                recordOperationLog("商品规格管理", "添加商品规格", 0, "保存失败");
                return HttpResult.error("添加规格失败");
            }
        } catch (Exception e) {
            recordOperationLog("商品规格管理", "添加商品规格", 0, e.getMessage());
            log.error("添加商品规格失败：", e);
            return HttpResult.error("添加规格失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品规格（管理员）
     * @param dto 商品规格信息
     * @return 操作结果
     */
    @PostMapping("/admin/update")
    @Operation(summary = "更新商品规格")
    public HttpResult update(@RequestBody @Validated ProductSpecPriceDTO dto) {
        try {
            ProductSpecPrice productSpecPrice = new ProductSpecPrice();
            productSpecPrice.setId(dto.getId());
            productSpecPrice.setProductId(dto.getProductId());
            productSpecPrice.setSpecType(dto.getSpecType());
            productSpecPrice.setSpecName(dto.getSpecName());
            productSpecPrice.setPriceAdd(java.math.BigDecimal.valueOf(dto.getPrice()));
            productSpecPrice.setUpdateTime(LocalDateTime.now());

            boolean success = productSpecPriceService.updateById(productSpecPrice);
            if (success) {
                recordOperationLog("商品规格管理", "更新商品规格", 1, null);
                return HttpResult.ok("更新规格成功");
            } else {
                recordOperationLog("商品规格管理", "更新商品规格", 0, "更新失败");
                return HttpResult.error("更新规格失败");
            }
        } catch (Exception e) {
            recordOperationLog("商品规格管理", "更新商品规格", 0, e.getMessage());
            log.error("更新商品规格失败：", e);
            return HttpResult.error("更新规格失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品规格（管理员）
     * @param id 规格ID
     * @return 操作结果
     */
    @PostMapping("/admin/delete/{id}")
    @Operation(summary = "删除商品规格")
    public HttpResult delete(@PathVariable Long id) {
        try {
            boolean success = productSpecPriceService.removeById(id);
            if (success) {
                recordOperationLog("商品规格管理", "删除商品规格", 1, null);
                return HttpResult.ok("删除规格成功");
            } else {
                recordOperationLog("商品规格管理", "删除商品规格", 0, "删除失败");
                return HttpResult.error("删除规格失败");
            }
        } catch (Exception e) {
            recordOperationLog("商品规格管理", "删除商品规格", 0, e.getMessage());
            log.error("删除商品规格失败：", e);
            return HttpResult.error("删除规格失败：" + e.getMessage());
        }
    }
}
