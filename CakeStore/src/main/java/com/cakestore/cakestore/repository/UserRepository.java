package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Phương thức tìm kiếm User theo email (dùng cho Spring Security)
    Optional<User> findByEmail(String email);

    // Phương thức kiểm tra email có tồn tại không
    boolean existsByEmail(String email);

    // Phương thức tìm kiếm User theo role (dùng cho quản lý Staff/Admin)
    long countByRole(String role);
}