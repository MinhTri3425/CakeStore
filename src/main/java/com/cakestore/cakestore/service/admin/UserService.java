// src/main/java/com/cakestore/cakestore/service/UserService.java
package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List; // Thêm import

public interface UserService {
	Page<User> findUsers(String keyword, Boolean status, Long branchId, Pageable pageable);
    Page<User> findUsersByRole(String role, String keyword, Boolean status, Long branchId, Pageable pageable);

    User findById(Long id);

    User save(User user, String rawPassword, String line1, String city, String district, String ward, Long branchId);

    void updateActiveStatus(Long id, boolean isActive);
    void deleteById(Long id);
    boolean isEmailExist(String email);

    User findByEmail(String email);

    // THÊM MỚI:
    List<User> findPotentialManagers(); 
}