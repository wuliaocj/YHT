package com.example.demo.service;

import com.example.demo.domain.Promotion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PromotionService {

    List<Promotion> listAll();

    Promotion save(Promotion promotion);

    void delete(Integer id);
    
    // 扩展方法
    List<Promotion> listActivePromotions();
    
    Promotion getPromotionById(Integer id);
    
    Map<String, Object> applyPromotion(Integer productId, Integer quantity, BigDecimal price);
}
