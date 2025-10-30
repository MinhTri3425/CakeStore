// src/main/java/com/cakestore/cakestore/controller/BranchController.java
package com.cakestore.cakestore.controller.admin;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.entity.User; // THÊM IMPORT
import com.cakestore.cakestore.service.admin.BranchService;
import com.cakestore.cakestore.service.admin.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branches")
public class BranchController {

    private final BranchService branchService;
    private final UserService userService; // THÊM MỚI

    // CẬP NHẬT CONSTRUCTOR
    public BranchController(BranchService branchService, UserService userService) {
        this.branchService = branchService;
        this.userService = userService;
    }

    // GET /admin/branches - Hiển thị danh sách
    @GetMapping
    public String listBranches(Model model) {
        model.addAttribute("branches", branchService.findAllActive());
        return "admin/branches"; 
    }

    // GET /admin/branches/new - Form tạo mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("branch", new Branch());
        model.addAttribute("isEdit", false);
        // THÊM MỚI: Lấy danh sách manager tiềm năng
        model.addAttribute("managers", userService.findPotentialManagers());
        return "admin/branch-form"; 
    }

    // GET /admin/branches/{id}/edit - Form sửa
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Branch branch = branchService.findById(id);
        if (branch == null) {
            ra.addFlashAttribute("error", "Chi nhánh không tồn tại.");
            return "redirect:/admin/branches";
        }
        model.addAttribute("branch", branch);
        model.addAttribute("isEdit", true);
        // THÊM MỚI: Lấy danh sách manager tiềm năng
        model.addAttribute("managers", userService.findPotentialManagers());
        return "admin/branch-form";
    }

    // POST /admin/branches - Lưu chi nhánh
    @PostMapping
    public String saveBranch(@ModelAttribute Branch branch,
                             @RequestParam(required = false) Long managerId, // THÊM MỚI
                             RedirectAttributes ra) {
        
        // THÊM MỚI: Xử lý gán Manager
        try {
            if (managerId != null) {
                User manager = userService.findById(managerId);
                // Cần kiểm tra xem manager này đã quản lý chi nhánh khác chưa
                // (Vì chúng ta đặt unique=true, DB sẽ tự báo lỗi nếu vi phạm)
                branch.setManager(manager);
            } else {
                branch.setManager(null);
            }

            branchService.save(branch);
            ra.addFlashAttribute("success", "Lưu chi nhánh thành công!");
        } catch (Exception e) {
             // Bắt lỗi nếu gán manager đã quản lý chi nhánh khác
             // Lỗi ConstraintViolationException thường bị bọc trong các exception khác
            if (e.getMessage() != null && e.getMessage().contains("UQ_Branches_ManagerId")) { // Giả định tên constraint
                 ra.addFlashAttribute("error", "Lỗi: Người dùng này đã là quản lý của một chi nhánh khác.");
            } else {
                 ra.addFlashAttribute("error", "Lỗi khi lưu: " + e.getMessage());
            }
            return (branch.getId() == null) ? "redirect:/admin/branches/new" 
                                            : "redirect:/admin/branches/" + branch.getId() + "/edit";
        }
        
        return "redirect:/admin/branches";
    }

    @PostMapping("/{id}/delete")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes ra) {
        try {
            branchService.deleteById(id);
            ra.addFlashAttribute("success", "Xóa chi nhánh thành công!");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa chi nhánh.");
        }
        return "redirect:/admin/branches";
    }

}