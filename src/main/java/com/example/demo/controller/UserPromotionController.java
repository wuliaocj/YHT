package com.example.demo.controller;

import com.example.demo.domain.Promotion;
import com.example.demo.http.HttpResult;
import com.example.demo.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion")
public class UserPromotionController {

    private final PromotionService promotionService;

    public UserPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * 获取活跃的促销活动
     * @return
     */
    @GetMapping("/active")
    public HttpResult listActivePromotions() {
        List<Promotion> list = promotionService.listActivePromotions();
        return HttpResult.ok(list);
    }

    /**
     * 应用促销活动
     * @param productId
     * @param quantity
     * @param price
     * @return
     */
    @GetMapping("/apply")
    public HttpResult applyPromotion(@RequestParam Integer productId, 
                                     @RequestParam Integer quantity, 
                                     @RequestParam BigDecimal price) {
        Map<String, Object> result = promotionService.applyPromotion(productId, quantity, price);
        return HttpResult.ok(result);
    }
}
