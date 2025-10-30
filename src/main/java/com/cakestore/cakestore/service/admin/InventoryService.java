// src/main/java/com/cakestore/cakestore/service/admin/InventoryService.java
package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.BranchInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    /**
     * Tìm kiếm tồn kho (có phân trang, tự động fetch branch & product)
     *
     * @param branchId ID chi nhánh (nullable)
     * @param keyword  Từ khóa tìm kiếm theo tên hoặc SKU sản phẩm
     * @param pageable Cấu hình phân trang
     * @return Trang dữ liệu tồn kho
     */
    Page<BranchInventory> searchInventory(Long branchId, String keyword, Pageable pageable);

    /**
     * Tìm bản ghi tồn kho theo ID
     *
     * @param id ID tồn kho
     * @return BranchInventory nếu tồn tại, ngược lại null
     */
    BranchInventory findById(Long id);

    /**
     * Cập nhật số lượng tồn kho (Quantity) cho sản phẩm tại chi nhánh.
     * Nếu chưa có bản ghi tồn kho, sẽ tự động tạo mới.
     *
     * @param branchId    ID chi nhánh
     * @param productId   ID sản phẩm
     * @param newQuantity Số lượng tồn mới
     * @return BranchInventory sau khi cập nhật
     */
    BranchInventory updateQuantity(Long branchId, Long productId, Integer newQuantity);

    /**
     * Tăng số lượng Reserved (đã giữ chỗ) cho sản phẩm trong đơn hàng.
     *
     * @param branchId  ID chi nhánh
     * @param productId ID sản phẩm
     * @param quantity  Số lượng tăng
     */
    void increaseReserved(Long branchId, Long productId, int quantity);

    /**
     * Giảm số lượng Reserved (khi đơn hàng bị hủy hoặc giao xong)
     *
     * @param branchId  ID chi nhánh
     * @param productId ID sản phẩm
     * @param quantity  Số lượng giảm
     */
    void decreaseReserved(Long branchId, Long productId, int quantity);
}
