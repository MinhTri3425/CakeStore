
package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.BranchInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    
    Page<BranchInventory> searchInventory(Long branchId, String keyword, Pageable pageable);

    BranchInventory findById(Long id);

    /** * Cập nhật số lượng tồn kho (quantity) cho một sản phẩm tại một chi nhánh.
     * Nếu chưa tồn tại BranchInventory, tạo mới.
     */
    BranchInventory updateQuantity(Long branchId, Long productId, Integer newQuantity);
    
    /**
     * Tăng số lượng Reserved (hàng đã giữ chỗ cho đơn hàng)
     */
    void increaseReserved(Long branchId, Long productId, int quantity);

    /**
     * Giảm số lượng Reserved (khi đơn hàng bị hủy hoặc giao thành công)
     */
    void decreaseReserved(Long branchId, Long productId, int quantity);
}