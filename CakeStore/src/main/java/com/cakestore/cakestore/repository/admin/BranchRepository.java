// src/main/java/com/cakestore/cakestore/repository/BranchRepository.java
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Branch;
import org.springframework.data.jpa.repository.EntityGraph; // Thêm import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Thêm import

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    // Tìm chi nhánh theo Code (để kiểm tra trùng)
    boolean existsByCode(String code);
    
    // THÊM MỚI: Lấy danh sách chi nhánh active và tải kèm thông tin manager
    @EntityGraph(attributePaths = {"manager"})
    List<Branch> findAllByIsActiveTrue();
}