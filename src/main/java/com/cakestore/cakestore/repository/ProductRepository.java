package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Tìm kiếm/Lọc sản phẩm theo tên hoặc SKU */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.sku LIKE %:keyword%")
    Page<Product> searchProducts(String keyword, Pageable pageable);

    /** Tìm kiếm sản phẩm theo tên, SKU và Category ID (dùng cho lọc) */
    @Query("SELECT p FROM Product p WHERE (p.name LIKE %:keyword% OR p.sku LIKE %:keyword%) AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> searchAndFilter(String keyword, Long categoryId, Pageable pageable);
}