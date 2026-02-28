package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    Role selectById(@Param("id") Integer id);

    List<Role> selectAll();

    int insert(Role role);

    int update(Role role);

    int deleteById(@Param("id") Integer id);

    /**
     * 分页查询角色
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 角色列表
     */
    List<Role> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);

    /**
     * 查询角色总数
     * @param wrapper 查询条件
     * @return 角色总数
     */
    long selectCount(@Param("wrapper") Object wrapper);
}
