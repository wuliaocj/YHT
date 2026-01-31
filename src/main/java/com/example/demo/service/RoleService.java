package com.example.demo.service;

import com.example.demo.domain.Role;

import java.util.List;

public interface RoleService {

    List<Role> listAllRoles();

    Role getById(Integer id);

    Role save(Role role);

    void deleteById(Integer id);

    void updateStatus(Integer id, Integer status);
}
