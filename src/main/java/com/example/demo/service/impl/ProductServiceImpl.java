package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.Product;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.mapper.*;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;
import com.example.demo.vo.ProductSpecVO;
import com.example.demo.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final ProductSpecPriceMapper productSpecPriceMapper;
    private final ProductSpecPriceService productSpecPriceService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addProduct(AddProductVO addProductVO) {
        try {
            // 保存商品
            Product product = new Product();
            product.setCategoryId(addProductVO.getCategoryId());
            product.setName(addProductVO.getName());
            product.setDescription(addProductVO.getDescription());
            product.setDetail(addProductVO.getDetail());
            product.setMainImage(addProductVO.getMainImage());
            product.setBasePrice(BigDecimal.valueOf(addProductVO.getBasePrice()));
            product.setOriginPrice(BigDecimal.valueOf(addProductVO.getOriginalPrice()));
            product.setIsHot(addProductVO.getIsHot());
            product.setIsNew(addProductVO.getIsNew());
            product.setIsRecommend(addProductVO.getIsRecommend());
            product.setStatus(addProductVO.getStatus());
            product.setSortOrder(addProductVO.getSort_order());
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            int insertResult = productMapper.insert(product);
            log.info("商品保存成功，商品ID：{}，插入结果：{}", product.getId(), insertResult);

            // 保存杯型
            List<ProductSpecPrice> cupTypeList = addProductVO.getCupTypeList();
            if (!CollectionUtils.isEmpty(cupTypeList)) {
                for (ProductSpecPrice productSpecPrice : cupTypeList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("cup_type");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存杯型规格数量：{}", cupTypeList.size());
            }
            // 保存小料
            List<ProductSpecPrice> toppingList = addProductVO.getToppingList();
            if (!CollectionUtils.isEmpty(toppingList)) {
                for (ProductSpecPrice productSpecPrice : toppingList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("topping");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存小料规格数量：{}", toppingList.size());
            }
            // 保存口味
            List<ProductSpecPrice> tasteList = addProductVO.getTasteList();
            if (!CollectionUtils.isEmpty(tasteList)) {
                for (ProductSpecPrice productSpecPrice : tasteList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("taste");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存口味规格数量：{}", tasteList.size());
            }
            // 保存温度
            List<ProductSpecPrice> temperatureList = addProductVO.getTemperatureList();
            if (!CollectionUtils.isEmpty(temperatureList)) {
                for (ProductSpecPrice productSpecPrice : temperatureList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("temperature");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存温度规格数量：{}", temperatureList.size());
            }
            return "保存成功";
        } catch (Exception e) {
            log.error("保存商品失败：", e);
            throw new BusinessException("保存商品失败：" + e.getMessage());
        }
    }

    /**
     * 根据商品ID查询商品详情（返回GetProductVO）
     */
    @Override
    public GetProductVO getProductById(Long id) {
        // 1. 查询商品基础信息
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 2. 查询杯型规格（type=cup_type，status=1）
        LambdaQueryWrapper<ProductSpecPrice> cupTypeWrapper = new LambdaQueryWrapper<>();
        cupTypeWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "cup_type")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> cupTypeSpecList = productSpecPriceService.list(cupTypeWrapper);
        List<ProductSpecVO> cupTypeList = cupTypeSpecList.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        // 3. 查询小料规格（type=topping，status=1）
        LambdaQueryWrapper<ProductSpecPrice> toppingWrapper = new LambdaQueryWrapper<>();
        toppingWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "topping")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> toppingSpecList = productSpecPriceService.list(toppingWrapper);
        List<ProductSpecVO> toppingList = toppingSpecList.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        // 4. 查询温度规格（type=temperature，status=1）
        LambdaQueryWrapper<ProductSpecPrice> temperatureWrapper = new LambdaQueryWrapper<>();
        temperatureWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "temperature")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> temperatureSpecList = productSpecPriceService.list(temperatureWrapper);
        List<ProductSpecVO> temperatureList = temperatureSpecList.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        // 5. 查询口味规格（type=taste，status=1）
        LambdaQueryWrapper<ProductSpecPrice> tasteWrapper = new LambdaQueryWrapper<>();
        tasteWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "taste")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> tasteSpecList = productSpecPriceService.list(tasteWrapper);
        List<ProductSpecVO> tasteList = tasteSpecList.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());


        // 4. 组装GetProductVO（复用buildProductVO方法）
        Map<String, List<ProductSpecPrice>> specsMap = Map.of(
                "cup_type", cupTypeSpecList,
                "topping", toppingSpecList,
                "temperature", temperatureSpecList,
                "taste", tasteSpecList
        );
        return buildProductVO(product, specsMap);
    }

    /**
     * 获取商品列表（优化：批量查询规格，避免N+1问题）
     */
    @Override
    public List<GetProductVO> getProductList() {
        List<Product> products = productMapper.selectAll();
        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 批量获取所有商品ID
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // 批量查询所有规格（一次性查询，避免N+1）
        LambdaQueryWrapper<ProductSpecPrice> specWrapper = new LambdaQueryWrapper<>();
        specWrapper.in(ProductSpecPrice::getProductId, productIds)
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> allSpecs = productSpecPriceService.list(specWrapper);

        // 按商品ID和规格类型分组
        Map<Long, Map<String, List<ProductSpecPrice>>> specsByProduct = allSpecs.stream()
                .collect(groupingBy(
                        ProductSpecPrice::getProductId,
                        groupingBy(ProductSpecPrice::getSpecType)
                ));

        // 转换为VO列表
        return products.stream()
                .map(product -> buildProductVO(product, specsByProduct.getOrDefault(product.getId(), Map.of())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateProduct(AddProductVO addProductVO) {
        try {
            // 验证商品ID
            Long productId = addProductVO.getProductId();
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }
            
            // 查询现有商品
            Product existingProduct = this.getById(productId);
            if (existingProduct == null) {
                throw new BusinessException(404, "商品不存在");
            }
            
            // 更新商品信息
            existingProduct.setCategoryId(addProductVO.getCategoryId());
            existingProduct.setName(addProductVO.getName());
            existingProduct.setDescription(addProductVO.getDescription());
            existingProduct.setDetail(addProductVO.getDetail());
            existingProduct.setMainImage(addProductVO.getMainImage());
            existingProduct.setBasePrice(BigDecimal.valueOf(addProductVO.getBasePrice()));
            existingProduct.setOriginPrice(BigDecimal.valueOf(addProductVO.getOriginalPrice()));
            existingProduct.setIsHot(addProductVO.getIsHot());
            existingProduct.setIsNew(addProductVO.getIsNew());
            existingProduct.setIsRecommend(addProductVO.getIsRecommend());
            existingProduct.setStatus(addProductVO.getStatus());
            existingProduct.setSortOrder(addProductVO.getSort_order());
            existingProduct.setUpdateTime(LocalDateTime.now());
            
            int updateResult = productMapper.update(existingProduct);
            log.info("商品更新成功，商品ID：{}，更新结果：{}", productId, updateResult);

            // 删除原有规格
            LambdaQueryWrapper<ProductSpecPrice> specWrapper = new LambdaQueryWrapper<>();
            specWrapper.eq(ProductSpecPrice::getProductId, productId);
            productSpecPriceService.remove(specWrapper);
            log.info("删除原有规格成功，商品ID：{}", productId);

            // 保存杯型规格
            List<ProductSpecPrice> cupTypeList = addProductVO.getCupTypeList();
            if (!CollectionUtils.isEmpty(cupTypeList)) {
                for (ProductSpecPrice productSpecPrice : cupTypeList) {
                    productSpecPrice.setProductId(productId);
                    productSpecPrice.setSpecType("cup_type");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存杯型规格数量：{}", cupTypeList.size());
            }
            
            // 保存口味规格
            List<ProductSpecPrice> tasteList = addProductVO.getTasteList();
            if (!CollectionUtils.isEmpty(tasteList)) {
                for (ProductSpecPrice productSpecPrice : tasteList) {
                    productSpecPrice.setProductId(productId);
                    productSpecPrice.setSpecType("taste");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存口味规格数量：{}", tasteList.size());
            }
            
            // 保存温度规格
            List<ProductSpecPrice> temperatureList = addProductVO.getTemperatureList();
            if (!CollectionUtils.isEmpty(temperatureList)) {
                for (ProductSpecPrice productSpecPrice : temperatureList) {
                    productSpecPrice.setProductId(productId);
                    productSpecPrice.setSpecType("temperature");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存温度规格数量：{}", temperatureList.size());
            }
            
            // 保存小料规格
            List<ProductSpecPrice> toppingList = addProductVO.getToppingList();
            if (!CollectionUtils.isEmpty(toppingList)) {
                for (ProductSpecPrice productSpecPrice : toppingList) {
                    productSpecPrice.setProductId(productId);
                    productSpecPrice.setSpecType("topping");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
                log.info("保存小料规格数量：{}", toppingList.size());
            }
            
            return "更新成功";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新商品失败：", e);
            throw new BusinessException("更新商品失败：" + e.getMessage());
        }
    }

    /**
     * 构建商品VO（复用逻辑）
     */
    private GetProductVO buildProductVO(Product product, Map<String, List<ProductSpecPrice>> specsMap) {
        GetProductVO vo = new GetProductVO();
        vo.setProductId(product.getId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setDetail(product.getDetail());
        vo.setMainImage(product.getMainImage());
        vo.setBasePrice(product.getBasePrice());
        vo.setOriginalPrice(product.getOriginPrice());
        vo.setSalesVolume(0); // 销量字段需根据订单表统计，此处先默认0
        vo.setIsHot(product.getIsHot());
        vo.setIsNew(product.getIsNew());
        vo.setIsRecommend(product.getIsRecommend());
        vo.setStatus(product.getStatus());
        vo.setSortOrder(product.getSortOrder());
        vo.setCreateTime(product.getCreateTime());

        // 转换杯型规格
        List<ProductSpecPrice> cupTypeSpecs = specsMap.getOrDefault("cup_type", List.of());
        List<ProductSpecVO> cupTypeList = cupTypeSpecs.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        vo.setCupTypeList(cupTypeList);

        // 转换小料规格
        List<ProductSpecPrice> toppingSpecs = specsMap.getOrDefault("topping", List.of());
        List<ProductSpecVO> toppingList = toppingSpecs.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        vo.setToppingList(toppingList);

        // 转换温度规格
        List<ProductSpecPrice> specsByProductId = specsMap.getOrDefault("temperature", List.of());
        List<ProductSpecVO> temperatureList = specsByProductId.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        vo.setTemperatureList(temperatureList);

        // 转换口味规格
        List<ProductSpecPrice> tasteSpecs = specsMap.getOrDefault("taste", List.of());
        List<ProductSpecVO> tasteList = tasteSpecs.stream()
                .map(this::convertToSpecVO)
                .collect(Collectors.toList());
        vo.setTasteList(tasteList);

        return vo;
    }

    /**
     * 删除商品（同时删除关联的规格）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        try {
            // 1. 检查商品是否存在
            Product product = this.getById(productId);
            if (product == null) {
                throw new BusinessException(404, "商品不存在");
            }

            // 2. 删除商品关联的规格
            LambdaQueryWrapper<ProductSpecPrice> specWrapper = new LambdaQueryWrapper<>();
            specWrapper.eq(ProductSpecPrice::getProductId, productId);
            productSpecPriceService.remove(specWrapper);
            log.info("删除商品规格成功，商品ID：{}", productId);

            // 3. 删除商品
            this.removeById(productId);
            log.info("删除商品成功，商品ID：{}", productId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除商品失败：", e);
            throw new BusinessException("删除商品失败：" + e.getMessage());
        }
    }

    /**
     * 搜索商品
     */
    @Override
    public List<GetProductVO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 搜索商品
        List<Product> products = productMapper.searchProducts(keyword);
        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 批量获取所有商品ID
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // 批量查询所有规格（一次性查询，避免N+1）
        LambdaQueryWrapper<ProductSpecPrice> specWrapper = new LambdaQueryWrapper<>();
        specWrapper.in(ProductSpecPrice::getProductId, productIds)
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> allSpecs = productSpecPriceService.list(specWrapper);

        // 按商品ID和规格类型分组
        Map<Long, Map<String, List<ProductSpecPrice>>> specsByProduct = allSpecs.stream()
                .collect(groupingBy(
                        ProductSpecPrice::getProductId,
                        groupingBy(ProductSpecPrice::getSpecType)
                ));

        // 转换为VO列表
        return products.stream()
                .map(product -> buildProductVO(product, specsByProduct.getOrDefault(product.getId(), Map.of())))
                .collect(Collectors.toList());
    }

    /**
     * 转换规格为VO
     */
    private ProductSpecVO convertToSpecVO(ProductSpecPrice spec) {
        ProductSpecVO vo = new ProductSpecVO();
        vo.setSpecId(spec.getId());
        vo.setProductId(spec.getProductId());
        vo.setSpecName(spec.getSpecName());
        vo.setPriceAdd(spec.getPriceAdd().doubleValue());
        vo.setStatus(spec.getStatus());
        return vo;
    }
}
