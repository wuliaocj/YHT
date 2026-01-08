package com.example.demo.mapper;

import com.example.demo.domain.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Category selectById(@Param("id") Integer id);

    List<Category> selectAll();

    int insert(Category category);

    int update(Category category);

    int delete(@Param("id") Integer id);
}


