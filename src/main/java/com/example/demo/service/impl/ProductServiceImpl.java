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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 修复点：
 * 1. 注入 CategoryMapper 处理分类查询
 * 2. 每个查询方法内创建独立的 LambdaQueryWrapper（避免多线程冲突）
 * 3. 修复字段名（basePriceAmount → basePrice）
 * 4. 补充所有查询方法的正确逻辑
 */
@Service
@RequiredArgsConstructor // 构造器注入依赖（替代 @Autowired）
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final ProductSpecPriceMapper productSpecPriceMapper;


    @Autowired
    ProductSpecPriceService productSpecPriceService;


    @Override
    public String addProduct(AddProductVO addProductVO) {
        try {
            //保存商品
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
            System.out.println(productMapper.insert(product));

            //保存杯型
            List<ProductSpecPrice> cupTypeList = addProductVO.getCupTypeList();
            if (!CollectionUtils.isEmpty(cupTypeList)) {
                for (ProductSpecPrice productSpecPrice : cupTypeList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("cup_type");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
            }
            //保存小料
            List<ProductSpecPrice> toppingList = addProductVO.getToppingList();
            if (!CollectionUtils.isEmpty(toppingList)) {
                for (ProductSpecPrice productSpecPrice : toppingList) {
                    productSpecPrice.setProductId(product.getId());
                    productSpecPrice.setSpecType("topping");
                    productSpecPrice.setCreateTime(LocalDateTime.now());
                    productSpecPrice.setUpdateTime(LocalDateTime.now());
                    productSpecPriceMapper.insert(productSpecPrice);
                }
            }
            return "保存成功";
        }
        catch (Exception e) {
            return "保存失败"+e.getMessage();
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
            throw new RuntimeException("商品不存在");
        }

        // 2. 查询杯型规格（type=cup_type，status=1）
        LambdaQueryWrapper<ProductSpecPrice> cupTypeWrapper = new LambdaQueryWrapper<>();
        cupTypeWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "cup_type")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> cupTypeSpecList = productSpecPriceService.list(cupTypeWrapper);
        // 转换为规格子VO
        List<ProductSpecVO> cupTypeList = cupTypeSpecList.stream()
                .map(spec -> {
                    ProductSpecVO vo = new ProductSpecVO();
                    vo.setSpecId(spec.getId());
                    vo.setProductId(spec.getProductId());
                    vo.setSpecName(spec.getSpecName());
                    vo.setPriceAdd(spec.getPriceAdd().doubleValue()); // BigDecimal转Double
                    vo.setStatus(spec.getStatus());
                    return vo;
                })
                .collect(Collectors.toList());

        // 3. 查询小料规格（type=topping，status=1）
        LambdaQueryWrapper<ProductSpecPrice> toppingWrapper = new LambdaQueryWrapper<>();
        toppingWrapper.eq(ProductSpecPrice::getProductId, id)
                .eq(ProductSpecPrice::getSpecType, "topping")
                .eq(ProductSpecPrice::getStatus, 1);
        List<ProductSpecPrice> toppingSpecList = productSpecPriceService.list(toppingWrapper);
        // 转换为规格子VO
        List<ProductSpecVO> toppingList = toppingSpecList.stream()
                .map(spec -> {
                    ProductSpecVO vo = new ProductSpecVO();
                    vo.setSpecId(spec.getId());
                    vo.setProductId(spec.getProductId());
                    vo.setSpecName(spec.getSpecName());
                    vo.setPriceAdd(spec.getPriceAdd().doubleValue());
                    vo.setStatus(spec.getStatus());
                    return vo;
                })
                .collect(Collectors.toList());

        // 4. 组装GetProductVO
        GetProductVO getProductVO = new GetProductVO();
        getProductVO.setProductId(product.getId());
        getProductVO.setName(product.getName());
        getProductVO.setCupTypeList(cupTypeList);
        getProductVO.setToppingList(toppingList);
        getProductVO.setDescription(product.getDescription());
        getProductVO.setDetail(product.getDetail());
        getProductVO.setMainImage(product.getMainImage());
        getProductVO.setBasePrice(product.getBasePrice());
        getProductVO.setOriginalPrice(product.getOriginPrice());
        getProductVO.setSalesVolume(0); // 销量字段需根据订单表统计，此处先默认0
        getProductVO.setIsHot(product.getIsHot());
        getProductVO.setIsNew(product.getIsNew());
        getProductVO.setIsRecommend(product.getIsRecommend());
        getProductVO.setStatus(product.getStatus());
        getProductVO.setSortOrder(product.getSortOrder());
        getProductVO.setCreateTime(product.getCreateTime());

        return getProductVO;
    }

    @Override
    public List<GetProductVO> getProductList() {
        List<Product>products=productMapper.selectAll();
        List<GetProductVO> ProductVOList=new ArrayList<>();
        if (!CollectionUtils.isEmpty(products)) {
            for (Product product : products) {
                ProductVOList.add(getProductById(product.getId()));
            }
        }
        return ProductVOList;
    }
}
