package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.OrderItem;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod; // Thêm import
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal; // Thêm import
import java.util.List; // Thêm import

public interface OrderService {
    Page<Order> findOrders(String orderStatus, Pageable pageable);
    Order findById(Long id);
    
    // Cập nhật trạng thái đơn hàng
    Order updateStatus(Long id, OrderStatus newStatus);
    
    // THÊM PHƯƠNG THỨC TẠO ĐƠN HÀNG THỦ CÔNG
    Order createManualOrder(Long branchId, String customerEmail, String fullName, 
                            String phone, String line1, String city, 
                            PaymentMethod paymentMethod, String note, 
                            List<OrderItem> rawItems, BigDecimal total);
    
    // TODO: Thêm logic cập nhật Fulfillment/PaymentStatus
}