package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.ProductSpecPrice;
import com.example.demo.mapper.ProductSpecPriceMapper;
import com.example.demo.service.ProductSpecPriceService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author 曜
* @description 针对表【product_spec_price(商品规格加价表)】的数据库操作Service实现
* @createDate 2026-01-12 16:04:19
*/
@Service
public class ProductSpecPriceServiceImpl extends ServiceImpl<ProductSpecPriceMapper, ProductSpecPrice>
    implements ProductSpecPriceService {

}




