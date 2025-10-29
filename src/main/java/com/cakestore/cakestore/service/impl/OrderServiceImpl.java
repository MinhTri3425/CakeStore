// CakeStore/src/main/java/com/cakestore/cakestore/service/impl/OrderServiceImpl.java
package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.*; // Import all
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.repository.OrderRepository;
import com.cakestore.cakestore.repository.UserRepository; // Thêm UserRepository
import com.cakestore.cakestore.repository.BranchRepository; // Thêm BranchRepository
import com.cakestore.cakestore.repository.ProductRepository; // Thêm ProductRepository
import com.cakestore.cakestore.service.OrderService;
import com.cakestore.cakestore.service.InventoryService; // Thêm InventoryService
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder; // Thêm PasswordEncoder
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

    


    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    // Ép Hibernate load các quan hệ trước khi session đóng
                    if (order.getUser() != null) order.getUser().getFullName();
                    if (order.getAddress() != null) order.getAddress().getLine1();
                    order.getItems().size(); // ép load danh sách items

                    return order;
                })
                .orElse(null);
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

    /**
     * TẠO ĐƠN HÀNG MỚI (Admin/Staff tự nhập)
     * Đây là logic phức tạp, cần xử lý nhiều Entity cùng lúc.
     */
    @Override
    @Transactional
    public Order createManualOrder(Long branchId, String customerEmail, String fullName, 
                                   String phone, String line1, String city, 
                                   PaymentMethod paymentMethod, String note, 
                                   List<OrderItem> rawItems, BigDecimal total) {
        
        // 1. TÌM HOẶC TẠO USER (Khách hàng)
        User user = userRepository.findByEmail(customerEmail).orElseGet(() -> {
            // Nếu khách hàng không tồn tại, tạo User mới (password ngẫu nhiên/default)
            User newUser = new User(customerEmail, passwordEncoder.encode("CKS"+System.currentTimeMillis()), fullName);
            newUser.setRole("customer");
            newUser.setPhone(phone);
            return userRepository.save(newUser);
        });

        // 2. TÌM BRANCH
        Branch branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không hợp lệ."));

        // 3. TẠO ADDRESS (Tạo mới mỗi lần vì là đơn thủ công/địa chỉ mới)
        Address address = new Address(user, fullName, phone, line1, city);
        // addressRepository.save(address); // Giả định Address được lưu cascade từ User hoặc Order

        // 4. TẠO ORDER MỚI
        Order order = new Order(user, branch);
        order.setAddress(address); // Gắn địa chỉ
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        
        order.setShippingFee(BigDecimal.ZERO); // Giả định Shipping Fee = 0 cho đơn tạo thủ công
        order.setDiscount(BigDecimal.ZERO);    // Giả định Discount = 0 cho đơn tạo thủ công
        order.setTotal(total);
        order.setSubtotal(total); // Tạm thời Subtotal = Total (Không tính phí ship, discount)
        
        order.setStatus(OrderStatus.CONFIRMED); // Mặc định đơn tạo thủ công là CONFIRMED (đã được xác nhận)

        // 5. TẠO ORDER ITEMS VÀ TÍNH TOÁN LẠI TỔNG TIỀN CHÍNH XÁC (Backend validation)
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        order.setItems(new LinkedHashSet<>());

        for (OrderItem rawItem : rawItems) {
            Product product = productRepository.findById(rawItem.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + rawItem.getProduct().getId()));
            
            // Tạo OrderItem chính xác
            OrderItem item = new OrderItem(
                order, 
                product, 
                null, // Bỏ qua Variant
                rawItem.getQuantity(), 
                rawItem.getUnitPrice(), // Lấy đơn giá từ form (cần tính lại nếu có discount)
                rawItem.getNameSnapshot()
            );
            
            calculatedTotal = calculatedTotal.add(item.getLineTotal());
            order.addItem(item);

            // 6. CẬP NHẬT TỒN KHO (Giữ chỗ - Reserved)
            if (item.getQuantity() > 0) {
                 inventoryService.increaseReserved(branchId, product.getId(), item.getQuantity());
            }
        }
        
        // Cập nhật lại tổng tiền chính xác từ các OrderItem
        order.setSubtotal(calculatedTotal);
        order.setTotal(calculatedTotal);
        
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải có ít nhất một sản phẩm.");
        }

        return orderRepository.save(order);
    }

	@Override
	public Page<Order> findOrders(String orderStatus, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
    @Transactional(readOnly = true)
    public Page<Order> findOrders(OrderStatus status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByStatus(status, pageable);
    }




	@Override
	public Order findByIdWithItems(Long id) {
		return orderRepository.findByIdWithItems(id).orElse(null);
	}
	
}