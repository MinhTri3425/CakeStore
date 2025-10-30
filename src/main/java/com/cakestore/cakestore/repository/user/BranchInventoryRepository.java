package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {

  // Dùng cho trang chi tiết: lấy tồn kho 1 sản phẩm trong 1 chi nhánh
  Optional<BranchInventory> findByBranch_IdAndProduct_Id(Long branchId, Long productId);

  // Dùng cho trang danh sách: lấy tất cả productId còn hàng (>0 available) trong
  // chi nhánh
  @Query("""
          select bi.product.id
          from BranchInventory bi
          where bi.branch.id = :branchId
            and (coalesce(bi.quantity,0) - coalesce(bi.reserved,0)) > 0
      """)
  List<Long> findInStockProductIdsByBranch(@Param("branchId") Long branchId);
}
