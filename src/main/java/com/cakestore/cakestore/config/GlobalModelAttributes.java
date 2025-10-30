package com.cakestore.cakestore.config;

import com.cakestore.cakestore.support.BranchContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
@Component
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final BranchContext branchContext;

    @ModelAttribute
    public void injectBranch(HttpServletRequest req,
            HttpSession session,
            Model model) {
        Long bid = branchContext.resolveBranchId(req, session);
        if (bid != null) {
            branchContext.saveToSession(bid, session); // sync lại session
            model.addAttribute("selectedBranchId", bid);
        }
    }
}
