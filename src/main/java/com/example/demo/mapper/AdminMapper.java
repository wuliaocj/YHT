package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    Admin selectById(@Param("id") Integer id);

    Admin selectByUsername(@Param("username") String username);

    int insert(Admin admin);

    int update(Admin admin);
}
