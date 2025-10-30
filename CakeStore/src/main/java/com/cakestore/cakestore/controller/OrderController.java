// src/main/java/com/cakestore/cakestore/controller/OrderController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.OrderItem; 
import com.cakestore.cakestore.entity.Product; 
import com.cakestore.cakestore.entity.User; // THÊM IMPORT
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.service.OrderService;
import com.cakestore.cakestore.service.BranchService; 
import com.cakestore.cakestore.service.ProductService;
import com.cakestore.cakestore.service.UserService; // THÊM IMPORT

import jakarta.servlet.http.HttpServletRequest; // THÊM IMPORT
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // THÊM IMPORT
import org.springframework.security.core.userdetails.UserDetails; // THÊM IMPORT
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.cakestore.cakestore.dto.ProductOption;
import java.math.BigDecimal;
import java.util.ArrayList; 
import java.util.List; 

@Controller
// BỎ /staff/orders, chỉ dùng /admin/orders
@RequestMapping("/admin/orders") 
public class OrderController {

    private final OrderService orderService;
    private final BranchService branchService; 
    private final ProductService productService;
    private final UserService userService; // THÊM SERVICE

    public OrderController(OrderService orderService, BranchService branchService, 
                           ProductService productService, UserService userService) { // CẬP NHẬT CONSTRUCTOR
        this.orderService = orderService;
        this.branchService = branchService;
        this.productService = productService;
        this.userService = userService; // INJECT
    }

    /**
     * GET /admin/orders - Hiển thị danh sách đơn hàng (Cho cả Admin và Staff)
     */
    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) OrderStatus status,
                             HttpServletRequest request, // Thêm HttpServletRequest
                             @AuthenticationPrincipal UserDetails userDetails) { // Thêm UserDetails

        Pageable pageable = PageRequest.of(page, size);
        Long branchId = null; // Admin thấy tất cả

        // Nếu là Staff, lọc theo chi nhánh
        if (request.isUserInRole("ROLE_STAFF") && userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            if (currentUser != null && currentUser.getBranch() != null) {
                branchId = currentUser.getBranch().getId();
            } else {
                // Nếu Staff không có chi nhánh (lỗi dữ liệu), không cho thấy gì
                branchId = -1L; // ID không thể tồn tại
            }
        }

        Page<Order> orderPage = orderService.findOrders(status, branchId, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status != null ? status.name() : "");

        return "admin/orders";
    }

    // ... (showCreateForm, createOrder, viewOrder, updateStatus giữ nguyên) ...
    // Các hàm này Staff đã được cấp quyền truy cập trong SecurityConfig
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        var products = productService.findPaginatedProducts(
            null, null,
            PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "name"))
        ).getContent();

        var productOptions = products.stream()
            .map(p -> new ProductOption(p.getId(), p.getName(), p.getSku(), p.getPrice()))
            .toList();

        model.addAttribute("products", productOptions);

        return "admin/order-form";
    }

    @PostMapping
    public String createOrder(@RequestParam Long branchId,
                              @RequestParam String customerEmail,
                              @RequestParam String fullName,
                              @RequestParam String phone,
                              @RequestParam String line1,
                              @RequestParam String city,
                              @RequestParam PaymentMethod paymentMethod,
                              @RequestParam(required = false) String note,
                              @RequestParam(required = false) List<Long> productId,
                              @RequestParam(required = false) List<Integer> quantity,
                              @RequestParam(required = false) List<BigDecimal> unitPrice,
                              @RequestParam(required = false) List<String> nameSnapshot,
                              @RequestParam BigDecimal total,
                              RedirectAttributes ra) {

        if (productId == null || productId.isEmpty()) {
            ra.addFlashAttribute("error", "Đơn hàng phải có ít nhất một sản phẩm.");
            return "redirect:/admin/orders/new";
        }
        
        List<OrderItem> rawItems = new ArrayList<>();
        for (int i = 0; i < productId.size(); i++) {
            if (productId.get(i) != null) {
                OrderItem tempItem = new OrderItem();
                Product dummyProduct = new Product();
                dummyProduct.setId(productId.get(i));
                tempItem.setProduct(dummyProduct); 
                tempItem.setQuantity(quantity.get(i));
                tempItem.setUnitPrice(unitPrice.get(i));
                tempItem.setNameSnapshot(nameSnapshot.get(i));
                rawItems.add(tempItem);
            }
        }
        
        try {
            Order newOrder = orderService.createManualOrder(
                branchId, customerEmail, fullName, phone, line1, city, 
                paymentMethod, note, rawItems, total
            );
            ra.addFlashAttribute("success", "Đã tạo đơn hàng mới thành công! Mã: #" + newOrder.getId());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Lỗi tạo đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders/new";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi hệ thống khi tạo đơn hàng.");
            e.printStackTrace();
            return "redirect:/admin/orders/new";
        }
        
        return "redirect:/admin/orders";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Order order = orderService.findByIdWithItems(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("allStatuses", Order.OrderStatus.values());

        return "admin/order-detail";
    }
    
    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Long id, 
                               @RequestParam OrderStatus newStatus, 
                               RedirectAttributes ra) {
        
        Order updatedOrder = orderService.updateStatus(id, newStatus);
        
        if (updatedOrder != null) {
            ra.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng #" + id + " thành công.");
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc lỗi cập nhật.");
        }
        return "redirect:/admin/orders/" + id;
    }
}