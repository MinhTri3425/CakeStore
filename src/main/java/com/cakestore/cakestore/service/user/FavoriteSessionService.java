// src/main/java/com/cakestore/cakestore/service/user/FavoriteSessionService.java
package com.cakestore.cakestore.service.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FavoriteSessionService {
    private static final String KEY = "FAV_IDS";

    @SuppressWarnings("unchecked")
    public Set<Long> get(HttpSession s) {
        return (Set<Long>) s.getAttribute(KEY);
    }

    public Set<Long> ensure(HttpSession s) {
        var set = get(s);
        if (set == null) {
            set = new LinkedHashSet<>();
            s.setAttribute(KEY, set);
        }
        return set;
    }

    /** Ghi đè toàn bộ session set theo DB (an toàn copy) */
    public void replace(HttpSession s, Collection<Long> ids) {
        LinkedHashSet<Long> copy = new LinkedHashSet<>();
        if (ids != null)
            copy.addAll(ids);
        s.setAttribute(KEY, copy);
    }
}
