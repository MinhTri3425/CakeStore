package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.service.UserService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    // Danh sách Role có sẵn
    private static final List<String> ROLES = List.of("customer", "staff", "admin", "shipper");

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /admin/users - Hiển thị danh sách Users & Staff
     */
    @GetMapping
    public String listUsers(Model model, 
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "") String keyword) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.findUsers(keyword, pageable);
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        // TODO: Tạo template admin/users.html
        return "admin/users"; 
    }

    /**
     * GET /admin/users/new - Form tạo User/Staff mới
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        model.addAttribute("availableRoles", ROLES);
        // TODO: Tạo template admin/user-form.html
        return "admin/user-form";
    }

    /**
     * GET /admin/users/{id}/edit - Form sửa User/Staff
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        User user = userService.findById(id);
        if (user == null) {
            ra.addFlashAttribute("error", "Tài khoản không tồn tại.");
            return "redirect:/admin/users";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("isEdit", true);
        model.addAttribute("availableRoles", ROLES);
        return "admin/user-form";
    }

    /**
     * POST /admin/users - Xử lý lưu User/Staff
     */
    @PostMapping
    public String saveUser(@ModelAttribute User user, 
                           @RequestParam(required = false) String rawPassword, 
                           RedirectAttributes ra) {
        
        // TODO: Thêm logic kiểm tra: chỉ Admin mới có thể tạo/sửa Admin/Staff
        
        try {
            userService.save(user, rawPassword);
            ra.addFlashAttribute("success", "Lưu tài khoản thành công!");
        } catch (Exception e) {
            // Xử lý lỗi trùng email, v.v.
            ra.addFlashAttribute("error", "Lỗi khi lưu tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**    * POST /admin/users/{id}/toggle - Bật/tắt trạng thái Active
     */
    @PostMapping("/{id}/toggle")
    public String toggleActiveStatus(@PathVariable Long id, RedirectAttributes ra) {
        User user = userService.findById(id);
        if (user != null) {
            userService.updateActiveStatus(id, !user.isActive());
            ra.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        }
        return "redirect:/admin/users";
    }
}