package com.cakestore.cakestore.config;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    // Tạo CommandLineRunner bean để chạy khi ứng dụng khởi động
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("--- Running Data Initializer ---");

            // 1. Kiểm tra và tạo tài khoản ADMIN (Chủ hệ thống)
            if (userRepository.findByEmail("admin@cakestore.com").isEmpty()) {
                User admin = new User(
                        "admin@cakestore.com",
                        passwordEncoder.encode("1234"), // Mật khẩu gốc: adminpassword
                        "System Administrator");
                admin.setRole("admin"); // Đặt role là admin
                userRepository.save(admin);
                System.out.println(">> Created Admin account: admin@cakestore.com / 1234");
            }

            // 2. Kiểm tra và tạo tài khoản STAFF (Nhân viên)
            if (userRepository.findByEmail("staff@cakestore.com").isEmpty()) {
                User staff = new User(
                        "staff@cakestore.com",
                        passwordEncoder.encode("1234"), // Mật khẩu gốc: staffpassword
                        "Cake Store Staff");
                staff.setRole("staff"); // Đặt role là staff
                userRepository.save(staff);
                System.out.println(">> Created Staff account: staff@cakestore.com / 1234");
            }

            // 3. Kiểm tra và tạo tài khoản USER (Khách hàng)
            if (userRepository.findByEmail("user@cakestore.com").isEmpty()) {
                User user = new User(
                        "user@cakestore.com",
                        passwordEncoder.encode("1234"), // Mật khẩu gốc: userpassword
                        "Regular Customer");
                user.setRole("customer"); // Đặt role là customer
                userRepository.save(user);
                System.out.println(">> Created Customer account: user@cakestore.com / 1234");
            }

            System.out.println("--- Data Initialization Complete ---");
        };
    }
}