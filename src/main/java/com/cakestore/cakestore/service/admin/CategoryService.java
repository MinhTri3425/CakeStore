package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    void deleteById(Long id);
    boolean existsByName(String name); // Sẽ dùng cho validation
    
    Page<Category> search(String q, Boolean active, Pageable pageable);
}