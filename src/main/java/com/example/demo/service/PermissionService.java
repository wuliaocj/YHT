package com.example.demo.service;

import com.example.demo.domain.Permission;

import java.util.List;

public interface PermissionService {

    List<Permission> listAllPermissions();

    Permission getById(Integer id);

    Permission save(Permission permission);

    void deleteById(Integer id);

    List<Permission> getByRoleId(Integer roleId);

    void assignPermissions(Integer roleId, List<Integer> permissionIds);
}
