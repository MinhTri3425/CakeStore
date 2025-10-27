package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.service.CategoryService;
import com.cakestore.cakestore.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    /**
     * GET /admin/products - Hiển thị danh sách sản phẩm (CRUD Read, Tìm kiếm, Lọc, Phân trang)
     */
    @GetMapping
    public String showProductList(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long categoryId) {

        // Phân trang và Lọc
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.findPaginatedProducts(keyword, categoryId, pageable);
        List<Product> products = productPage.getContent();

        // Load danh mục cho bộ lọc
        List<com.cakestore.cakestore.entity.Category> categories = categoryService.findAll();

        // Đưa dữ liệu vào Model
        model.addAttribute("products", products);
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);

        return "admin/products"; // Trả về template admin/products.html
    }

    /**
     * GET /admin/products/new - Hiển thị form tạo sản phẩm (CRUD Create)
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/products-form"; // Cần tạo template này
    }

    /**
     * GET /admin/products/{id}/edit - Hiển thị form sửa sản phẩm (CRUD Update)
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Product product = productService.getProductForEdit(id); // dùng hàm mới đã fetch images
        if (product == null) {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/products-form";
    }

    /**
     * POST /admin/products - Xử lý lưu (tạo mới hoặc cập nhật) sản phẩm
     */
    @PostMapping
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes ra) {
    	// bắt buộc chọn category hợp lệ
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn danh mục hợp lệ cho sản phẩm.");
            return (product.getId() == null) ? "redirect:/admin/products/new"
                                             : "redirect:/admin/products/" + product.getId() + "/edit";
        }
        var cat = categoryService.findById(product.getCategory().getId());
        if (cat == null) {
            ra.addFlashAttribute("error", "Danh mục đã chọn không tồn tại.");
            return (product.getId() == null) ? "redirect:/admin/products/new"
                                             : "redirect:/admin/products/" + product.getId() + "/edit";
        }
        product.setCategory(cat);


        try {
            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // vi phạm unique SKU/Slug...
            ra.addFlashAttribute("error", "SKU hoặc Slug đã tồn tại. Vui lòng đổi giá trị khác.");
            return (product.getId() == null) ? "redirect:/admin/products/new"
                                             : "redirect:/admin/products/" + product.getId() + "/edit";
        }
    }

    /**
     * POST /admin/products/{id}/delete - Xử lý xóa sản phẩm
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        // Cần xử lý logic: nếu sản phẩm có trong đơn hàng/tồn kho thì không được xóa vĩnh viễn (chỉ set Status=0)
        productService.deleteProduct(id);
        ra.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        return "redirect:/admin/products";
    }
}