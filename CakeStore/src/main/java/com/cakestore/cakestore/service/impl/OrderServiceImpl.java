// src/main/java/com/cakestore/cakestore/service/impl/OrderServiceImpl.java
package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.repository.OrderRepository;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.repository.BranchRepository;
import com.cakestore.cakestore.repository.ProductRepository;
import com.cakestore.cakestore.service.OrderService;
import com.cakestore.cakestore.service.InventoryService;
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

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PasswordEncoder passwordEncoder;

    // ... (Constructor giữ nguyên) ...
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, 
                            BranchRepository branchRepository, ProductRepository productRepository, 
                            InventoryService inventoryService, PasswordEncoder passwordEncoder) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.passwordEncoder = passwordEncoder;
    }


    // ... (findById, updateStatus, createManualOrder giữ nguyên) ...
    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    if (order.getUser() != null) order.getUser().getFullName();
                    if (order.getAddress() != null) order.getAddress().getLine1();
                    order.getItems().size();
                    return order;
                })
                .orElse(null);
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
    public Order createManualOrder(Long branchId, String customerEmail, String fullName, 
                                   String phone, String line1, String city, 
                                   PaymentMethod paymentMethod, String note, 
                                   List<OrderItem> rawItems, BigDecimal total) {
        
        User user = userRepository.findByEmail(customerEmail).orElseGet(() -> {
            User newUser = new User(customerEmail, passwordEncoder.encode("CKS"+System.currentTimeMillis()), fullName);
            newUser.setRole("customer");
            newUser.setPhone(phone);
            return userRepository.save(newUser);
        });

        Branch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không hợp lệ."));

        Address address = new Address(user, fullName, phone, line1, city);
        Order order = new Order(user, branch);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setShippingFee(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotal(total);
        order.setSubtotal(total);
        order.setStatus(OrderStatus.CONFIRMED);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        order.setItems(new LinkedHashSet<>());

        for (OrderItem rawItem : rawItems) {
            Product product = productRepository.findById(rawItem.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + rawItem.getProduct().getId()));
            
            OrderItem item = new OrderItem(
                order, 
                product, 
                null, // Bỏ qua Variant
                rawItem.getQuantity(), 
                rawItem.getUnitPrice(),
                rawItem.getNameSnapshot()
            );
            
            calculatedTotal = calculatedTotal.add(item.getLineTotal());
            order.addItem(item);

            if (item.getQuantity() > 0) {
                 inventoryService.increaseReserved(branchId, product.getId(), item.getQuantity());
            }
        }
        
        order.setSubtotal(calculatedTotal);
        order.setTotal(calculatedTotal);
        
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm.");
        }

        return orderRepository.save(order);
    }

    // XÓA PHƯƠNG THỨC CŨ
	// @Override
	// public Page<Order> findOrders(String orderStatus, Pageable pageable) {
	// 	return null;
	// }
	
    // CẬP NHẬT PHƯƠNG THỨC NÀY
    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrders(OrderStatus status, Long branchId, Pageable pageable) {
        if (branchId != null) {
            // Đây là Staff, lọc theo chi nhánh
            if (status == null) {
                return orderRepository.findByBranchId(branchId, pageable);
            }
            return orderRepository.findByStatusAndBranchId(status, branchId, pageable);
        } else {
            // Đây là Admin, thấy tất cả
            if (status == null) {
                return orderRepository.findAll(pageable);
            }
            return orderRepository.findByStatus(status, pageable);
        }
    }

	@Override
	public Order findByIdWithItems(Long id) {
		return orderRepository.findByIdWithItems(id).orElse(null);
	}
}