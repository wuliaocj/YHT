package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.ProductSpecPrice;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 曜
* @description 针对表【product_spec_price(商品规格加价表)】的数据库操作Mapper
* @createDate 2026-01-12 16:04:19
* @Entity generator.domain.ProductSpecPrice
*/
@Mapper
public interface ProductSpecPriceMapper extends BaseMapper<ProductSpecPrice> {

}




