package com.example.demo.mapper;

import com.example.demo.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") Integer id);

    List<Product> selectByCategoryId(@Param("categoryId") Integer categoryId);

    List<Product> selectHotProducts();

    List<Product> selectAll();

    int insert(Product product);

    int update(Product product);

    int delete(@Param("id") Integer id);
}


