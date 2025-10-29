package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.repository.BranchRepository;
import com.cakestore.cakestore.service.BranchService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public List<Branch> findAllActive() {
        return branchRepository.findAll().stream()
                .filter(Branch::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public Branch findById(Long id) {
        return branchRepository.findById(id).orElse(null);
    }

    @Override
    public Branch save(Branch branch) {
        // Cần thêm validation logic (ví dụ: code không trùng)
        return branchRepository.save(branch);
    }

    @Override
    public void deleteById(Long id) {
        // Nên cân nhắc đổi sang set isActive = false thay vì xóa cứng
        branchRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return branchRepository.existsByCode(code);
    }
}