// CakeStore/src/main/java/com/cakestore/cakestore/controller/ProductController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.ProductImage; // Thêm import ProductImage
import com.cakestore.cakestore.service.CategoryService;
import com.cakestore.cakestore.service.ProductService;
import com.cakestore.cakestore.service.CloudinaryService; // Thêm import CloudinaryService
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Thêm import MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException; // Thêm import IOException
import java.util.LinkedHashSet; // Thêm import LinkedHashSet
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService; // THÊM CLOUDINARY SERVICE

    public ProductController(ProductService productService, CategoryService categoryService, CloudinaryService cloudinaryService) { // CẬP NHẬT CONSTRUCTOR
        this.productService = productService;
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
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
    public String saveProduct(@ModelAttribute Product product, 
                              @RequestParam("productImages") MultipartFile[] productImages, // THÊM PARAMETER CHO UPLOAD FILE
                              RedirectAttributes ra) {
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

        // [Tối gọn] Logic tự động điền Slug và dọn dẹp các trường bỏ qua
        if (product.getId() == null || product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        } else {
             product.setSlug(generateSlug(product.getName())); 
        }
        product.setCompareAtPrice(null); 
        if (product.getVariants() != null) {
             product.getVariants().clear();
        }
        
        try {
            // Xử lý Upload Ảnh Mới (Chỉ xử lý khi tạo mới hoặc thêm ảnh mới, không xử lý xóa/cập nhật ảnh cũ)
            if (productImages != null && productImages.length > 0) {
                 // Đảm bảo collection images được khởi tạo (cho trường hợp new Product)
                 if (product.getImages() == null) {
                    product.setImages(new LinkedHashSet<>()); 
                 }
                
                for (MultipartFile file : productImages) {
                    if (!file.isEmpty()) {
                        String imageUrl = cloudinaryService.uploadFile(file);
                        if (imageUrl != null) {
                            // Tạo ProductImage mới và thêm vào Product
                            ProductImage img = new ProductImage(product, imageUrl, product.getName(), product.getImages().size());
                            product.addImage(img);
                        }
                    }
                }
            }

            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Lưu sản phẩm thành công!");
            return "redirect:/admin/products";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "SKU đã tồn tại. Vui lòng đổi giá trị khác.");
            return (product.getId() == null) ? "redirect:/admin/products/new"
                                             : "redirect:/admin/products/" + product.getId() + "/edit";
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage());
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

    /**
     * POST /admin/products/{id}/toggle - Xử lý thay đổi trạng thái sản phẩm (1: Active, 0: Hidden)
     */
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        Product product = productService.getProductById(id);
        if (product == null) {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại.");
        } else {
            // Logic toggle: 1 -> 0 (Hidden), anything else -> 1 (Active)
            Integer newStatus = (product.getStatus() == 1) ? 0 : 1;
            product.setStatus(newStatus);
            
            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Cập nhật trạng thái sản phẩm thành công!");
        }
        return "redirect:/admin/products";
    }

    /**
     * Helper: Tạo slug (tương tự logic trong CategoryServiceImpl)
     */
    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) return "";
        // Chuyển về chữ thường, thay thế khoảng trắng bằng dấu gạch ngang, loại bỏ ký tự đặc biệt
        return name.toLowerCase()
                   .replaceAll("\\s+", "-")
                   .replaceAll("[^a-z0-9-]", "");
    }
}