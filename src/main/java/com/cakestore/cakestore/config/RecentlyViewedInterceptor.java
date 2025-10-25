// config/RecentlyViewedInterceptor.java
package com.cakestore.cakestore.config;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecentlyViewedInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object h) {
        String uri = req.getRequestURI();
        if (uri.startsWith("/product/")) {
            String id = uri.substring("/product/".length());
            try {
                long pid = Long.parseLong(id);
                List<String> ids = new ArrayList<>();
                if (req.getCookies() != null) {
                    for (var c : req.getCookies())
                        if ("RV".equals(c.getName())) {
                            ids.addAll(
                                    Arrays.asList(URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8).split(",")));
                        }
                }
                ids.remove(String.valueOf(pid)); // dedup
                ids.add(0, String.valueOf(pid)); // push front
                if (ids.size() > 12)
                    ids = ids.subList(0, 12);
                String val = URLEncoder.encode(String.join(",", ids), StandardCharsets.UTF_8);
                var ck = new Cookie("RV", val);
                ck.setPath("/");
                ck.setMaxAge(30 * 24 * 3600); // 30 ngày
                res.addCookie(ck);
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}
