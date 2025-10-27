package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
	Page<User> findUsers(String keyword, Boolean status, Pageable pageable);
    Page<User> findUsersByRole(String role, String keyword, Boolean status, Pageable pageable);

    User findById(Long id);

    // Lưu/cập nhật (mã hóa nếu có rawPassword)
    User save(User user, String rawPassword);

    // Bật/tắt active
    void updateActiveStatus(Long id, boolean isActive);

    // Soft delete = disable
    void deleteById(Long id);

    boolean isEmailExist(String email);
}