package com.example.demo.service.impl;

import com.example.demo.domain.Permission;
import com.example.demo.domain.RolePermission;
import com.example.demo.mapper.PermissionMapper;
import com.example.demo.mapper.RolePermissionMapper;
import com.example.demo.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper, RolePermissionMapper rolePermissionMapper) {
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public List<Permission> listAllPermissions() {
        return permissionMapper.selectAll();
    }

    @Override
    public Permission getById(Integer id) {
        return permissionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission save(Permission permission) {
        if (permission.getId() == null) {
            // 新增
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.insert(permission);
            log.info("新增权限成功，permissionId：{}，name：{}", permission.getId(), permission.getName());
        } else {
            // 更新
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.update(permission);
            log.info("更新权限成功，permissionId：{}，name：{}", permission.getId(), permission.getName());
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        permissionMapper.deleteById(id);
        log.info("删除权限成功，permissionId：{}", id);
    }

    @Override
    public List<Permission> getByRoleId(Integer roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Integer roleId, List<Integer> permissionIds) {
        // 先删除原有的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 批量添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (Integer permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            rolePermissionMapper.insertBatch(rolePermissions);
            log.info("分配权限成功，roleId：{}，权限数量：{}", roleId, permissionIds.size());
        } else {
            log.info("清空角色权限，roleId：{}", roleId);
        }
    }
}
