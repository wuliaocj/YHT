package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectById(@Param("id") Integer id);

    User selectByOpenid(@Param("openid") String openid);

    int insert(User user);

    int update(User user);

    List<User> selectByIds(@Param("ids") List<Integer> ids);

    List<User> selectAll();

    /**
     * 分页查询用户
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 用户列表
     */
    List<User> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);

    /**
     * 查询用户总数
     * @param wrapper 查询条件
     * @return 用户总数
     */
    long selectCount(@Param("wrapper") Object wrapper);
}


