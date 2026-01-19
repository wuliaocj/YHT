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

        // 4. 组装GetProductVO（复用buildProductVO方法）
        Map<String, List<ProductSpecPrice>> specsMap = Map.of(
                "cup_type", cupTypeSpecList,
                "topping", toppingSpecList
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

        return vo;
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
