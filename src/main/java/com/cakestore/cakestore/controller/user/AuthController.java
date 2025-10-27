package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.OtpToken;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.service.user.OtpService;
import com.cakestore.cakestore.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService; 

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
                                  @RequestParam("password") String password,
                                  Model model) {
        try {
            // 1. Đăng ký user
            User u = userService.register(user.getFullName(), user.getEmail(), password);

            // 2. Gửi OTP kích hoạt
            otpService.generateOtp(u, OtpToken.Purpose.ACTIVATE);

            // 3. Trả về trang verify
            model.addAttribute("email", user.getEmail());
            return "auth/verify"; // trang nhập mã OTP
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        }
    }
    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam("email") String email,
                         @RequestParam("otp") String otp,
                         Model model) {
    	boolean isValid = otpService.verifyOtp(email, otp, OtpToken.Purpose.ACTIVATE);

        if (isValid) {
            // Hiển thị thông báo ngay tại trang verify
            model.addAttribute("success", "Xác thực tài khoản thành công!");
        } else {
            model.addAttribute("error", " Mã OTP không đúng hoặc đã hết hạn!");
        }

        model.addAttribute("email", email);
        return "auth/verify"; // Không redirect đi đâu
    }




}
