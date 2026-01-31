package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    Permission selectById(@Param("id") Integer id);

    List<Permission> selectAll();

    List<Permission> selectByRoleId(@Param("roleId") Integer roleId);

    int insert(Permission permission);

    int update(Permission permission);

    int deleteById(@Param("id") Integer id);
}
