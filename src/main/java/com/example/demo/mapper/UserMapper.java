package com.example.demo.mapper;

import com.example.demo.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Integer id);

    User selectByOpenid(@Param("openid") String openid);

    int insert(User user);

    int update(User user);

    List<User> selectByIds(@Param("ids") List<Integer> ids);

    List<User> selectAll();
}


