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
    public HttpResult detail(@PathVariable long id) {
        GetProductVO productVO = productService.getProductById(id);
        return HttpResult.ok("查询成功",productVO);
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
}


