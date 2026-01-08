package com.example.demo.service.impl;

import com.example.demo.domain.Admin;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.service.AdminService;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
            adminMapper.insert(admin);
        } else {
            // 更新
            adminMapper.update(admin);
        }
        return admin;
    }
}
