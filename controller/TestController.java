package com.example.demo.controller;

import com.example.demo.domain.Admin;
import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.mapper.PermissionMapper;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.service.IdGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 * 用于测试ID生成服务
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Resource
    private IdGeneratorService idGeneratorService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private AdminMapper adminMapper;

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
