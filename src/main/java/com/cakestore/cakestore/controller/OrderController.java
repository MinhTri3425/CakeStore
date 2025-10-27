package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/admin/orders", "/staff/orders"})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * GET /admin/orders - Hiển thị danh sách đơn hàng
     */
    @GetMapping
    public String listOrders(Model model, 
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("orderPage", orderService.findOrders(status, pageable));
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status);

        // TODO: Tạo template admin/orders.html
        return "admin/orders"; 
    }

    /**
     * GET /admin/orders/{id} - Xem chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Order order = orderService.findById(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/admin/orders";
        }
        
        model.addAttribute("order", order);
        model.addAttribute("allStatuses", OrderStatus.values());
        // TODO: Tạo template admin/order-detail.html
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
            ra.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng #" + id + " thành công.");
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc lỗi cập nhật.");
        }
        return "redirect:/admin/orders/" + id;
    }
}