package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.domain.Role;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.service.RoleService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
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

    @Override
    public PageResponseVO<Role> getRoleListByPage(PageRequestVO pageRequest, String keyword) {
        // 校验分页参数
        pageRequest.validate();

        // 构建查询条件
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加过滤条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> {
                wrapper.like(Role::getName, keyword)
                       .or()
                       .like(Role::getDescription, keyword);
            });
        }
        
        // 添加排序条件
        if (pageRequest.getOrderBy() != null && !pageRequest.getOrderBy().isEmpty()) {
            switch (pageRequest.getOrderBy()) {
                case "createTime":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Role::getCreateTime);
                    break;
                case "name":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Role::getName);
                    break;
                default:
                    queryWrapper.orderByDesc(Role::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(Role::getCreateTime);
        }
        
        // 计算总数
        long total = roleMapper.selectCount(queryWrapper);
        if (total == 0) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        // 分页查询
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getPageSize();
        List<Role> records = roleMapper.selectByPage(offset, limit, queryWrapper);
        
        if (records == null || records.isEmpty()) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        return PageResponseVO.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }
}
