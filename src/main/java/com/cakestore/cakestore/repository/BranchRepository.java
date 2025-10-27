package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    // Tìm chi nhánh theo Code (để kiểm tra trùng)
    boolean existsByCode(String code);
}