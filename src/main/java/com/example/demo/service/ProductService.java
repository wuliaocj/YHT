package com.example.demo.service;

import com.example.demo.domain.Category;
import com.example.demo.domain.Product;

import java.util.List;

public interface ProductService {

    List<Category> listCategories();

    List<Product> listProductsByCategory(Integer categoryId);

    Product getProductDetail(Integer productId);

    List<Product> listHotProducts();

    List<Product> listAllProducts();

    Product saveProduct(Product product);

    void deleteProduct(Integer id);

    List<Product> listNewProducts();
}


