package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.OtpToken;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.user.OtpTokenRepository;
import com.cakestore.cakestore.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JavaMailSender mailSender;

    public String generateOtp(User user, OtpToken.Purpose purpose) {
        String code = String.format("%06d", new Random().nextInt(999999));
        OtpToken token = new OtpToken(user, code, purpose, LocalDateTime.now().plusMinutes(5));
        otpRepo.save(token);
        sendOtpEmail(user.getEmail(), code);
        return code;
    }

    private void sendOtpEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Mã kích hoạt tài khoản CakeStore");
        msg.setText("Mã OTP của bạn là: " + code + "\nMã có hiệu lực trong 5 phút.");
        mailSender.send(msg);
        System.out.println("📧 OTP sent to " + to + ": " + code); // in ra console để debug
    }

    public boolean verifyOtp(String email, String code, OtpToken.Purpose purpose) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Optional<OtpToken> tokenOpt =
                otpRepo.findTopByUserAndPurposeOrderByCreatedAtDesc(user, purpose);

        if (tokenOpt.isEmpty()) return false;

        OtpToken token = tokenOpt.get();
        if (token.isUsed() || token.isExpired(LocalDateTime.now())) return false;
        if (!token.getCode().equals(code)) return false;

        //  Đánh dấu là đã dùng
        token.setUsedAt(LocalDateTime.now());
        otpRepo.save(token);

        //  Kích hoạt user
        user.setActive(true);
        userRepo.save(user);

        return true;
    }
    
}
