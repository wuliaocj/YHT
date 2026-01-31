package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.ProductCollection;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ProductCollectionMapper;
import com.example.demo.service.ProductCollectionService;
import com.example.demo.service.ProductService;
import com.example.demo.vo.GetProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品收藏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCollectionServiceImpl extends ServiceImpl<ProductCollectionMapper, ProductCollection> implements ProductCollectionService {

    private final ProductCollectionMapper productCollectionMapper;
    private final ProductService productService;

    /**
     * 添加商品收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductCollection addCollection(Integer userId, Long productId) {
        try {
            // 验证参数
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }

            // 检查是否已收藏
            if (isCollected(userId, productId)) {
                throw new BusinessException("该商品已经收藏过了");
            }

            // 检查商品是否存在
            GetProductVO product = productService.getProductById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }

            // 创建收藏
            ProductCollection collection = new ProductCollection();
            collection.setUserId(userId);
            collection.setProductId(productId);
            collection.setCreateTime(LocalDateTime.now());

            // 保存收藏
            productCollectionMapper.insert(collection);
            log.info("用户{}添加商品收藏成功，商品ID：{}", userId, productId);

            return collection;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加商品收藏失败：", e);
            throw new BusinessException("添加商品收藏失败：" + e.getMessage());
        }
    }

    /**
     * 取消商品收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeCollection(Integer userId, Long productId) {
        try {
            // 验证参数
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }

            // 检查是否已收藏
            if (!isCollected(userId, productId)) {
                throw new BusinessException("该商品未收藏");
            }

            // 删除收藏
            int result = productCollectionMapper.deleteByUserIdAndProductId(userId, productId);
            if (result > 0) {
                log.info("用户{}取消商品收藏成功，商品ID：{}", userId, productId);
                return true;
            } else {
                log.warn("用户{}取消商品收藏失败，商品ID：{}", userId, productId);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消商品收藏失败：", e);
            throw new BusinessException("取消商品收藏失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户收藏商品列表
     */
    @Override
    public Map<String, Object> getUserCollections(Integer userId, Integer page, Integer pageSize) {
        try {
            // 验证参数
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 计算偏移量
            Integer offset = (page - 1) * pageSize;

            // 查询收藏列表
            List<ProductCollection> collections = productCollectionMapper.selectByUserId(userId, offset, pageSize);

            // 查询收藏总数
            Integer total = productCollectionMapper.countByUserId(userId);

            // 获取收藏的商品详情
            List<GetProductVO> productList = new ArrayList<>();
            if (!collections.isEmpty()) {
                List<Long> productIds = collections.stream()
                        .map(ProductCollection::getProductId)
                        .collect(Collectors.toList());

                for (Long productId : productIds) {
                    try {
                        GetProductVO product = productService.getProductById(productId);
                        if (product != null) {
                            productList.add(product);
                        }
                    } catch (Exception e) {
                        log.warn("获取商品详情失败，商品ID：{}", productId, e);
                    }
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("products", productList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (total + pageSize - 1) / pageSize);

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户收藏列表失败：", e);
            throw new BusinessException("获取用户收藏列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查商品是否已收藏
     */
    @Override
    public boolean isCollected(Integer userId, Long productId) {
        try {
            if (userId == null || productId == null) {
                return false;
            }

            ProductCollection collection = productCollectionMapper.selectByUserIdAndProductId(userId, productId);
            return collection != null;
        } catch (Exception e) {
            log.error("检查收藏状态失败：", e);
            throw new BusinessException("检查收藏状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户收藏总数
     */
    @Override
    public Integer getCollectionCount(Integer userId) {
        try {
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }

            return productCollectionMapper.countByUserId(userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取收藏总数失败：", e);
            throw new BusinessException("获取收藏总数失败：" + e.getMessage());
        }
    }
}
