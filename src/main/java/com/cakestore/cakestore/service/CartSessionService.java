package com.cakestore.cakestore.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CartSessionService {

    public static final String CART_KEY = "SESSION_CART";

    public SessionCart getCart(HttpSession session) {
        var cart = (SessionCart) session.getAttribute(CART_KEY);
        if (cart == null) {
            cart = new SessionCart();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }
}
