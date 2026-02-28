package com.example.demo.service;

import com.example.demo.domain.Permission;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import java.util.List;

public interface PermissionService {

    List<Permission> listAllPermissions();

    Permission getById(Integer id);

    Permission save(Permission permission);

    void deleteById(Integer id);

    List<Permission> getByRoleId(Integer roleId);

    void assignPermissions(Integer roleId, List<Integer> permissionIds);

    /**
     * 分页获取权限列表
     * @param pageRequest 分页请求参数
     * @param keyword 搜索关键词
     * @return 分页响应
     */
    PageResponseVO<Permission> getPermissionListByPage(PageRequestVO pageRequest, String keyword);
}
