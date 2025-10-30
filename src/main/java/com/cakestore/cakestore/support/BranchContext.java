package com.cakestore.cakestore.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class BranchContext {
    public static final String SESSION_BRANCH_KEY = "SELECTED_BRANCH_ID";

    public Long resolveBranchId(HttpServletRequest req, HttpSession session) {
        // 1) ưu tiên cookie
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("BRANCH_ID".equals(c.getName())) {
                    try {
                        return Long.valueOf(c.getValue());
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        // 2) fallback session
        Object v = session.getAttribute(SESSION_BRANCH_KEY);
        if (v instanceof Long l)
            return l;
        if (v instanceof String s)
            try {
                return Long.valueOf(s);
            } catch (Exception ignored) {
            }
        return null;
    }

    public void saveToSession(Long branchId, HttpSession session) {
        if (branchId != null)
            session.setAttribute(SESSION_BRANCH_KEY, branchId);
    }
}
