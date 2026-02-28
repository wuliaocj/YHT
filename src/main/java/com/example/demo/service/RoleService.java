package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import java.util.List;

public interface RoleService {

    List<Role> listAllRoles();

    Role getById(Integer id);

    Role save(Role role);

    void deleteById(Integer id);

    void updateStatus(Integer id, Integer status);

    /**
     * 分页获取角色列表
     * @param pageRequest 分页请求参数
     * @param keyword 搜索关键词
     * @return 分页响应
     */
    PageResponseVO<Role> getRoleListByPage(PageRequestVO pageRequest, String keyword);
}
