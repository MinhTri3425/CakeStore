package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.service.UserService;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserController {

	private final UserService userService;
	private static final List<String> ROLES = List.of("customer", "staff", "admin");

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// GET /admin/users?keyword=&role=&status=all&page=0
	@GetMapping
	public String listUsers(Model model, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "") String keyword, @RequestParam(required = false) String role,
			@RequestParam(defaultValue = "active") String status) {

		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "id"));

		// Map status string -> Boolean
		Boolean statusFilter = switch (status.toLowerCase()) {
		case "active" -> Boolean.TRUE;
		case "inactive" -> Boolean.FALSE;
		default -> null; // "all" => không lọc
		};

		Page<User> userPage = (role == null || role.isBlank()) ? userService.findUsers(keyword, statusFilter, pageable)
				: userService.findUsersByRole(role, keyword, statusFilter, pageable);

		model.addAttribute("userPage", userPage);
		model.addAttribute("users", userPage.getContent());
		model.addAttribute("currentPage", userPage.getNumber());
		model.addAttribute("totalPages", userPage.getTotalPages());
		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedRole", role == null ? "" : role);
		model.addAttribute("status", status); // "all"/"active"/"inactive"
		model.addAttribute("availableRoles", ROLES);
		return "admin/users";
	}

	@GetMapping("/new")
	public String showCreateForm(Model model) {
		User u = new User();
		u.setActive(true);
		model.addAttribute("user", u);
		model.addAttribute("isEdit", false);
		model.addAttribute("availableRoles", ROLES);
		return "admin/user-form";
	}

	@GetMapping("/{id}/edit")
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
		User user = userService.findById(id);
		if (user == null) {
			ra.addFlashAttribute("error", "Tài khoản không tồn tại.");
			return "redirect:/admin/users";
		}
		model.addAttribute("user", user);
		model.addAttribute("isEdit", true);
		model.addAttribute("availableRoles", ROLES);
		return "admin/user-form";
	}

	@PostMapping
	public String saveUser(@ModelAttribute User user, @RequestParam(required = false) String rawPassword,
			RedirectAttributes ra) {
		try {
			userService.save(user, rawPassword);
			ra.addFlashAttribute("success", "Lưu tài khoản thành công!");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi khi lưu tài khoản: " + e.getMessage());
		}
		return "redirect:/admin/users";
	}

	@PostMapping("/{id}/toggle")
	public String toggleActiveStatus(@PathVariable Long id, RedirectAttributes ra) {
		User user = userService.findById(id);
		if (user != null) {
			userService.updateActiveStatus(id, !user.isActive());
			ra.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
		} else {
			ra.addFlashAttribute("error", "Tài khoản không tồn tại.");
		}
		return "redirect:/admin/users";
	}

	// Xoá = Disable
	@PostMapping("/{id}/delete")
	public String softDelete(@PathVariable Long id, RedirectAttributes ra) {
		User user = userService.findById(id);
		if (user == null) {
			ra.addFlashAttribute("error", "Tài khoản không tồn tại.");
		} else if (!user.isActive()) {
			ra.addFlashAttribute("info", "Tài khoản đã Inactive.");
		} else {
			userService.deleteById(id);
			ra.addFlashAttribute("success", "Đã vô hiệu hoá tài khoản.");
		}
		return "redirect:/admin/users";
	}
}