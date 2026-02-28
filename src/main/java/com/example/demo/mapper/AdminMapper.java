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

    /**
     * 分页查询管理员
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 管理员列表
     */
    java.util.List<Admin> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);

    /**
     * 查询管理员总数
     * @param wrapper 查询条件
     * @return 管理员总数
     */
    long selectCount(@Param("wrapper") Object wrapper);
}
