// src/main/java/com/cakestore/cakestore/service/admin/impl/InventoryServiceImpl.java
package com.cakestore.cakestore.service.admin.impl;

import com.cakestore.cakestore.entity.BranchInventory;
import com.cakestore.cakestore.repository.admin.AdminBranchInventoryRepository;
import com.cakestore.cakestore.repository.admin.AdminBranchRepository;
import com.cakestore.cakestore.repository.admin.AdminProductRepository;
import com.cakestore.cakestore.service.admin.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final AdminBranchInventoryRepository inventoryRepository;
    private final AdminBranchRepository branchRepository;
    private final AdminProductRepository productRepository;

    public InventoryServiceImpl(
            AdminBranchInventoryRepository inventoryRepository,
            AdminProductRepository productRepository,
            AdminBranchRepository branchRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
    }

    // ✅ Truy vấn tồn kho có phân trang + tìm kiếm (đã EntityGraph branch + product)
    @Override
    public Page<BranchInventory> searchInventory(Long branchId, String keyword, Pageable pageable) {
        return inventoryRepository.searchInventory(branchId, keyword, pageable);
    }

    @Override
    public BranchInventory findById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public BranchInventory updateQuantity(Long branchId, Long productId, Integer newQuantity) {
        BranchInventory inventory = inventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .orElseGet(() -> {
                    var branch = branchRepository.findById(branchId).orElse(null);
                    var product = productRepository.findById(productId).orElse(null);

                    if (branch == null || product == null) {
                        throw new IllegalArgumentException("Chi nhánh hoặc Sản phẩm không tồn tại.");
                    }
                    return new BranchInventory(branch, product, 0);
                });

        if (newQuantity < inventory.getReserved()) {
            throw new IllegalArgumentException(
                    "Số lượng tồn (Quantity) không thể nhỏ hơn số lượng đã giữ chỗ (Reserved).");
        }

        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void increaseReserved(Long branchId, Long productId, int quantity) {
        if (quantity <= 0)
            return;

        inventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .ifPresent(bi -> {
                    bi.setReserved(bi.getReserved() + quantity);
                    inventoryRepository.save(bi);
                });
    }

    @Override
    @Transactional
    public void decreaseReserved(Long branchId, Long productId, int quantity) {
        if (quantity <= 0)
            return;

        inventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .ifPresent(bi -> {
                    int newReserved = Math.max(0, bi.getReserved() - quantity);
                    bi.setReserved(newReserved);
                    inventoryRepository.save(bi);
                });
    }
}
