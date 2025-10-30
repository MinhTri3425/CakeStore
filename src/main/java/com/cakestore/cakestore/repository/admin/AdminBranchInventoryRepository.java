// src/main/java/com/cakestore/cakestore/repository/admin/AdminBranchInventoryRepository.java
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.BranchInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminBranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

        // ===== Tìm theo Branch + Product (cho cập nhật tồn kho) =====
        Optional<BranchInventory> findByBranchIdAndProductId(Long branchId, Long productId);

        // ===== Truy vấn phân trang có JOIN, tránh lazy crash =====
        // Dùng EntityGraph để load branch + product mà không cần FETCH JOIN (đỡ lỗi SQL
        // Server)
        @EntityGraph(attributePaths = { "branch", "product" })
        @Query("""
                        SELECT bi FROM BranchInventory bi
                        WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
                          AND (:keyword IS NULL OR :keyword = '' OR
                               LOWER(bi.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                               LOWER(bi.product.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
                        """)
        Page<BranchInventory> searchInventory(
                        @Param("branchId") Long branchId,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
