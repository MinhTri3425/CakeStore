package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.repository.user.BranchRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BranchContextService {

    private final BranchRepository branchRepository;

    public BranchContextService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    // Lấy branch đang chọn:
    // 1. session.BRANCH_ID
    // 2. cookie BRANCH_ID
    // 3. fallback: branch active đầu tiên
    public Optional<Branch> resolveCurrentBranch(HttpSession session, HttpServletRequest req) {
        Long chosenId = null;

        // try session first
        if (session != null) {
            Object s = session.getAttribute("BRANCH_ID");
            if (s != null) {
                try {
                    chosenId = Long.valueOf(String.valueOf(s));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // then cookie
        if (chosenId == null && req != null && req.getCookies() != null) {
            for (Cookie ck : req.getCookies()) {
                if ("BRANCH_ID".equals(ck.getName())) {
                    try {
                        chosenId = Long.valueOf(ck.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }
        }

        // if we got an id -> load that branch
        if (chosenId != null) {
            return branchRepository.findById(chosenId);
        }

        // fallback branch active hoặc branch đầu tiên
        return branchRepository.findTopByIsActiveTrueOrderByIdAsc()
                .or(() -> branchRepository.findTopByOrderByIdAsc());
    }
}
