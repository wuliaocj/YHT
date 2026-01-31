package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.ProductService;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public HttpResult detail(@PathVariable(required = false) Long id) {
        if (id == null) {
            return HttpResult.error("商品ID不能为空");
        }
        try {
            GetProductVO productVO = productService.getProductById(id);
            return HttpResult.ok("查询成功",productVO);
        } catch (Exception e) {
            log.error("查询商品详情失败：", e);
            return HttpResult.error("查询商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 添加商品，同时包括了商品可用的小料和规格
     * @param addProductVO 商品信息
     * @return 添加结果
     */
    @PostMapping("/admin/add")
    public HttpResult addProduct(@Valid @RequestBody AddProductVO addProductVO) {
        String result = productService.addProduct(addProductVO);
        log.info("添加商品结果：{}", result);
        return HttpResult.ok("商品添加成功");
    }

    @GetMapping("/admin/list")
    public HttpResult listProduct() {
        return HttpResult.ok( "商品查询成功",productService.getProductList());
    }

    @PostMapping("/admin/update")
    public HttpResult updateProduct(@Valid @RequestBody AddProductVO addProductVO) {
        return HttpResult.ok("商品修改成功",productService.updateProduct(addProductVO));
    }

    @GetMapping("/user/list")
    public HttpResult listUserProduct() {
        return HttpResult.ok("用户商品查询成功",productService.getProductList());
    }

    /**
     * 搜索商品
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/user/search")
    public HttpResult searchProduct(@RequestParam String keyword) {
        try {
            return HttpResult.ok("商品搜索成功",productService.searchProducts(keyword));
        } catch (Exception e) {
            log.error("搜索商品失败：", e);
            return HttpResult.error("搜索商品失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/delete/{id}")
    public HttpResult deleteProduct(@PathVariable(required = false) Long id) {
        if (id == null) {
            return HttpResult.error("商品ID不能为空");
        }
        try {
            productService.deleteProduct(id);
            return HttpResult.ok("商品删除成功");
        } catch (Exception e) {
            log.error("删除商品失败：", e);
            return HttpResult.error("删除商品失败：" + e.getMessage());
        }
    }

}


