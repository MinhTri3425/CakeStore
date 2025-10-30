// src/main/java/com/cakestore/cakestore/config/SecurityConfig.java
package com.cakestore.cakestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String ADMIN = "ADMIN";
    private final String STAFF = "STAFF";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Sử dụng strength=10
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép các tài nguyên tĩnh và trang đăng nhập, ws
                .requestMatchers("/", "/auth/**", "/webjars/**", "/css/**", "/js/**", "/ws/**").permitAll()

                // --- PHÂN QUYỀN CHO STAFF & ADMIN ---
                
                // Staff và Admin có thể vào trang dashboard của Staff
                .requestMatchers("/staff/home").hasAnyRole(ADMIN, STAFF)

                // Staff và Admin đều có thể vào các chức năng dùng chung trong /admin/
                .requestMatchers(
                    "/admin/products", "/admin/products/**", // Quản lý Sản phẩm
                    "/admin/orders", "/admin/orders/**",     // Quản lý Đơn hàng
                    "/admin/chat"                           // Chat
                ).hasAnyRole(ADMIN, STAFF)

                // Các trang Admin còn lại CHỈ DÀNH CHO ADMIN
                .requestMatchers("/admin/**").hasRole(ADMIN)

                // Các trang Staff khác (nếu có)
                .requestMatchers("/staff/**").hasAnyRole(ADMIN, STAFF)

                // Các request còn lại yêu cầu đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/default-success", true) // Vẫn trỏ về /default-success
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/perform_logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}