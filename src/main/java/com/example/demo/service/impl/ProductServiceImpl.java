package com.example.demo.service.impl;

import com.example.demo.domain.Category;
import com.example.demo.domain.Product;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public ProductServiceImpl(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectAll();
    }

    @Override
    public List<Product> listProductsByCategory(Integer categoryId) {
        return productMapper.selectByCategoryId(categoryId);
    }

    @Override
    public Product getProductDetail(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public List<Product> listHotProducts() {
        return productMapper.selectHotProducts();
    }

    @Override
    public List<Product> listAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
        return product;
    }

    @Override
    public void deleteProduct(Integer id) {
        productMapper.delete(id);
    }
}


