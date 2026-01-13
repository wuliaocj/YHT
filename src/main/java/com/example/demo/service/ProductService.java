package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.Product;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;

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
}


