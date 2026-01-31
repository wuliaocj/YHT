package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.ProductCollection;
import com.example.demo.vo.GetProductVO;

import java.util.List;
import java.util.Map;

public interface ProductCollectionService extends IService<ProductCollection> {

    /**
     * 添加商品收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 收藏信息
     */
    ProductCollection addCollection(Integer userId, Long productId);

    /**
     * 取消商品收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean removeCollection(Integer userId, Long productId);

    /**
     * 获取用户收藏商品列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 收藏商品列表和总数
     */
    Map<String, Object> getUserCollections(Integer userId, Integer page, Integer pageSize);

    /**
     * 检查商品是否已收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 是否已收藏
     */
    boolean isCollected(Integer userId, Long productId);

    /**
     * 获取用户收藏总数
     * @param userId 用户ID
     * @return 收藏总数
     */
    Integer getCollectionCount(Integer userId);
}
