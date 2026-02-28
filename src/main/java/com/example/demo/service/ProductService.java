package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.Product;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import java.util.List;

public interface ProductService extends IService<Product> {

//    List<Category> listCategories();
//
//    List<Product> listProductsByCategory(Integer categoryId);
//
//    Product getProductDetail(Integer productId);
//
//    List<Product> listHotProducts();
//
//    List<Product> listAllProducts();
//
//    Product saveProduct(Product product);
//
//    void deleteProduct(Integer id);
//
//    List<Product> listNewProducts();
//
    String addProduct(AddProductVO addProductVO);

    GetProductVO getProductById(Long productId);

    List<GetProductVO> getProductList();

    /**
     * 分页获取商品列表
     * @param pageRequest 分页请求参数
     * @param keyword 搜索关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @return 分页响应
     */
    PageResponseVO<GetProductVO> getProductListByPage(PageRequestVO pageRequest, String keyword, Integer categoryId, Integer status);

    Object updateProduct(AddProductVO addProductVO);

    void deleteProduct(Long productId);

    /**
     * 搜索商品
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<GetProductVO> searchProducts(String keyword);
}


