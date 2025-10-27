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
    public Page<User> findUsers(String keyword, Boolean status, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.searchAllWithStatus(kw, status, pageable);
    }

    @Override
    public Page<User> findUsersByRole(String role, String keyword, Boolean status, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        return userRepository.searchByRoleWithStatus(role, kw, status, pageable);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public User save(User user, String rawPassword) {
        if (user.getId() == null) {
            if (rawPassword == null || rawPassword.isBlank()) {
                throw new IllegalArgumentException("Mật khẩu không được để trống khi tạo mới.");
            }
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            if (!user.isActive()) user.setActive(true); // tạo mới mặc định Active
        } else {
            if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            } else {
                User existed = userRepository.findById(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
                user.setPasswordHash(existed.getPasswordHash());
            }
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateActiveStatus(Long id, boolean isActive) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(isActive);
            userRepository.save(u);
        });
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Soft delete
        updateActiveStatus(id, false);
    }

    @Override
    public boolean isEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }
}