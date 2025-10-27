package com.cakestore.cakestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String ADMIN = "ADMIN";
    private final String STAFF = "STAFF";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Sử dụng strength=10 từ application.properties
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/auth/**", "/webjars/**", "/css/**", "/js/**").permitAll()
               
                .requestMatchers("/api/admin/**").hasRole(ADMIN)
                .requestMatchers("/api/staff/**").hasAnyRole(ADMIN, STAFF)
                .requestMatchers("/admin/**").hasRole(ADMIN)
                .requestMatchers("/staff/**").hasAnyRole(ADMIN, STAFF)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/default-success", true)
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