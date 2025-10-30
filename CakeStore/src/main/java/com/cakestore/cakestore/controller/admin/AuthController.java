// src/main/java/com/cakestore/cakestore/controller/AuthController.java
package com.cakestore.cakestore.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Đảm bảo đã import Model
import org.springframework.web.bind.annotation.GetMapping;

import com.cakestore.cakestore.service.admin.StatsService;

@Controller
public class AuthController {

    private final StatsService statsService; 

    public AuthController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/")
	public String home() {
	    return "redirect:/auth/login";
	}

    @GetMapping("/auth/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/default-success")
    public String defaultSuccess(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (request.isUserInRole("ROLE_ADMIN")) {
                return "redirect:/admin/home";
            } else if (request.isUserInRole("ROLE_STAFF")) {
                return "redirect:/staff/home"; // Chuyển đến trang chủ Staff
            }
        }
        return "redirect:/user/home";
    }

    // Trang chủ Admin (Dashboard)
    @GetMapping("/admin/home")
    public String adminHome(Model model) { 
        model.addAllAttributes(statsService.getDashboardStats());
        return "admin/home"; // Trả về dashboard của Admin
    }

    // Trang chủ Staff (Dashboard)
    @GetMapping("/staff/home")
    public String staffHome(Model model) { // Thêm Model
        // Lấy dữ liệu cho Staff
        var dashboardData = statsService.getDashboardStats();
        // Giả định 'newOrders' là số đơn hàng cần xử lý
        long ordersToProcess = (long) dashboardData.getOrDefault("newOrders", 0L);
        // Tạm thời hardcode, bạn cần thay thế bằng logic nghiệp vụ lấy hàng sắp hết
        int lowStockProducts = 5; 

        model.addAttribute("ordersToProcess", ordersToProcess);
        model.addAttribute("lowStockProducts", lowStockProducts);
        
        // Trả về template dashboard MỚI của Staff
        return "admin/staff-dashboard"; 
    }

    @GetMapping("/user/home")
    public String userHome() {
        // Trang chủ cho khách hàng
        return "user/home";
    }
}