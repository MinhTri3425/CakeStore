package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Category;
import com.cakestore.cakestore.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Hiển thị danh sách danh mục
    @GetMapping
    public String showCategoryList(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories"; 
    }

    // Hiển thị form tạo danh mục mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        // Lấy danh sách các danh mục khác (trừ chính nó nếu là edit) làm danh mục cha
        model.addAttribute("parentCategories", categoryService.findAll()); 
        return "admin/category-form"; 
    }

    // Hiển thị form sửa danh mục
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Category category = categoryService.findById(id);
        if (category == null) {
            ra.addFlashAttribute("error", "Danh mục không tồn tại.");
            return "redirect:/admin/categories";
        }
        
        model.addAttribute("category", category);
        model.addAttribute("isEdit", true);
        model.addAttribute("parentCategories", categoryService.findAll());
        return "admin/category-form";
    }

    // Xử lý lưu (tạo mới hoặc cập nhật) danh mục
    @PostMapping
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        categoryService.save(category);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    // Xử lý xóa danh mục
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa danh mục thành công!");
        return "redirect:/admin/categories";
    }
}