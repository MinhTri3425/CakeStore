// src/main/java/com/cakestore/cakestore/service/impl/BranchServiceImpl.java
package com.cakestore.cakestore.service.admin.impl;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.repository.admin.BranchRepository;
import com.cakestore.cakestore.service.admin.BranchService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Thêm import
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    @Transactional(readOnly = true) // Thêm Transactional
    public List<Branch> findAllActive() {
        // CẬP NHẬT: Gọi phương thức mới để fetch manager
        return branchRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true) // Thêm Transactional
    public Branch findById(Long id) {
        // CẬP NHẬT: Fetch cả manager khi sửa
        return branchRepository.findById(id).map(b -> {
            if (b.getManager() != null) {
                b.getManager().getFullName(); // Tải manager (chỉ cần gọi 1 hàm)
            }
            return b;
        }).orElse(null);
    }

    @Override
    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    @Override
    public void deleteById(Long id) {
        branchRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return branchRepository.existsByCode(code);
    }
}