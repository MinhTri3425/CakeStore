// src/main/java/com/cakestore/cakestore/controller/admin/InventoryController.java
package com.cakestore.cakestore.controller.admin;

import com.cakestore.cakestore.entity.BranchInventory;
import com.cakestore.cakestore.service.admin.BranchService;
import com.cakestore.cakestore.service.admin.InventoryService;
import com.cakestore.cakestore.service.admin.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final BranchService branchService;
    private final ProductService productService;

    public InventoryController(InventoryService inventoryService,
            BranchService branchService,
            ProductService productService) {
        this.inventoryService = inventoryService;
        this.branchService = branchService;
        this.productService = productService;
    }

    /**
     * GET /admin/inventory - Danh sách tồn kho (lọc theo chi nhánh + tìm kiếm)
     */
    @GetMapping
    public String listInventory(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "") String keyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BranchInventory> inventoryPage = inventoryService.searchInventory(branchId, keyword, pageable);

        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("selectedBranchId", branchId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("inventoryPage", inventoryPage);
        model.addAttribute("inventories", inventoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventoryPage.getTotalPages());

        return "admin/inventory";
    }

    /**
     * GET /admin/inventory/new - Form nhập kho mới
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("inventory", new BranchInventory());
        model.addAttribute("isEdit", false);
        model.addAttribute("actionText", "Nhập kho");
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("products",
                productService.findPaginatedProducts(null, null, PageRequest.of(0, 1000)).getContent());
        return "admin/inventory-form";
    }

    /**
     * GET /admin/inventory/{id}/edit - Form chỉnh sửa tồn kho
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        BranchInventory inventory = inventoryService.findById(id);
        if (inventory == null) {
            ra.addFlashAttribute("error", "Không tìm thấy tồn kho này.");
            return "redirect:/admin/inventory";
        }
        model.addAttribute("inventory", inventory);
        model.addAttribute("isEdit", true);
        model.addAttribute("actionText", "Cập nhật");
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("products",
                productService.findPaginatedProducts(null, null, PageRequest.of(0, 1000)).getContent());
        return "admin/inventory-form";
    }

    /**
     * POST /admin/inventory/update - Cập nhật hoặc tạo mới tồn kho
     */
    @PostMapping("/update")
    public String updateInventory(@RequestParam Long branchId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            RedirectAttributes ra) {
        try {
            inventoryService.updateQuantity(branchId, productId, quantity);
            ra.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/inventory/new";
        }
        return "redirect:/admin/inventory";
    }
}
