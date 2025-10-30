package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.security.CustomUserDetails;
import com.cakestore.cakestore.service.user.AccountService;
import com.cakestore.cakestore.viewmodel.AccountProfileVM;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String viewAccount(
            Model model,
            Authentication auth,
            @RequestParam(value = "msg", required = false) String msg,
            @RequestParam(value = "err", required = false) String err) {

        Long userId = resolveUserId(auth);

        AccountProfileVM profile = accountService.getProfile(userId);

        model.addAttribute("profile", profile);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);

        return "account/profile";
    }

    /**
     * Cập nhật thông tin cá nhân (fullName, phone).
     * KHÔNG còn động tới địa chỉ nữa.
     * Địa chỉ chuyển sang /account/address.
     */
    @PostMapping("/profile")
    public String updateProfile(
            Authentication auth,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone) {
        Long userId = resolveUserId(auth);

        try {
            // chỉ update user info
            accountService.updateProfile(userId, fullName, phone);

            // không còn upsertDefaultAddress() ở đây nữa
            return "redirect:/account?msg=updated";
        } catch (Exception e) {
            return "redirect:/account?err=update_failed";
        }
    }

    /**
     * Đổi mật khẩu.
     * Giữ nguyên flow cũ.
     */
    @PostMapping("/password")
    public String changePassword(
            Authentication auth,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {
        Long userId = resolveUserId(auth);

        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/account?err=pw_mismatch";
        }

        try {
            accountService.changePassword(userId, oldPassword, newPassword);
            return "redirect:/account?msg=pw_ok";
        } catch (IllegalArgumentException wrongOld) {
            if ("WRONG_OLD_PASSWORD".equals(wrongOld.getMessage())) {
                return "redirect:/account?err=pw_incorrect_old";
            }
            return "redirect:/account?err=pw_failed";
        } catch (Exception other) {
            return "redirect:/account?err=pw_failed";
        }
    }

    private Long resolveUserId(Authentication auth) {
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // nếu vì lý do gì đó principal không phải CustomUserDetails
        throw new IllegalStateException("Cannot resolve user id from Authentication principal");
    }
}
