package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<Order> findOrders(String orderStatus, Pageable pageable);
    Order findById(Long id);
    
    // Cập nhật trạng thái đơn hàng
    Order updateStatus(Long id, OrderStatus newStatus);
    
    // TODO: Thêm logic cập nhật Fulfillment/PaymentStatus
}