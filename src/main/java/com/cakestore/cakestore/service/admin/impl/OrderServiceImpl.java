// src/main/java/com/cakestore/cakestore/service/impl/OrderServiceImpl.java
package com.cakestore.cakestore.service.admin.impl;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.repository.admin.AdminBranchRepository;
import com.cakestore.cakestore.repository.admin.AdminOrderRepository;
import com.cakestore.cakestore.repository.admin.AdminProductRepository;
import com.cakestore.cakestore.repository.admin.AdminUserRepository;
import com.cakestore.cakestore.service.admin.InventoryService;
import com.cakestore.cakestore.service.admin.OrderService;

import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl; // Không cần nữa
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final AdminOrderRepository orderRepository;
    private final AdminUserRepository userRepository;
    private final AdminBranchRepository branchRepository;
    private final AdminProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PasswordEncoder passwordEncoder;

    public OrderServiceImpl(
            AdminOrderRepository orderRepository,
            AdminUserRepository userRepository,
            AdminBranchRepository branchRepository,
            AdminProductRepository productRepository,
            InventoryService inventoryService,
            PasswordEncoder passwordEncoder) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        // dùng query có JOIN FETCH, tránh lazy crash
        return orderRepository.findByIdWithItems(id).orElse(null);
    }

    @Override
    @Transactional
    public Order updateStatus(Long id, OrderStatus newStatus) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }).orElse(null);
    }

    @Override
    @Transactional
    public Order createManualOrder(
            Long branchId, String customerEmail, String fullName,
            String phone, String line1, String city,
            PaymentMethod paymentMethod, String note,
            List<OrderItem> rawItems, BigDecimal total) {

        User user = userRepository.findByEmail(customerEmail).orElseGet(() -> {
            User newUser = new User(
                    customerEmail,
                    passwordEncoder.encode("CKS" + System.currentTimeMillis()),
                    fullName);
            newUser.setRole("customer");
            newUser.setPhone(phone);
            return userRepository.save(newUser);
        });

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không hợp lệ."));

        Address address = new Address(user, fullName, phone, line1, city);

        Order order = new Order(user, branch);
        order.setAddress(address);
        order.snapshotShippingFrom(address); // <-- Quan trọng
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTotal(total);
        order.setStatus(OrderStatus.CONFIRMED);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        order.setItems(new LinkedHashSet<>());

        for (OrderItem rawItem : rawItems) {
            Product product = productRepository.findById(rawItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sản phẩm không tồn tại: " + rawItem.getProduct().getId()));

            OrderItem item = new OrderItem(
                    order,
                    product,
                    null, // bỏ qua variant
                    rawItem.getQuantity(),
                    rawItem.getUnitPrice(),
                    rawItem.getNameSnapshot());

            calculatedTotal = calculatedTotal.add(item.getLineTotal());
            order.addItem(item);

            if (item.getQuantity() > 0) {
                inventoryService.increaseReserved(branchId, product.getId(), item.getQuantity());
            }
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm.");
        }

        order.setSubtotal(calculatedTotal);
        order.setTotal(calculatedTotal);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrders(OrderStatus status, Long branchId, Pageable pageable) {
        if (branchId != null) {
            return (status == null)
                    ? orderRepository.findByBranchId(branchId, pageable)
                    : orderRepository.findByStatusAndBranchId(status, branchId, pageable);
        } else {
            return (status == null)
                    ? orderRepository.findAll(pageable)
                    : orderRepository.findByStatus(status, pageable);
        }
    }

    @Override
    public Order findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id).orElse(null);
    }
}
