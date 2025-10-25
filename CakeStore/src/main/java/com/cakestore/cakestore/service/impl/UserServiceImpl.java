package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<User> findUsers(String keyword, Pageable pageable) {
        // Cần viết custom query trong UserRepository để search theo email/fullname
        return userRepository.findAll(pageable); // Tạm thời dùng findAll
    }
    
    @Override
    public Page<User> findUsersByRole(String role, String keyword, Pageable pageable) {
        // TODO: Cần viết custom query trong UserRepository để search theo Role và keyword
        return userRepository.findAll(pageable); // Tạm thời dùng findAll
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public User save(User user, String rawPassword) {
        // Chỉ mã hóa nếu là tạo mới hoặc có nhập mật khẩu mới
        if (user.getId() == null || (rawPassword != null && !rawPassword.isEmpty())) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else if (user.getId() != null) {
            // Đảm bảo không ghi đè passwordHash nếu không có rawPassword (tránh mất hash)
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser != null) {
                user.setPasswordHash(existingUser.getPasswordHash());
            }
        }
        // UpdatedAt được DB quản lý
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateActiveStatus(Long id, boolean isActive) {
        userRepository.findById(id).ifPresent(user -> {
            user.setActive(isActive);
            // UpdatedAt được DB quản lý
            userRepository.save(user);
        });
    }

    @Override
    public void deleteById(Long id) {
        // Thường chỉ set isActive=false
        userRepository.deleteById(id);
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }
}