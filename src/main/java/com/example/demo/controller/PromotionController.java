package com.example.demo.controller;

import com.example.demo.domain.Promotion;
import com.example.demo.http.HttpResult;
import com.example.demo.service.PromotionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotion")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/list")
    public HttpResult list() {
        List<Promotion> list = promotionService.listAll();
        return HttpResult.ok(list);
    }

    @PostMapping("/save")
    public HttpResult save(@RequestBody Promotion promotion) {
        Promotion saved = promotionService.save(promotion);
        return HttpResult.ok(saved);
    }

    @PostMapping("/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        promotionService.delete(id);
        return HttpResult.ok("删除成功");
    }
}
