package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.repository.OrderRepository;
import com.cakestore.cakestore.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Page<Order> findOrders(String orderStatus, Pageable pageable) {
        // TODO: Viết custom method trong Repository để lọc theo Status
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order findById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            // UpdatedAt được DB quản lý
            // TODO: Thêm logic nghiệp vụ khi chuyển trạng thái (ví dụ: tạo Fulfillment khi chuyển sang SHIPPING)
            return orderRepository.save(order);
        }).orElse(null);
    }
}