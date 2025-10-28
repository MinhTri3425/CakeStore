package com.cakestore.cakestore.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
	@GetMapping("/")
	public String home() {
	    return "redirect:/auth/login";
	}
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
            // Kiểm tra quyền (Role trong Spring Security là ROLE_ADMIN, ROLE_STAFF, ROLE_CUSTOMER)
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
    public String adminHome() {
        // TODO: Tạo template admin/home.html
        return "admin/home"; 
    }

    @GetMapping("/staff/home")
    public String staffHome() {
        // TODO: Tạo template staff/home.html
        return "staff/home"; 
    }

    @GetMapping("/user/home")
    public String userHome() {
        return "user/home"; // Trả về template user/home.html
    }
}