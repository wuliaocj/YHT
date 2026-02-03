package com.example.demo.controller;

import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.service.IdGeneratorService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.util.HttpResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品规格加价表 前端控制器
 * </p>
 *
 * @author TYQ
 * @since 2026-01-12 15:57:18
 */
@RestController
@RequestMapping("/api/productSpecPrice")
public class ProductSpecPriceController  {

    @Resource
    private ProductSpecPriceService productSpecPriceService;

    @Resource
    private IdGeneratorService idGeneratorService;

    /**
     * 添加商品规格
     * @param productSpecPrice 商品规格信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public HttpResult add(@RequestBody ProductSpecPrice productSpecPrice) {
        // 1. 生成规格ID
        Long id = idGeneratorService.generateSpecId(productSpecPrice.getProductId(), productSpecPrice.getSpecType());
        productSpecPrice.setId(id);

        // 2. 设置时间
        LocalDateTime now = LocalDateTime.now();
        productSpecPrice.setCreateTime(now);
        productSpecPrice.setUpdateTime(now);

        // 3. 保存规格
        boolean success = productSpecPriceService.save(productSpecPrice);
        if (success) {
            return HttpResult.ok("添加规格成功", productSpecPrice);
        } else {
            return HttpResult.error("添加规格失败");
        }
    }
}
