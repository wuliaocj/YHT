package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.domain.Admin;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.service.AdminService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public Admin login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        // 验证密码（这里使用BCrypt加密，如果数据库存储的是明文，需要先修改）
        // 如果数据库密码是明文，可以先用BCrypt加密一次，然后后续都用BCrypt验证
        // 这里假设密码是明文存储，直接比较（生产环境应该使用BCrypt）
        if (!admin.getPassword().equals(password)) {
            // 如果使用BCrypt，应该这样验证：
            // if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 更新最后登录时间
        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.update(admin);
        return admin;
    }

    @Override
    public Admin getById(Integer id) {
        return adminMapper.selectById(id);
    }

    @Override
    public Admin getByUsername(String username) {
        return adminMapper.selectByUsername(username);
    }

    @Override
    public Admin save(Admin admin) {
        if (admin.getId() == null) {
            // 新增
            if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
                // 如果使用BCrypt加密密码
                // admin.setPassword(BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt()));
            }
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            adminMapper.insert(admin);
        } else {
            // 更新
            admin.setUpdateTime(LocalDateTime.now());
            adminMapper.update(admin);
        }
        return admin;
    }

    @Override
    public List<Admin> listAllAdmins() {
        return adminMapper.selectList(null);
    }

    @Override
    public void deleteAdmin(Integer id) {
        adminMapper.deleteById(id);
    }

    @Override
    public void updateAdminStatus(Integer id, Integer status) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setStatus(status);
        admin.setUpdateTime(LocalDateTime.now());
        adminMapper.update(admin);
    }

    @Override
    public PageResponseVO<Admin> getAdminListByPage(PageRequestVO pageRequest, String keyword) {
        // 校验分页参数
        pageRequest.validate();

        // 构建查询条件
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加过滤条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> {
                wrapper.like(Admin::getUsername, keyword)
                       .or()
                       .like(Admin::getRealName, keyword)
                       .or()
                       .like(Admin::getPhone, keyword);
            });
        }
        
        // 添加排序条件
        if (pageRequest.getOrderBy() != null && !pageRequest.getOrderBy().isEmpty()) {
            switch (pageRequest.getOrderBy()) {
                case "createTime":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Admin::getCreateTime);
                    break;
                case "username":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), Admin::getUsername);
                    break;
                default:
                    queryWrapper.orderByDesc(Admin::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(Admin::getCreateTime);
        }
        
        // 计算总数
        long total = adminMapper.selectCount(queryWrapper);
        if (total == 0) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        // 分页查询
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getPageSize();
        List<Admin> records = adminMapper.selectByPage(offset, limit, queryWrapper);
        
        if (records == null || records.isEmpty()) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        return PageResponseVO.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }
}
