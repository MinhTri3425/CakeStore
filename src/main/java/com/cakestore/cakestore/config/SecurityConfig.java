package com.cakestore.cakestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // TẮT CSRF cho đơn giản (form POST không cần token).
                // Khi bạn làm đăng nhập/đăng ký thật, cân nhắc bật lại và thêm token CSRF vào
                // form.
                .csrf(csrf -> csrf.disable())

                // Cho phép TẤT CẢ endpoints (không yêu cầu đăng nhập)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/**",
                                "/h2-console/**" // nếu bạn dùng H2 dev
                        ).permitAll()
                        .anyRequest().permitAll())

                // H2 console (tuỳ bạn có dùng hay không)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // Nếu sau này bạn thêm JWT filter:
        // http.addFilterBefore(jwtAuthFilter,
        // UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Để sẵn cho tương lai (mã hoá mật khẩu khi bạn làm đăng ký/đăng nhập)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
