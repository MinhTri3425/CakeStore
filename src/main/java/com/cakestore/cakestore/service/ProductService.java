package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<Product> findPaginatedProducts(String keyword, Long categoryId, Pageable pageable);
    Product getProductById(Long id);
    Product saveProduct(Product product);
    void deleteProduct(Long id);
    Product getProductForEdit(Long id);
    
}