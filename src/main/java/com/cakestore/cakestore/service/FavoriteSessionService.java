// service/FavoriteSessionService.java
package com.cakestore.cakestore.service;

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
}
