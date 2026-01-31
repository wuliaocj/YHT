package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {

    /**
     * 根据商品ID查询评价列表
     * @param productId 商品ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评价列表
     */
    List<ProductReview> selectByProductId(@Param("productId") Long productId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据商品ID查询评价总数
     * @param productId 商品ID
     * @return 评价总数
     */
    Integer countByProductId(@Param("productId") Long productId);

    /**
     * 根据商品ID查询平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * 根据用户ID查询评价列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 评价列表
     */
    List<ProductReview> selectByUserId(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据订单ID和商品ID查询评价
     * @param orderId 订单ID
     * @param productId 商品ID
     * @return 评价信息
     */
    ProductReview selectByOrderIdAndProductId(@Param("orderId") Integer orderId, @Param("productId") Long productId);
}
