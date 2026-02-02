package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.Cart;
import com.example.demo.domain.Product;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.util.JsonUtil;
import com.example.demo.vo.AddCartVO;
import javax.annotation.Resource;

import com.example.demo.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private  CartMapper cartMapper;
    @Autowired
    private  ProductMapper productMapper;


    @Resource
    private ProductService productService;
    @Resource
    private ProductSpecPriceService productSpecPriceService;

    /**
     * 通过用户id获取购物车列表
     * @param userId
     * @return
     */
    @Override
    public List<Cart> listUserCart(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 添加商品到购物车
     * @param addCartVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddCartVO addToCart(AddCartVO addCartVO) {
        // 1. 基础参数获取与校验
        Integer userId = addCartVO.getUserId();
        Integer productId = addCartVO.getProductId();
        Integer quantity = addCartVO.getQuantity();
        List<Long> cupSpecIds = addCartVO.getCupSpecIds();
        List<Long> tasteSpecIds = addCartVO.getTasteSpecIds();
        List<Long> temperatureSpecIds = addCartVO.getTemperatureSpecIds();
        List<Long> toppingSpecIds = addCartVO.getToppingSpecIds();

        if (quantity < 1) {
            throw new BusinessException("购买数量不能小于1");
        }

        // 2. 校验商品有效性
        Product product = productService.getById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 3. 查询并校验杯型规格
        String cupSpecNames = "";
        List<ProductSpecPrice> cupSpecList = List.of();
        if (!CollectionUtils.isEmpty(cupSpecIds)) {
            cupSpecList = productSpecPriceService.listByIds(cupSpecIds);
            // 过滤有效杯型规格
            cupSpecList = cupSpecList.stream()
                    .filter(s -> "cup_type".equals(s.getSpecType()) && s.getStatus() == 1)
                    .collect(Collectors.toList());
            // 校验是否有无效杯型
            if (cupSpecList.size() != cupSpecIds.size()) {
                throw new BusinessException("部分杯型规格无效或已下架");
            }
            // 拼接杯型名称
            cupSpecNames = cupSpecList.stream()
                    .map(ProductSpecPrice::getSpecName)
                    .collect(Collectors.joining("+"));
        }

        // 4. 查询并校验口味规格
        String tasteSpecNames = "";
        List<ProductSpecPrice> tasteSpecList = List.of();
        if (!CollectionUtils.isEmpty(tasteSpecIds)) {
            tasteSpecList = productSpecPriceService.listByIds(tasteSpecIds);
            // 过滤有效口味规格
            tasteSpecList = tasteSpecList.stream()
                    .filter(s -> "taste".equals(s.getSpecType()) && s.getStatus() == 1)
                    .collect(Collectors.toList());
            // 校验是否有无效口味
            if (tasteSpecList.size() != tasteSpecIds.size()) {
                throw new BusinessException("部分口味规格无效或已下架");
            }
            // 拼接口味名称
            tasteSpecNames = tasteSpecList.stream()
                    .map(ProductSpecPrice::getSpecName)
                    .collect(Collectors.joining("+"));
        }

        // 5. 查询并校验温度规格
        String temperatureSpecNames = "";
        List<ProductSpecPrice> temperatureSpecList = List.of();
        if (!CollectionUtils.isEmpty(temperatureSpecIds)) {
            temperatureSpecList = productSpecPriceService.listByIds(temperatureSpecIds);
            // 过滤有效温度规格
            temperatureSpecList = temperatureSpecList.stream()
                    .filter(s -> "temperature".equals(s.getSpecType()) && s.getStatus() == 1)
                    .collect(Collectors.toList());
            // 校验是否有无效温度
            if (temperatureSpecList.size() != temperatureSpecIds.size()) {
                throw new BusinessException("部分温度规格无效或已下架");
            }
            // 拼接温度名称
            temperatureSpecNames = temperatureSpecList.stream()
                    .map(ProductSpecPrice::getSpecName)
                    .collect(Collectors.joining("+"));
        }

        // 6. 查询并校验小料规格
        String toppingSpecNames = "";
        List<ProductSpecPrice> toppingSpecList = List.of();
        if (!CollectionUtils.isEmpty(toppingSpecIds)) {
            toppingSpecList = productSpecPriceService.listByIds(toppingSpecIds);
            // 过滤有效小料规格
            toppingSpecList = toppingSpecList.stream()
                    .filter(s -> "topping".equals(s.getSpecType()) && s.getStatus() == 1)
                    .collect(Collectors.toList());
            // 校验是否有无效小料
            if (toppingSpecList.size() != toppingSpecIds.size()) {
                throw new BusinessException("部分小料规格无效或已下架");
            }
            // 拼接小料名称
            toppingSpecNames = toppingSpecList.stream()
                    .map(ProductSpecPrice::getSpecName)
                    .collect(Collectors.joining("+"));
        }

        // 7. 构建规格相关字段
        Map<String, Object> specMap = new LinkedHashMap<>();
        specMap.put("cupSpecIds", cupSpecIds == null ? new ArrayList<>() : cupSpecIds);
        specMap.put("tasteSpecIds", tasteSpecIds == null ? new ArrayList<>() : tasteSpecIds);
        specMap.put("temperatureSpecIds", temperatureSpecIds == null ? new ArrayList<>() : temperatureSpecIds);
        specMap.put("toppingSpecIds", toppingSpecIds == null ? new ArrayList<>() : toppingSpecIds);

        // 8. 序列化规格信息
        String specIds = JsonUtil.toJson(specMap);
        log.debug("最终生成的specIds：{}", specIds);

        // 9. 构建selectedSpecs
        StringBuilder selectedSpecsBuilder = new StringBuilder();
        if (!cupSpecNames.isEmpty()) {
            selectedSpecsBuilder.append(cupSpecNames);
        }
        if (!tasteSpecNames.isEmpty()) {
            if (selectedSpecsBuilder.length() > 0) {
                selectedSpecsBuilder.append("/");
            }
            selectedSpecsBuilder.append(tasteSpecNames);
        }
        if (!temperatureSpecNames.isEmpty()) {
            if (selectedSpecsBuilder.length() > 0) {
                selectedSpecsBuilder.append("/");
            }
            selectedSpecsBuilder.append(temperatureSpecNames);
        }
        if (!toppingSpecNames.isEmpty()) {
            if (selectedSpecsBuilder.length() > 0) {
                selectedSpecsBuilder.append("/");
            }
            selectedSpecsBuilder.append(toppingSpecNames);
        }
        String selectedSpecs = selectedSpecsBuilder.toString();

        // 10. 计算商品单价（基础价+杯型加价+口味加价+温度加价+小料加价）
        BigDecimal cupAdd = cupSpecList.stream()
                .map(ProductSpecPrice::getPriceAdd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tasteAdd = tasteSpecList.stream()
                .map(ProductSpecPrice::getPriceAdd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal temperatureAdd = temperatureSpecList.stream()
                .map(ProductSpecPrice::getPriceAdd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal toppingAdd = toppingSpecList.stream()
                .map(ProductSpecPrice::getPriceAdd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unitPrice = product.getBasePrice().add(cupAdd).add(tasteAdd).add(temperatureAdd).add(toppingAdd);

        // 8. 检查是否存在同商品+同规格的购物车记录
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        log.debug("查询参数：userId={}, productId={}, specIds={}", userId, productId, specIds);
        wrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(Cart::getSpecIds, specIds);
        Cart existCart = this.getOne(wrapper);
        log.debug("查询结果：{}", existCart);


        if (existCart != null) {
            // 已存在：更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            existCart.setUpdateTime(LocalDateTime.now());
            this.updateById(existCart);
        } else {
            // 不存在：新增购物车记录
            try {
                Cart cart = new Cart();
                cart.setUserId(userId);
                cart.setProductId(productId);
                cart.setProductName(product.getName());
                cart.setProductImage(product.getMainImage());
                log.debug("新增购物车记录，specIds：{}", specIds);
                cart.setSpecIds(specIds);
                cart.setQuantity(quantity);
                cart.setSelectedSpecs(selectedSpecs);
                cart.setUnitPrice(unitPrice);
                cart.setIsSelected(addCartVO.getIsSelected() == null ? 1 : addCartVO.getIsSelected());
                cart.setCreateTime(LocalDateTime.now());
                cart.setUpdateTime(LocalDateTime.now());

                cartMapper.insertCartWithSpecIds(cart);
                log.info("新增购物车成功，cartId：{}，specIds：{}", cart.getId(), cart.getSpecIds());

            } catch (DuplicateKeyException e) {
                // 捕获并发导致的重复插入异常，再次查询并更新
                Cart concurrentCart = this.getOne(wrapper);
                if (concurrentCart != null) {
                    concurrentCart.setQuantity(concurrentCart.getQuantity() + quantity);
                    concurrentCart.setUpdateTime(LocalDateTime.now());
                    this.updateById(concurrentCart);
                }
            }
        }
        return addCartVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItem(Integer cartId, Integer quantity, String selectedSpecs, Boolean selected) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            log.warn("购物车项不存在，cartId：{}", cartId);
            return;
        }
        cart.setQuantity(quantity);
        cart.setSelectedSpecs(selectedSpecs);
        if (selected != null) {
            cart.setIsSelected(selected ? 1 : 0);
        }
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.update(cart);
        log.info("更新购物车项成功，cartId：{}", cartId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Integer cartId) {
        cartMapper.deleteById(cartId);
        log.info("删除购物车项成功，cartId：{}", cartId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUserCart(Integer userId) {
        cartMapper.deleteByUserId(userId);
        log.info("清空用户购物车成功，userId：{}", userId);
    }
}


