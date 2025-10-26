package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Category;
import com.cakestore.cakestore.repository.CategoryRepository;
import com.cakestore.cakestore.service.CategoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        // Tìm tất cả Category đang hoạt động và sắp xếp theo sortOrder
        return categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .sorted((c1, c2) -> Integer.compare(c1.getSortOrder(), c2.getSortOrder()))
                .toList();
    }
    
    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
    
    @Override
    public Category save(Category category) {
        // Logic tự động tạo slug nếu cần
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            // Placeholder: Cần triển khai hàm chuyển tên -> slug
            category.setSlug(generateSlug(category.getName())); 
        }
        // Liên kết lại Parent Category nếu chỉ truyền ID
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = findById(category.getParent().getId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        return categoryRepository.save(category);
    }
    
    @Override
    public void deleteById(Long id) {
        // Lưu ý: Xóa vĩnh viễn có thể gây lỗi FK. Cần cân nhắc chỉ set isActive=false.
        categoryRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        // TODO: Triển khai phương thức kiểm tra tên tồn tại
        return false;
    }
    
    // Hàm giả định tạo slug
    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
    }

    @Override
    public Page<Category> search(String q, Boolean active, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return categoryRepository.search(query, active, pageable);
    }
}