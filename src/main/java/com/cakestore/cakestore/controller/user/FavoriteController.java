// src/main/java/com/cakestore/cakestore/controller/user/FavoriteController.java
package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.repository.user.FavoriteRepository.FavoriteItemRow;
import com.cakestore.cakestore.service.user.FavoriteDbService;
import com.cakestore.cakestore.service.user.FavoriteSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fav")
public class FavoriteController {

    private final FavoriteSessionService favSessionSvc;
    private final FavoriteDbService favDbSvc;

    /** Toggle qua POST — user đã login ghi DB + sync session, guest chỉ session */
    @PostMapping("/toggle")
    public String toggle(@RequestParam Long productId,
            HttpSession session,
            Authentication auth,
            @RequestHeader(value = "Referer", required = false) String ref) {

        String back = redirectBack(ref);

        if (productId == null) {
            return back + (back.contains("?") ? "&" : "?") + "fav_error=invalid";
        }

        if (auth != null && auth.isAuthenticated()) {
            // Ghi DB (true = like, false = unlike)
            boolean liked = favDbSvc.toggle(auth, productId);

            // Đồng bộ session từ DB cho chắc (tránh lệch do tab khác)
            favDbSvc.refreshSessionFavs(auth, session);

            // Nếu muốn tối ưu, có thể chỉ add/remove vào set session theo biến 'liked'
            // nhưng refresh full set thì an toàn hơn với nhiều tab.
        } else {
            // Guest → session-only
            Set<Long> set = favSessionSvc.ensure(session);
            if (set.contains(productId))
                set.remove(productId);
            else
                set.add(productId);
        }
        return back;
    }

    /**
     * Trang danh sách yêu thích:
     * - User login: lấy danh sách Product từ DB để render grid/list.
     * - Guest: chưa hỗ trợ list Product (thiếu user), chỉ trả favIds session (nếu
     * muốn thì tự map ids -> sản phẩm bên controller khác).
     */
    @GetMapping
    public String list(HttpSession session, Authentication auth, Model model) {
        Set<Long> favIds;
        java.util.List<FavoriteItemRow> items = java.util.List.of();

        if (auth != null && auth.isAuthenticated()) {
            items = favDbSvc.listFavoriteItems(auth); // ✅ projection có thumbnailUrl
            favDbSvc.refreshSessionFavs(auth, session);
            favIds = favDbSvc.getUserFavoriteIds(auth);
        } else {
            favIds = favSessionSvc.ensure(session);
            // guest: chưa load items từ DB
        }

        model.addAttribute("items", items);
        model.addAttribute("favIds", favIds);
        return "user/favorites";
    }

    // ===== Helpers =====
    private String redirectBack(String ref) {
        return "redirect:" + (ref != null ? ref : "/");
    }
}
