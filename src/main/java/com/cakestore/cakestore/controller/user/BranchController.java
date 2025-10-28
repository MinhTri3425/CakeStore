package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.repository.user.BranchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class BranchController {

    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping("/api/branches")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listActiveBranches() {
        List<Map<String, Object>> out = branchRepository.findAll().stream()
                .filter(b -> b != null)
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    m.put("code", b.getCode());
                    m.put("name", b.getName());
                    m.put("city", b.getCity());
                    m.put("isActive", b.isActive());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PostMapping("/branch/select")
    public String selectBranch(@RequestParam Long branchId,
            HttpServletResponse response,
            @RequestHeader(value = "Referer", required = false) String referer) {
        Cookie ck = new Cookie("BRANCH_ID", branchId == null ? "" : String.valueOf(branchId));
        ck.setPath("/");
        ck.setMaxAge(365 * 24 * 3600); // 1 year
        response.addCookie(ck);
        return "redirect:" + (referer != null ? referer : "/");
    }
}