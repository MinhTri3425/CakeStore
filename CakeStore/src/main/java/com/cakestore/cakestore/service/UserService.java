package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
    // Tìm kiếm phân trang tất cả User (Customer, Staff, Admin)
    Page<User> findUsers(String keyword, Pageable pageable);
    
    // Tìm kiếm User theo Role (Chỉ dùng cho Staff/Admin)
    Page<User> findUsersByRole(String role, String keyword, Pageable pageable);

    User findById(Long id);
    
    // Lưu hoặc cập nhật User/Staff, mã hóa mật khẩu trước khi lưu
    User save(User user, String rawPassword); 
    
    // Đổi trạng thái (Active/Inactive)
    void updateActiveStatus(Long id, boolean isActive);
    
    // Xóa (thường là disable thay vì xóa hẳn trong môi trường thực)
    void deleteById(Long id);

    boolean isEmailExist(String email);
}