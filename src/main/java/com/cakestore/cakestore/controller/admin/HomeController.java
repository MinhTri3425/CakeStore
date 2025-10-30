//// CakeStore/src/main/java/com/cakestore/cakestore/controller/HomeController.java (CHỈNH SỬA)
//package com.cakestore.cakestore.controller;
//
//import com.cakestore.cakestore.service.StatsService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//public class HomeController {
//
//    private final StatsService statsService;
//
//    // Inject StatsService
//    public HomeController(StatsService statsService) {
//        this.statsService = statsService;
//    }
//
//    // Hiển thị trang đăng nhập
//    @GetMapping("/auth/login")
//    public String login() {
//        return "auth/login"; 
//    }
//
//    /**
//     * Chuyển hướng dựa trên Role sau khi đăng nhập thành công
//     */
//    @GetMapping("/default-success")
//    public String defaultSuccess(jakarta.servlet.http.HttpServletRequest request) {
//        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
//        
//        if (auth != null && auth.isAuthenticated()) {
//            if (request.isUserInRole("ROLE_ADMIN")) {
//                return "redirect:/admin/home";
//            } else if (request.isUserInRole("ROLE_STAFF")) {
//                return "redirect:/staff/home";
//            }
//        }
//        
//        return "redirect:/user/home"; 
//    }
//    
//    // Trang chủ Admin (Dashboard)
//    @GetMapping("/admin/home")
//    public String adminHome(Model model) {
//        // Lấy dữ liệu thống kê cho Dashboard
//        model.addAllAttributes(statsService.getDashboardStats());
//        return "admin/home"; 
//    }
//
//    // Trang chủ Staff
//    @GetMapping("/staff/home")
//    public String staffHome() {
//        return "staff/home"; 
//    }
//
//    @GetMapping("/user/home")
//    public String userHome() {
//        return "user/home"; 
//    }
//}


