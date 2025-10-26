package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @EntityGraph(attributePaths = { "images" })
  @Query("""
          select p
          from Product p
          join p.productStats s
          where p.status = 1
            and s.soldCount > 10
          order by s.soldCount desc, p.id desc
      """)
  List<Product> topSellingOver10(Pageable pageable);

  @EntityGraph(attributePaths = { "images" })
  @Query("""
          select p
          from Product p
          where (:categoryId is null or p.category.id = :categoryId)
            and (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
          order by p.id desc
      """)
  Page<Product> search(@Param("categoryId") Long categoryId,
      @Param("q") String q,
      Pageable pageable);

  @EntityGraph(attributePaths = { "images" })
  @Query("""
          select p
          from Product p
          where (:categoryId is null or p.category.id = :categoryId)
            and (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
      """)
  Page<Product> searchAll(@Param("categoryId") Long categoryId,
      @Param("q") String q,
      Pageable pageable);

  @EntityGraph(attributePaths = { "images" })
  @Query("select p from Product p where p.id = :id")
  Optional<Product> findByIdWithImages(@Param("id") Long id);
}
