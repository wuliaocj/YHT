package com.example.demo.controller;

import com.example.demo.domain.Category;
import com.example.demo.domain.Product;
import com.example.demo.http.HttpResult;
import com.example.demo.service.ProductService;
import com.example.demo.vo.AddProductVO;
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

//    @GetMapping("/categories")
//    public HttpResult listCategories() {
//        List<Category> categories = productService.listCategories();
//        return HttpResult.ok(categories);
//    }
//
//    @GetMapping("/category/{categoryId}")
//    public HttpResult listByCategory(@PathVariable Integer categoryId) {
//        List<Product> products = productService.listProductsByCategory(categoryId);
//        return HttpResult.ok(products);
//    }
//
//    @GetMapping("/{id}")
//    public HttpResult detail(@PathVariable Integer id) {
//        Product product = productService.getProductDetail(id);
//        if (product == null) {
//            return HttpResult.error("商品不存在");
//        }
//        Map<String, Object> result = new HashMap<>();
//        result.put("product", product);
//        return HttpResult.ok(result);
//    }
//
//    @GetMapping("/hot")
//    public HttpResult hotProducts() {
//        return HttpResult.ok(productService.listHotProducts());
//    }
//
//    @GetMapping("/new")
//    public HttpResult newProducts() {
//        return HttpResult.ok(productService.listNewProducts());
//    }
//
//    // 管理后台接口
//    @GetMapping("/admin/product/list")
//    public HttpResult adminListProducts() {
//        return HttpResult.ok(productService.listAllProducts());
//    }
//
//    /**
//     * 添加商品，同时包括了商品可用的小料和规格
//     * @param addProductVO
//     * @return
//     */
//    public HttpResult addProduct(@Valid @RequestBody AddProductVO addProductVO) {
//        // 调用 Service 层添加商品逻辑
//        return HttpResult.ok(productService.addProduct(addProductVO), "商品添加成功");
//    }
//
//
//    @PostMapping("/admin/product/delete/{id}")
//    public HttpResult adminDeleteProduct(@PathVariable Integer id) {
//        productService.deleteProduct(id);
//        return HttpResult.ok("删除成功");
//    }
}


