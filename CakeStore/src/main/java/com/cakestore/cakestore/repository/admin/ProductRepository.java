// CakeStore/src/main/java/com/cakestore/cakestore/repository/ProductRepository.java
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "images"})
    @Query("""
       SELECT p FROM Product p
       WHERE lower(p.name) LIKE lower(concat('%', :keyword, '%'))
          OR lower(p.sku)  LIKE lower(concat('%', :keyword, '%'))
    """)
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "images"})
    @Query("""
       SELECT p FROM Product p
       WHERE (lower(p.name) LIKE lower(concat('%', :keyword, '%'))
              OR lower(p.sku) LIKE lower(concat('%', :keyword, '%')))
         AND (:categoryId IS NULL OR p.category.id = :categoryId)
    """)
    Page<Product> searchAndFilter(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  Pageable pageable);

    long countByCategoryId(Long categoryId);
    
    @EntityGraph(attributePaths = {"images"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
}