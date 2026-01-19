package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    Category selectById(@Param("id") Integer id);

    List<Category> selectAll();

    int insert(Category category);

    int update(Category category);

    int delete(@Param("id") Integer id);
}


