package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    Product selectById(@Param("id") Integer id);

    List<Product> selectByCategoryId(@Param("categoryId") Integer categoryId);

    List<Product> selectHotProducts();

    List<Product> selectAll();

    int insert(Product product);

    int update(Product product);

    int delete(@Param("id") Integer id);

    List<Product> selectNewProducts();

    /**
     * 搜索商品
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * 分页查询商品
     * @param offset 偏移量
     * @param limit 限制数量
     * @param wrapper 查询条件
     * @return 商品列表
     */
    List<Product> selectByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("wrapper") Object wrapper);
}


