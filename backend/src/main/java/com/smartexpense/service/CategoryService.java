package com.smartexpense.service;

import com.smartexpense.dto.response.CategoryDTO;
import com.smartexpense.model.Category;
import com.smartexpense.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CategoryDTO getCategoryByCode(String code) {
        Category category = categoryRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Category not found: " + code));
        return convertToDTO(category);
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCode(category.getCode());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setItrSection(category.getItrSection());
        dto.setMaxLimit(category.getMaxLimit());
        dto.setApplicableFor(category.getApplicableFor());
        dto.setIsActive(category.getIsActive());
        return dto;
    }
}