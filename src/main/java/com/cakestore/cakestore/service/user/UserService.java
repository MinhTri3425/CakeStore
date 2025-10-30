package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.OtpToken;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.user.UserRepository;
import com.cakestore.cakestore.service.OtpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(String fullName, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setActive(false); // chưa kích hoạt
        u.setRole("customer");

        userRepository.save(u);

        otpService.generateOtp(u, OtpToken.Purpose.ACTIVATE);

        return u;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // public void enableUser(String email) {
    // User user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email:
    // " + email));
    // user.setActive(true);
    // userRepository.save(user);
    // }
    @Autowired
    private UserRepository userRepo;

    public void enableUser(String email) {
        Optional<User> optUser = userRepo.findByEmail(email);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setActive(true);
            userRepo.save(user);
        }
    }
}
