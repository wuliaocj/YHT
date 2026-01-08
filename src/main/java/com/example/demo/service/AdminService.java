package com.example.demo.service;

import com.example.demo.domain.Admin;

public interface AdminService {

    Admin login(String username, String password);

    Admin getById(Integer id);

    Admin getByUsername(String username);

    Admin save(Admin admin);
}
