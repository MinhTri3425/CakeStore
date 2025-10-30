package com.cakestore.cakestore.controller.admin;

import com.cakestore.cakestore.entity.Category;
import com.cakestore.cakestore.service.admin.CategoryService;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

	private final CategoryService categoryService;

    // Danh sách + Tìm kiếm + Phân trang + Sắp xếp
    @GetMapping
    public String list(
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "status", required = false, defaultValue = "all") String status,
        @RequestParam(value = "page", required = false, defaultValue = "1") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size,
        @RequestParam(value = "sort", required = false, defaultValue = "sortOrder") String sort,
        @RequestParam(value = "dir", required = false, defaultValue = "asc") String dir,
        Model model
    ) {
        Boolean active = switch (status) {
            case "active" -> true;
            case "disabled" -> false;
            default -> null;
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(1, size), Sort.by(direction, sort));

        Page<Category> pageData = categoryService.search(q, active, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("categories", pageData.getContent()); // dùng trong bảng

        // giữ lại các tham số lọc/sort để hiển thị
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        return "admin/categories";
    }

    public CategoryController(CategoryService categoryService) {
		super();
		this.categoryService = categoryService;
	}

	@GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isEdit", false);
        model.addAttribute("parentCategories", categoryService.findAll());
        return "admin/category-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
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

    @PostMapping
    public String save(@ModelAttribute Category category, RedirectAttributes ra) {
        categoryService.save(category);
        ra.addFlashAttribute("success", "Lưu danh mục thành công!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("success", "Xoá danh mục thành công!");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}