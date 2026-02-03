package com.example.demo.controller;

import com.example.demo.service.IdGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试控制器
 * 用于测试ID生成服务
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Resource
    private IdGeneratorService idGeneratorService;

    /**
     * 测试生成规格ID
     * @param productId 产品ID
     * @param specType 规格类型
     * @return 生成的规格ID
     */
    @GetMapping("/generateSpecId/{productId}/{specType}")
    public Long generateSpecId(@PathVariable Long productId, @PathVariable String specType) {
        return idGeneratorService.generateSpecId(productId, specType);
    }
}
