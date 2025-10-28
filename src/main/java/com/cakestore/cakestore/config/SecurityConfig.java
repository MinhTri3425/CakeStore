package com.cakestore.cakestore.config;

import com.cakestore.cakestore.security.CartAuthenticationSuccessHandler;
import com.cakestore.cakestore.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String ADMIN = "ADMIN";
        private static final String STAFF = "STAFF";

        private final UserDetailsService userDetailsService;
        private final CartAuthenticationSuccessHandler successHandler;

        public SecurityConfig(AuthService authService,
                        CartAuthenticationSuccessHandler successHandler) {
                this.userDetailsService = authService;
                this.successHandler = successHandler;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(10);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Dev nhanh: tắt CSRF (khi deploy production, nên bật lại)
                                .csrf(AbstractHttpConfigurer::disable)

                                .authorizeHttpRequests(auth -> auth
                                                // --- PUBLIC (guest xem tự do) ---
                                                .requestMatchers(
                                                                "/", "/home",
                                                                "/search", "/category/**", "/product/**",
                                                                "/cart/**", "/fav/**",
                                                                "/register", "/forgot",
                                                                "/api/branches",
                                                                "/branch/select",
                                                                "/css/**", "/js/**", "/images/**", "/uploads/**",
                                                                "/webjars/**",
                                                                "/h2-console/**")
                                                .permitAll()

                                                // --- API & trang nội bộ ---
                                                .requestMatchers("/api/admin/**", "/admin/**").hasRole(ADMIN)
                                                .requestMatchers("/api/staff/**", "/staff/**").hasAnyRole(ADMIN, STAFF)

                                                // --- Những đường dẫn khác (nếu có) ---
                                                .anyRequest().authenticated())

                                // --- FORM LOGIN ---
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/perform_login")
                                                .successHandler(successHandler)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(l -> l
                                                .logoutUrl("/perform_logout")
                                                .logoutSuccessUrl("/")
                                                .permitAll())
                                .userDetailsService(userDetailsService)

                                // Cho H2 console (nếu đang dùng)
                                .headers(h -> h.frameOptions(f -> f.sameOrigin()));

                return http.build();
        }
}