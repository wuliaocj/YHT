package com.example.demo.service.impl;

import com.example.demo.domain.Promotion;
import com.example.demo.mapper.PromotionMapper;
import com.example.demo.service.PromotionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionMapper promotionMapper;

    public PromotionServiceImpl(PromotionMapper promotionMapper) {
        this.promotionMapper = promotionMapper;
    }

    @Override
    public List<Promotion> listAll() {
        return promotionMapper.selectAll();
    }

    @Override
    public Promotion save(Promotion promotion) {
        if (promotion.getId() == null) {
            promotionMapper.insert(promotion);
        } else {
            promotionMapper.update(promotion);
        }
        return promotion;
    }

    @Override
    public void delete(Integer id) {
        promotionMapper.delete(id);
    }

    @Override
    public List<Promotion> listActivePromotions() {
        List<Promotion> allPromotions = promotionMapper.selectAll();
        List<Promotion> activePromotions = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        for (Promotion promotion : allPromotions) {
            if (promotion.getStatus() == 1 && 
                promotion.getStartTime().isBefore(now) && 
                promotion.getEndTime().isAfter(now)) {
                activePromotions.add(promotion);
            }
        }
        
        return activePromotions;
    }

    @Override
    public Promotion getPromotionById(Integer id) {
        return promotionMapper.selectById(id);
    }

    @Override
    public Map<String, Object> applyPromotion(Integer productId, Integer quantity, BigDecimal price) {
        Map<String, Object> result = new HashMap<>();
        BigDecimal originalPrice = price.multiply(new BigDecimal(quantity));
        result.put("originalPrice", originalPrice);
        result.put("promotionApplied", false);
        result.put("finalPrice", originalPrice);
        
        // 获取所有活跃的促销活动
        List<Promotion> activePromotions = listActivePromotions();
        
        // 简单实现：应用第一个适用的促销活动
        for (Promotion promotion : activePromotions) {
            // 这里简化处理，实际应该根据促销活动的规则配置进行判断
            // 例如：满减、折扣、买赠等
            
            // 假设促销活动是折扣类型
            if (promotion.getType() == 1) { // 折扣
                BigDecimal discount = new BigDecimal("0.9"); // 9折
                BigDecimal discountedPrice = originalPrice.multiply(discount);
                result.put("promotionApplied", true);
                result.put("promotionId", promotion.getId());
                result.put("promotionName", promotion.getName());
                result.put("finalPrice", discountedPrice);
                break;
            }
        }
        
        return result;
    }
}
