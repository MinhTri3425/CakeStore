package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.entity.Coupon.Type;
import com.cakestore.cakestore.service.BranchService;
import com.cakestore.cakestore.service.CouponService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
public class CouponController {

	private final CouponService couponService;
	private final BranchService branchService;

	public CouponController(CouponService couponService, BranchService branchService) {
		this.couponService = couponService;
		this.branchService = branchService;
	}

	// Danh sách + tìm kiếm + lọc + phân trang + sắp xếp
	@GetMapping
	public String listCoupons(@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "type", required = false) Type type,
			@RequestParam(value = "status", required = false, defaultValue = "all") String status,
			@RequestParam(value = "branchId", required = false) Long branchId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "5") int size,
			@RequestParam(value = "sort", required = false, defaultValue = "createdAt") String sort,
			@RequestParam(value = "dir", required = false, defaultValue = "desc") String dir, Model model) {
		Boolean active = switch (status) {
		case "active" -> true;
		case "inactive" -> false;
		default -> null;
		};

		Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(direction, sort));

		Page<Coupon> pageData = couponService.search(q, type, active, branchId, pageable);

		model.addAttribute("pageData", pageData);
		model.addAttribute("coupons", pageData.getContent());
		model.addAttribute("q", q);
		model.addAttribute("type", type);
		model.addAttribute("status", status);
		model.addAttribute("branchId", branchId);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("sort", sort);
		model.addAttribute("dir", dir);

		model.addAttribute("couponTypes", Type.values());
		model.addAttribute("branches", branchService.findAllActive());

		return "admin/coupons";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		model.addAttribute("coupon", new Coupon());
		model.addAttribute("isEdit", false);
		model.addAttribute("couponTypes", Type.values());
		model.addAttribute("branches", branchService.findAllActive());
		return "admin/coupon-form";
	}

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

	@PostMapping
	public String saveCoupon(@ModelAttribute Coupon coupon,
			@RequestParam(value = "branchId", required = false) Long branchId, RedirectAttributes ra) {
		try {
			// map branchId → coupon.branch (nếu bạn dùng select branchId trong form)
			if (branchId != null) {
				var b = branchService.findById(branchId);
				coupon.setBranch(b); // có thể null nghĩa là áp dụng toàn hệ thống
			} else {
				coupon.setBranch(null);
			}
			couponService.save(coupon);
			ra.addFlashAttribute("success", "Lưu mã giảm giá thành công!");
			return "redirect:/admin/coupons";
		} catch (IllegalArgumentException ex) {
			ra.addFlashAttribute("error", ex.getMessage());
			return (coupon.getId() == null) ? "redirect:/admin/coupons/new"
					: "redirect:/admin/coupons/" + coupon.getId() + "/edit";
		}
	}

	@PostMapping("/{id}/delete")
	public String deleteCoupon(@PathVariable Long id, RedirectAttributes ra) {
		couponService.deleteById(id);
		ra.addFlashAttribute("success", "Xóa mã giảm giá thành công!");
		return "redirect:/admin/coupons";
	}
}