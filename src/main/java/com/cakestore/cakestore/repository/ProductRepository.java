package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Trang chủ: sản phẩm có SoldCount > 10, sắp xếp giảm dần */
    @Query("""
                select p from Product p
                 join ProductStats s on s.product = p
                where p.status = 1 and s.soldCount > 10
                order by s.soldCount desc
            """)
    List<Product> topSellingOver10(Pageable pageable);

    Optional<Product> findBySlug(String slug);

    /** Search/filter danh mục (20/sp trang, order mới nhất) */
    @Query("""
                select p from Product p
                where p.status = 1
                  and (:categoryId is null or p.category.id = :categoryId)
                  and (:kw is null or lower(p.name) like lower(concat('%', :kw, '%')))
                order by p.id desc
            """)
    Page<Product> search(Long categoryId, String kw, Pageable pageable);

    @Query("""
              select p from Product p
               left join ProductStats s on s.product = p
              where p.status = 1
                and (:categoryId is null or p.category.id = :categoryId)
                and (:kw is null or lower(p.name) like lower(concat('%', :kw, '%')))
            """)
    Page<Product> searchAll(Long categoryId, String kw, Pageable pageable);
}
