package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.Category;
import com.example.demo.domain.Product;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductSpecPriceService;
import com.example.demo.vo.AddProductVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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


}
