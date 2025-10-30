
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.BranchInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

    Optional<BranchInventory> findByBranchIdAndProductId(Long branchId, Long productId);

    // Tìm kiếm tồn kho theo tên sản phẩm/SKU, lọc theo chi nhánh
    @Query("""
        SELECT bi FROM BranchInventory bi JOIN FETCH bi.product p
        WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
          AND (:keyword IS NULL OR :keyword = '' OR
               LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<BranchInventory> searchInventory(@Param("branchId") Long branchId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}