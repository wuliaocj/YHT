package com.example.demo.controller;

import com.example.demo.domain.Promotion;
import com.example.demo.http.HttpResult;
import com.example.demo.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // 管理员接口
    @RequestMapping("/api/admin/promotion")
    public static class AdminPromotionController {

        private final PromotionService promotionService;

        public AdminPromotionController(PromotionService promotionService) {
            this.promotionService = promotionService;
        }

        /**
         * 获取活动列表
         * @return
         */
        @GetMapping("/list")
        public HttpResult list() {
            List<Promotion> list = promotionService.listAll();
            return HttpResult.ok(list);
        }

        /**
         * 添加活动
         * @param promotion
         * @return
         */
        @PostMapping("/save")
        public HttpResult save(@RequestBody Promotion promotion) {
            Promotion saved = promotionService.save(promotion);
            return HttpResult.ok(saved);
        }

        /**
         * 根据活动ID删除活动
         * @param id
         * @return
         */
        @PostMapping("/delete/{id}")
        public HttpResult delete(@PathVariable Integer id) {
            promotionService.delete(id);
            return HttpResult.ok("删除成功");
        }
    }

    // 用户接口
    @RequestMapping("/api/promotion")
    public static class UserPromotionController {

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
}
