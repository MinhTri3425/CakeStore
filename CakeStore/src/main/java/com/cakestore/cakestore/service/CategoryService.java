package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    void deleteById(Long id);
    boolean existsByName(String name); // Sẽ dùng cho validation
}