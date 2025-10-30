package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.user.UserRepository;
import com.cakestore.cakestore.security.CustomUserDetails;

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

        // check active / locked
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User is inactive/locked: " + email);
        }

        // THIS is the fix:
        // trả về CustomUserDetails thay vì User.withUsername(...)
        return new CustomUserDetails(user);
    }

}