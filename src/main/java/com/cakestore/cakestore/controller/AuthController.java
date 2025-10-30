package com.cakestore.cakestore.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.cakestore.cakestore.entity.OtpToken;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.service.OtpService;
import com.cakestore.cakestore.service.user.UserService;
import com.cakestore.cakestore.service.admin.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// import com.cakestore.cakestore.entity.OtpToken;
// import com.cakestore.cakestore.service.OtpService;

@Controller
public class AuthController {
    private final StatsService statsService;

    public AuthController(StatsService statsService) {
        this.statsService = statsService;
    }

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    // Hiển thị trang đăng nhập
    @GetMapping("/auth/login")
    public String login() {
        return "auth/login"; // Trả về template "auth/login.html"
    }

    /**
     * Chuyển hướng dựa trên Role sau khi đăng nhập thành công
     */
    @GetMapping("/default-success")
    public String defaultSuccess(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            // Kiểm tra quyền (Role trong Spring Security là ROLE_ADMIN, ROLE_STAFF,
            // ROLE_CUSTOMER)
            if (request.isUserInRole("ROLE_ADMIN")) {
                return "redirect:/admin/home";
            } else if (request.isUserInRole("ROLE_STAFF")) {
                return "redirect:/staff/home";
            }
        }

        // Mặc định cho User thường (Customer) hoặc trường hợp khác
        return "redirect:/user/home"; // Trả về template user/home.html
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        model.addAllAttributes(statsService.getDashboardStats());
        return "admin/home"; // Trả về dashboard của Admin
    }

    @GetMapping("/staff/home")
    public String staffHome(Model model) {
        // Lấy dữ liệu cho Staff
        var dashboardData = statsService.getDashboardStats();
        long ordersToProcess = (long) dashboardData.getOrDefault("newOrders", 0L);
        int lowStockProducts = 5;

        model.addAttribute("ordersToProcess", ordersToProcess);
        model.addAttribute("lowStockProducts", lowStockProducts);

        model.addAttribute("view", "admin/staff-dashboard");
        return "admin/admin_layout"; // Vẫn trả về layout cha
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
            @RequestParam("password") String password,
            Model model) {
        try {
            // 1. Đăng ký user
            User u = userService.register(user.getFullName(), user.getEmail(), password);

            // 2. Gửi OTP kích hoạt
            otpService.generateOtp(u, OtpToken.Purpose.ACTIVATE);

            // 3. Trả về trang verify
            model.addAttribute("email", user.getEmail());
            return "auth/verify"; // trang nhập mã OTP
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email,
            @RequestParam("otp") String otp,
            Model model) {
        boolean isValid = otpService.verifyOtp(email, otp, OtpToken.Purpose.ACTIVATE);

        if (isValid) {
            // Hiển thị thông báo ngay tại trang verify
            model.addAttribute("success", "Xác thực tài khoản thành công!");
        } else {
            model.addAttribute("error", " Mã OTP không đúng hoặc đã hết hạn!");
        }

        model.addAttribute("email", email);
        return "auth/verify"; // Không redirect đi đâu
    }

    // @GetMapping("/user/home")
    // public String userHome() {
    // return "user/home"; // Trả về template user/home.html
    // }
}