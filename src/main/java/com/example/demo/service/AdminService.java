package com.example.demo.service;

import com.example.demo.domain.Admin;

import java.util.List;

public interface AdminService {

    Admin login(String username, String password);

    Admin getById(Integer id);

    Admin getByUsername(String username);

    Admin save(Admin admin);

    List<Admin> listAllAdmins();

    void deleteAdmin(Integer id);

    void updateAdminStatus(Integer id, Integer status);
}
