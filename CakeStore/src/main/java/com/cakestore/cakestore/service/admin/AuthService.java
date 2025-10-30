// src/main/java/com/cakestore/cakestore/service/AuthService.java
package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.admin.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring Security sẽ gọi hàm này khi người dùng đăng nhập
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Kiểm tra tài khoản có bị khóa không
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa: " + email);
        }
        
        // Trả về UserDetails chuẩn của Spring
        // Spring sẽ tự động dùng PasswordEncoder để so sánh mật khẩu
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(), // Đây là mật khẩu đã mã hóa
                getAuthorities(user.getRole()) // Đây là quyền
        );
    }

    // Chuyển Role (String) thành GrantedAuthority
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // Role trong DB là 'customer', 'staff', 'admin'
        // Spring Security yêu cầu format "ROLE_..."
        Set<String> roles = Set.of(role.toUpperCase());

        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r)) // QUAN TRỌNG: Thêm tiền tố "ROLE_"
                .collect(Collectors.toList());
    }
}