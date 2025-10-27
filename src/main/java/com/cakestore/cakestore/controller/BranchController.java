package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.service.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    // GET /admin/branches - Hiển thị danh sách
    @GetMapping
    public String listBranches(Model model) {
        model.addAttribute("branches", branchService.findAllActive());
        // TODO: Tạo template admin/branches.html
        return "admin/branches"; 
    }

    // GET /admin/branches/new - Form tạo mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("branch", new Branch());
        model.addAttribute("isEdit", false);
        // TODO: Tạo template admin/branch-form.html
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
        return "admin/branch-form";
    }

    // POST /admin/branches - Lưu chi nhánh
    @PostMapping
    public String saveBranch(@ModelAttribute Branch branch, RedirectAttributes ra) {
        // TODO: Thêm validation (code unique)
        branchService.save(branch);
        ra.addFlashAttribute("success", "Lưu chi nhánh thành công!");
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