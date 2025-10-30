package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.Branch;
import java.util.List;

public interface BranchService {
    List<Branch> findAllActive();
    Branch findById(Long id);
    Branch save(Branch branch);
    void deleteById(Long id); // Cân nhắc đổi thành toggleActive
    boolean existsByCode(String code);
}