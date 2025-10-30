package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.repository.user.BranchRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class BranchController {
    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping("/api/branches")
    @ResponseBody
    public List<Map<String, Object>> listActiveBranches() {
        // chỉ trả branch đang hoạt động
        return branchRepository.findAll().stream()
                .filter(b -> b != null && b.isActive())
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    m.put("code", b.getCode());
                    m.put("name", b.getName());
                    m.put("city", b.getCity());
                    return m;
                }).toList();
    }

    @PostMapping("/branch/select")
    public String selectBranch(@RequestParam Long branchId,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Referer", required = false) String referer) {
        ResponseCookie cookie = ResponseCookie.from("BRANCH_ID", String.valueOf(branchId))
                .path("/")
                .maxAge(Duration.ofDays(365))
                .httpOnly(false) // chỉ là branchId, không nhạy cảm
                .secure(request.isSecure())
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Clear coupon để tránh đang áp mã của chi nhánh cũ
        var session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("COUPON_CODE");
            session.removeAttribute("COUPON_VALUE");
            session.removeAttribute("COUPON_MSG");
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

}
