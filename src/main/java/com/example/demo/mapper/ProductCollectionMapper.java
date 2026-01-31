package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.ProductCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductCollectionMapper extends BaseMapper<ProductCollection> {

    /**
     * 根据用户ID查询收藏商品列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收藏商品列表
     */
    List<ProductCollection> selectByUserId(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据用户ID和商品ID查询收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 收藏信息
     */
    ProductCollection selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Long productId);

    /**
     * 根据用户ID查询收藏总数
     * @param userId 用户ID
     * @return 收藏总数
     */
    Integer countByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID和商品ID删除收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 删除结果
     */
    int deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Long productId);
}
