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
}
