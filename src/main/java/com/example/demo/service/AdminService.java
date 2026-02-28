package com.example.demo.service;

import com.example.demo.domain.Admin;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import java.util.List;

public interface AdminService {

    Admin login(String username, String password);

    Admin getById(Integer id);

    Admin getByUsername(String username);

    Admin save(Admin admin);

    List<Admin> listAllAdmins();

    void deleteAdmin(Integer id);

    void updateAdminStatus(Integer id, Integer status);

    /**
     * 分页获取管理员列表
     * @param pageRequest 分页请求参数
     * @param keyword 搜索关键词
     * @return 分页响应
     */
    PageResponseVO<Admin> getAdminListByPage(PageRequestVO pageRequest, String keyword);
}
