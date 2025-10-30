// src/main/java/com/cakestore/cakestore/controller/admin/OrderController.java
package com.cakestore.cakestore.controller.admin;

import com.cakestore.cakestore.dto.admin.ProductOption;
import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.service.admin.BranchService;
import com.cakestore.cakestore.service.admin.OrderService;
import com.cakestore.cakestore.service.admin.ProductService;
import com.cakestore.cakestore.service.admin.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderService orderService;
    private final BranchService branchService;
    private final ProductService productService;
    private final UserService userService;

    public OrderController(OrderService orderService,
            BranchService branchService,
            ProductService productService,
            UserService userService) {
        this.orderService = orderService;
        this.branchService = branchService;
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * GET /admin/orders - Hiển thị danh sách đơn hàng (Admin + Staff)
     */
    @GetMapping
    public String listOrders(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            HttpServletRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long branchId = null; // Admin mặc định thấy tất cả

        // Nếu là STAFF → chỉ thấy đơn của chi nhánh mình
        if (request.isUserInRole("ROLE_STAFF") && userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            if (currentUser == null || currentUser.getBranch() == null) {
                model.addAttribute("orderPage", Page.empty());
                model.addAttribute("error", "Tài khoản Staff chưa được gán chi nhánh.");
                model.addAttribute("allStatuses", OrderStatus.values());
                return "admin/orders";
            }
            branchId = currentUser.getBranch().getId();
        }

        Page<Order> orderPage = orderService.findOrders(status, branchId, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status != null ? status.name() : "");
        return "admin/orders";
    }

    /**
     * GET /admin/orders/new - Form tạo đơn hàng thủ công
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("paymentMethods", PaymentMethod.values());

        var products = productService.findPaginatedProducts(
                null, null,
                PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "name"))).getContent();

        var productOptions = products.stream()
                .map(p -> new ProductOption(p.getId(), p.getName(), p.getSku(), p.getPrice()))
                .toList();

        model.addAttribute("products", productOptions);
        return "admin/order-form";
    }

    /**
     * POST /admin/orders - Lưu đơn hàng mới thủ công
     */
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

        if (productId == null || productId.isEmpty()
                || quantity == null || unitPrice == null) {
            ra.addFlashAttribute("error", "Đơn hàng phải có ít nhất một sản phẩm hợp lệ.");
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
                    paymentMethod, note, rawItems, total);
            ra.addFlashAttribute("success",
                    "Đã tạo đơn hàng mới thành công! Mã đơn #" + newOrder.getId());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Lỗi tạo đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders/new";
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi hệ thống khi tạo đơn hàng.");
            return "redirect:/admin/orders/new";
        }

        return "redirect:/admin/orders";
    }

    /**
     * GET /admin/orders/{id} - Xem chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Order order = orderService.findByIdWithItems(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/admin/orders";
        }

        // Preload tránh LazyInitializationException
        if (order.getUser() != null)
            order.getUser().getFullName();
        if (order.getBranch() != null)
            order.getBranch().getName();

        model.addAttribute("order", order);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/order-detail";
    }

    /**
     * POST /admin/orders/{id}/update-status - Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam OrderStatus newStatus,
            RedirectAttributes ra) {

        Order updatedOrder = orderService.updateStatus(id, newStatus);

        if (updatedOrder != null) {
            ra.addFlashAttribute("success",
                    "Cập nhật trạng thái đơn hàng #" + id + " thành công.");
        } else {
            ra.addFlashAttribute("error",
                    "Không tìm thấy đơn hàng hoặc lỗi khi cập nhật trạng thái.");
        }
        return "redirect:/admin/orders/" + id;
    }
}
