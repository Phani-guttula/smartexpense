package com.smartexpense.repository;

import com.smartexpense.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    Optional<Category> findByCode(String code);
    
    List<Category> findByIsActiveTrue();
    
    Boolean existsByCode(String code);
}