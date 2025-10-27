package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.repository.ProductRepository;
import com.cakestore.cakestore.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<Product> findPaginatedProducts(String keyword, Long categoryId, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        if (categoryId != null && categoryId > 0) {
            return productRepository.searchAndFilter(keyword, categoryId, pageable);
        } else {
            // Tìm kiếm chung nếu không có lọc theo Category
            return productRepository.searchProducts(keyword, pageable);
        }
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Product getProductForEdit(Long id) {
        return productRepository.findByIdWithImages(id).orElse(null);
    }
    
    
}