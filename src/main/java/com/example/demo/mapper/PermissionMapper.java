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

    /**
     * 分页查询权限
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 权限列表
     */
    List<Permission> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);

    /**
     * 查询权限总数
     * @param wrapper 查询条件
     * @return 权限总数
     */
    long selectCount(@Param("wrapper") Object wrapper);
}
