package com.example.demo.service.impl;

import com.example.demo.domain.Promotion;
import com.example.demo.mapper.PromotionMapper;
import com.example.demo.service.PromotionService;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
