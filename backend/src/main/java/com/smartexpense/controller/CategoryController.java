package com.smartexpense.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.CategoryDTO;
import com.smartexpense.dto.response.ApiResponse;
import com.smartexpense.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryByCode(@PathVariable String code) {
        CategoryDTO category = categoryService.getCategoryByCode(code);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}