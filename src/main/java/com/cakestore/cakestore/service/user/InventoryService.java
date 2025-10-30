package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.BranchInventory;
import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.OrderItem;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final BranchInventoryRepository branchInventoryRepo;

    /**
     * Nhả hàng đã giữ (reserved) cho đơn.
     * Chỉ dùng khi huỷ đơn trước khi xuất kho thực sự.
     */
    @Transactional
    public void releaseReservedFromOrder(Order order) {
        // nếu đơn không gắn chi nhánh (không nên xảy ra, nhưng cứ phòng thủ)
        if (order.getBranch() == null) {
            return;
        }
        Long branchId = order.getBranch().getId();

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null) {
                continue;
            }
            Long productId = item.getProduct().getId();
            int qty = item.getQuantity();

            BranchInventory inv = branchInventoryRepo
                    .findByBranch_IdAndProduct_Id(branchId, productId)
                    .orElse(null);

            if (inv != null) {
                int cur = inv.getReserved() != null ? inv.getReserved() : 0;
                int next = cur - qty;
                if (next < 0)
                    next = 0; // không để âm
                inv.setReserved(next);
                branchInventoryRepo.save(inv);
            }
        }
    }
}
