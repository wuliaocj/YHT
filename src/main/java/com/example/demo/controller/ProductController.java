package com.example.demo.controller;

import com.example.demo.domain.Category;
import com.example.demo.domain.Product;
import com.example.demo.http.HttpResult;
import com.example.demo.service.ProductService;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public HttpResult detail(@PathVariable long id) {
        GetProductVO productVO = productService.getProductById(id);
        return HttpResult.ok("查询成功",productVO);
    }

    /**
     * 添加商品，同时包括了商品可用的小料和规格
     * @param addProductVO
     * @return
     */
    @PostMapping("/admin/add")
    public HttpResult addProduct(@Valid @RequestBody AddProductVO addProductVO) {
        // 调用 Service 层添加商品逻辑
        System.out.println(productService.addProduct(addProductVO));
        return HttpResult.ok( "商品添加成功");
    }

    @GetMapping("/admin/list")
    public HttpResult listProduct() {
        return HttpResult.ok( "商品查询成功",productService.getProductList());
    }
}


