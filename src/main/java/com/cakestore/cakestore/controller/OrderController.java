// CakeStore/src/main/java/com/cakestore/cakestore/controller/OrderController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.OrderItem; 
import com.cakestore.cakestore.entity.Product; 
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.entity.Order.PaymentMethod;
import com.cakestore.cakestore.service.OrderService;
import com.cakestore.cakestore.service.BranchService; 
import com.cakestore.cakestore.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Đã có import Sort
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.cakestore.cakestore.dto.ProductOption;
import java.math.BigDecimal;
import java.util.ArrayList; 
import java.util.List; 

@Controller
@RequestMapping({"/admin/orders", "/staff/orders"})
public class OrderController {

    private final OrderService orderService;
    private final BranchService branchService; 
    private final ProductService productService; 

    public OrderController(OrderService orderService, BranchService branchService, ProductService productService) { 
        this.orderService = orderService;
        this.branchService = branchService;
        this.productService = productService;
    }

    /**
     * GET /admin/orders - Hiển thị danh sách đơn hàng
     */
    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) OrderStatus status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderService.findOrders(status, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentStatus", status != null ? status.name() : "");

        return "admin/orders";
    }



    /**
     * GET /admin/orders/new - Form tạo đơn hàng mới
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("branches", branchService.findAllActive());
        model.addAttribute("paymentMethods", PaymentMethod.values());

 


        // Lấy danh sách sản phẩm & ánh xạ sang DTO
        var products = productService.findPaginatedProducts(
            null, null,
            PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "name"))
        ).getContent();

        var productOptions = products.stream()
            .map(p -> new ProductOption(p.getId(), p.getName(), p.getSku(), p.getPrice()))
            .toList();

        model.addAttribute("products", productOptions);

        return "admin/order-form";
    }

    /**
     * POST /admin/orders - Xử lý tạo đơn hàng thủ công
     */
    @PostMapping
    public String createOrder(@RequestParam Long branchId,
                              @RequestParam String customerEmail,
                              @RequestParam String fullName,
                              @RequestParam String phone,
                              @RequestParam String line1,
                              @RequestParam String city,
                              @RequestParam PaymentMethod paymentMethod,
                              @RequestParam(required = false) String note,
                              
                              // Các tham số List từ Form (Item rows)
                              @RequestParam(required = false) List<Long> productId,
                              @RequestParam(required = false) List<Integer> quantity,
                              @RequestParam(required = false) List<BigDecimal> unitPrice,
                              @RequestParam(required = false) List<String> nameSnapshot,
                              
                              @RequestParam BigDecimal total,
                              RedirectAttributes ra) {

        // Validate cơ bản
        if (productId == null || productId.isEmpty()) {
            ra.addFlashAttribute("error", "Đơn hàng phải có ít nhất một sản phẩm.");
            return "redirect:/admin/orders/new";
        }
        
        // Map List raw data thành List OrderItem (để truyền vào Service)
        List<OrderItem> rawItems = new ArrayList<>();
        for (int i = 0; i < productId.size(); i++) {
            // Kiểm tra xem có phải là dòng trống không
            if (productId.get(i) != null) {
                // Tạo OrderItem tạm thời chứa data thô
                OrderItem tempItem = new OrderItem();
                
                // Khắc phục lỗi Constructor Product(Long)
                Product dummyProduct = new Product();
                dummyProduct.setId(productId.get(i));
                tempItem.setProduct(dummyProduct); 
                
                tempItem.setQuantity(quantity.get(i));
                tempItem.setUnitPrice(unitPrice.get(i));
                tempItem.setNameSnapshot(nameSnapshot.get(i));
                rawItems.add(tempItem);
            }
        }
        
        try {
            Order newOrder = orderService.createManualOrder(
                branchId, customerEmail, fullName, phone, line1, city, 
                paymentMethod, note, rawItems, total
            );
            ra.addFlashAttribute("success", "Đã tạo đơn hàng mới thành công! Mã: #" + newOrder.getId());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Lỗi tạo đơn hàng: " + e.getMessage());
            return "redirect:/admin/orders/new";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi hệ thống khi tạo đơn hàng.");
            e.printStackTrace();
            return "redirect:/admin/orders/new";
        }
        
        return "redirect:/admin/orders";
    }

    /**
     * GET /admin/orders/{id} - Xem chi tiết đơn hàng
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Order order = orderService.findByIdWithItems(id);
        if (order == null) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại.");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("allStatuses", Order.OrderStatus.values());

        return "admin/order-detail"; // đúng đường dẫn templates/admin/order-detail.html
    }

    
    

    /**
     * POST /admin/orders/{id}/update-status - Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Long id, 
                               @RequestParam OrderStatus newStatus, 
                               RedirectAttributes ra) {
        
        Order updatedOrder = orderService.updateStatus(id, newStatus);
        
        if (updatedOrder != null) {
            ra.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng #" + id + " thành công.");
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy đơn hàng hoặc lỗi cập nhật.");
        }
        return "redirect:/admin/orders/" + id;
    }
}