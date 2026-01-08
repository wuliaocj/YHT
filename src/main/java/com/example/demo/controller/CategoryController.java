package com.example.demo.controller;

import com.example.demo.domain.Category;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.CategoryMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @PostMapping("/admin/category/save")
    public HttpResult save(@RequestBody Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.update(category);
        }
        return HttpResult.ok(category);
    }

    @PostMapping("/admin/category/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        categoryMapper.delete(id);
        return HttpResult.ok("删除成功");
    }
}
