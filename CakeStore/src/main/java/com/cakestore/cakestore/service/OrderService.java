// src/main/java/com/cakestore/cakestore/service/OrderService.java
package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.OrderItem;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    // Xóa phương thức cũ không dùng: Page<Order> findOrders(String orderStatus, Pageable pageable);
    Order findById(Long id);
    
    Order updateStatus(Long id, OrderStatus newStatus);
    
    Order createManualOrder(Long branchId, String customerEmail, String fullName, 
                            String phone, String line1, String city, 
                            PaymentMethod paymentMethod, String note, 
                            List<OrderItem> rawItems, BigDecimal total);
    
    // CẬP NHẬT: Thêm branchId để lọc
    Page<Order> findOrders(OrderStatus status, Long branchId, Pageable pageable);
    
    Order findByIdWithItems(Long id);
}