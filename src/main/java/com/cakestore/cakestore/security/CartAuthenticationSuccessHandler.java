package com.cakestore.cakestore.security;

import com.cakestore.cakestore.service.user.CartDbService;
import com.cakestore.cakestore.service.user.CartSessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CartAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CartDbService cartDbService;
    private final CartSessionService cartSessionService;

    public CartAuthenticationSuccessHandler(CartDbService cartDbService, CartSessionService cartSessionService) {
        this.cartDbService = cartDbService;
        this.cartSessionService = cartSessionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Gộp giỏ hàng session vào DB nếu có
        var session = request.getSession(false);
        if (session != null) {
            var cart = cartSessionService.getCart(session);
            cartDbService.mergeCart(authentication, cart);
        }

        // Kiểm tra role để redirect phù hợp
        String redirectUrl = request.getContextPath() + "/default-success"; // fallback
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = request.getContextPath() + "/admin/home";
                break;
            } else if (role.equals("ROLE_STAFF")) {
                redirectUrl = request.getContextPath() + "/staff/home";
                break;
            } else if (role.equals("ROLE_USER")) {
                redirectUrl = request.getContextPath() + "/user/home";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
