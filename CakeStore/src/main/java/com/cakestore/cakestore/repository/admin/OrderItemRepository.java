// src/main/java/com/cakestore/cakestore/repository/OrderItemRepository.java
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Lấy top sản phẩm bán chạy nhất dựa trên tổng số lượng đã bán.
     * Trả về List các Map, mỗi Map chứa 'productName' (String) và 'totalQuantity' (Long).
     * @param pageable Sử dụng Pageable để giới hạn kết quả (ví dụ: top 5).
     */
    @Query("""
            SELECT oi.product.name as productName, SUM(oi.quantity) as totalQuantity
            FROM OrderItem oi
            WHERE oi.order.status NOT IN (com.cakestore.cakestore.entity.Order.OrderStatus.CANCELED, com.cakestore.cakestore.entity.Order.OrderStatus.RETURNED)
            GROUP BY oi.product.name
            ORDER BY totalQuantity DESC
            """) // Đã xóa comment ở cuối dòng WHERE
    List<Map<String, Object>> findTopSellingProducts(Pageable pageable);

}