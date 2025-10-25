package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.entity.Coupon.Type; // Import Enum Type
import com.cakestore.cakestore.service.BranchService;
import com.cakestore.cakestore.service.CouponService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
public class CouponController {

    private final CouponService couponService;
    private final BranchService branchService; // Cần để chọn chi nhánh áp dụng

    public CouponController(CouponService couponService, BranchService branchService) {
        this.couponService = couponService;
        this.branchService = branchService;
    }

    // GET /admin/coupons - Hiển thị danh sách
    @GetMapping
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.findAll());
        // TODO: Tạo template admin/coupons.html
        return "admin/coupons"; 
    }

    // GET /admin/coupons/new - Form tạo mới
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        model.addAttribute("isEdit", false);
        model.addAttribute("couponTypes", Type.values()); // Gửi Enum Type sang view
        model.addAttribute("branches", branchService.findAllActive()); // Gửi danh sách chi nhánh
        // TODO: Tạo template admin/coupon-form.html
        return "admin/coupon-form"; 
    }

    // GET /admin/coupons/{id}/edit - Form sửa
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Coupon coupon = couponService.findById(id);
        if (coupon == null) {
            ra.addFlashAttribute("error", "Mã giảm giá không tồn tại.");
            return "redirect:/admin/coupons";
        }
        model.addAttribute("coupon", coupon);
        model.addAttribute("isEdit", true);
        model.addAttribute("couponTypes", Type.values());
        model.addAttribute("branches", branchService.findAllActive());
        return "admin/coupon-form";
    }

    // POST /admin/coupons - Lưu mã giảm giá
    @PostMapping
    public String saveCoupon(@ModelAttribute Coupon coupon, RedirectAttributes ra) {
        // TODO: Thêm validation
        couponService.save(coupon);
        ra.addFlashAttribute("success", "Lưu mã giảm giá thành công!");
        return "redirect:/admin/coupons";
    }

    // POST /admin/coupons/{id}/delete - Xóa mã giảm giá
    @PostMapping("/{id}/delete")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes ra) {
        couponService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa mã giảm giá thành công!");
        return "redirect:/admin/coupons";
    }
}