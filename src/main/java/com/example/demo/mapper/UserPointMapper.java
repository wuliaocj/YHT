package com.example.demo.mapper;

import com.example.demo.domain.UserPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPointMapper {

    UserPoint selectById(@Param("id") Integer id);

    List<UserPoint> selectByUserId(@Param("userId") Integer userId);

    Integer selectUserPointBalance(@Param("userId") Integer userId);

    int insert(UserPoint userPoint);

    int update(UserPoint userPoint);

    int delete(@Param("id") Integer id);
}
