// File: CakeStore/src/main/java/com/cakestore/cakestore/service/admin/impl/ProductServiceImpl.java (Đã sửa)

package com.cakestore.cakestore.service.admin.impl;

import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.repository.admin.AdminProductRepository;
import com.cakestore.cakestore.service.admin.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate; // <-- THÊM DÒNG IMPORT NÀY

@Service
public class ProductServiceImpl implements ProductService {

    private final AdminProductRepository productRepository;

    public ProductServiceImpl(AdminProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findPaginatedProducts(String keyword, Long categoryId, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        if (categoryId != null && categoryId > 0) {
            return productRepository.searchAndFilter(keyword, categoryId, pageable);
        } else {
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

    @Override
    @Transactional
    public Product getProductForEdit(Long id) {
        Product product = productRepository.findById(id).orElse(null);

        if (product != null) {
            // SỬ DỤNG Hibernate.initialize() để cưỡng chế tải Category
            if (product.getCategory() != null) {
                Hibernate.initialize(product.getCategory());
            }
            // SỬ DỤNG Hibernate.initialize() để cưỡng chế tải ProductImages
            if (product.getImages() != null) {
                Hibernate.initialize(product.getImages());
            }
        }
        return product;
    }
}