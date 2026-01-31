package com.example.demo.service.impl;

import com.example.demo.domain.Role;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<Role> listAllRoles() {
        return roleMapper.selectAll();
    }

    @Override
    public Role getById(Integer id) {
        return roleMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role save(Role role) {
        if (role.getId() == null) {
            // 新增
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.insert(role);
            log.info("新增角色成功，roleId：{}，name：{}", role.getId(), role.getName());
        } else {
            // 更新
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.update(role);
            log.info("更新角色成功，roleId：{}，name：{}", role.getId(), role.getName());
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Integer id) {
        roleMapper.deleteById(id);
        log.info("删除角色成功，roleId：{}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Integer status) {
        Role role = roleMapper.selectById(id);
        if (role != null) {
            role.setStatus(status);
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.update(role);
            log.info("更新角色状态成功，roleId：{}，status：{}", id, status);
        }
    }
}
