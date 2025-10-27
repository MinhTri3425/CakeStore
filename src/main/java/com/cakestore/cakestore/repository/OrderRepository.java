package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Phương thức để lấy các đơn hàng theo trạng thái cụ thể
    List<Order> findByStatus(Order.OrderStatus status);
    
    // TODO: Thêm phương thức tìm kiếm/lọc phức tạp hơn nếu cần
}