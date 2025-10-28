// src/main/java/com/cakestore/cakestore/web/FavoritesViewAdvice.java
package com.cakestore.cakestore.web;

import com.cakestore.cakestore.service.user.FavoriteDbService;
import com.cakestore.cakestore.service.user.FavoriteSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;

@Component
@ControllerAdvice
@RequiredArgsConstructor
public class FavoritesViewAdvice {

    private final FavoriteDbService favDbSvc;
    private final FavoriteSessionService favSessionSvc;

    /**
     * Inject cho mọi view: đồng bộ session từ DB nếu đã đăng nhập, và trả favIds
     * cho view dùng luôn.
     */
    @ModelAttribute("favIds")
    public Set<Long> injectFavIds(HttpSession session, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Set<Long> ids = favDbSvc.getUserFavoriteIds(auth);
            // Sync session để các template đang đọc session.FAV_IDS hoạt động ngay
            favSessionSvc.replace(session, ids);
            return ids;
        }
        // Guest: lấy từ session
        return favSessionSvc.ensure(session);
    }
}
