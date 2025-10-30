package com.cakestore.cakestore.service.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CartSessionService {

    private static final String CART_SESSION_KEY = "SESSION_CART";

    public SessionCart getCart(HttpSession session) {
        Object o = session.getAttribute(CART_SESSION_KEY);
        if (o instanceof SessionCart)
            return (SessionCart) o;
        SessionCart cart = new SessionCart();
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public void saveCart(HttpSession session, SessionCart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}