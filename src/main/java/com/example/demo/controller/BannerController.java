package com.example.demo.controller;

import com.example.demo.domain.Banner;
import com.example.demo.http.HttpResult;
import com.example.demo.service.BannerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banner")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping("/list")
    public HttpResult list() {
        List<Banner> list = bannerService.listAll();
        return HttpResult.ok(list);
    }

    @PostMapping("/save")
    public HttpResult save(@RequestBody Banner banner) {
        Banner saved = bannerService.save(banner);
        return HttpResult.ok(saved);
    }

    @PostMapping("/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        bannerService.delete(id);
        return HttpResult.ok("删除成功");
    }


}
