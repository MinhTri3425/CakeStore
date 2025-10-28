package com.cakestore.cakestore.security;

import com.cakestore.cakestore.service.user.CartDbService;
import com.cakestore.cakestore.service.user.CartSessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
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
            Authentication authentication) throws IOException, ServletException {
        var session = request.getSession(false);
        if (session != null) {
            var cart = cartSessionService.getCart(session);
            cartDbService.mergeCart(authentication, cart);
            // optional: clear session cart if you want DB-only storage
            // cartSessionService.clearCart(session);
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}