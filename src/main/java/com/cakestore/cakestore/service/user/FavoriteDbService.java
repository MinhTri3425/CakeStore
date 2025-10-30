// src/main/java/com/cakestore/cakestore/service/user/FavoriteDbService.java
package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.Favorite;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.user.FavoriteRepository;
import com.cakestore.cakestore.repository.user.FavoriteRepository.FavoriteItemRow;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteDbService {

    private final FavoriteRepository favoriteRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    /** Lấy userId từ Authentication (linh hoạt: email → username → id) */
    private Long currentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return null;
        Object p = auth.getPrincipal();
        String key = (p instanceof UserDetails ud) ? ud.getUsername() : (p instanceof String s ? s : null);
        if (key == null)
            return null;

        try {
            var m = userRepo.getClass().getMethod("findByEmail", String.class);
            Object r = m.invoke(userRepo, key);
            User u = unwrap(r);
            if (u != null)
                return u.getId();
        } catch (Throwable ignored) {
        }

        try {
            var m = userRepo.getClass().getMethod("findByUsername", String.class);
            Object r = m.invoke(userRepo, key);
            User u = unwrap(r);
            if (u != null)
                return u.getId();
        } catch (Throwable ignored) {
        }

        try {
            return userRepo.findById(Long.parseLong(key)).map(User::getId).orElse(null);
        } catch (Throwable ignored) {
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private User unwrap(Object r) {
        if (r == null)
            return null;
        if (r instanceof User u)
            return u;
        if (r instanceof Optional<?> opt && opt.isPresent() && opt.get() instanceof User u)
            return u;
        if (r instanceof Iterable<?> it) {
            for (Object o : it)
                if (o instanceof User u)
                    return u;
        }
        return null;
    }

    /**
     * Toggle DB: trả về true nếu KẾT QUẢ sau toggle là "đã like", false nếu "bỏ
     * like".
     */
    @Transactional
    public boolean toggle(Authentication auth, Long productId) {
        Long uid = currentUserId(auth);
        if (uid == null || productId == null) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        if (favoriteRepo.existsByUserIdAndProductId(uid, productId)) {
            favoriteRepo.deleteByUserIdAndProductId(uid, productId);
            return false; // sau toggle = bỏ like
        } else {
            // đảm bảo tồn tại
            Product p = productRepo.findById(productId).orElseThrow();
            User u = userRepo.findById(uid).orElseThrow();
            try {
                favoriteRepo.save(new Favorite(u, p));
            } catch (DataIntegrityViolationException dup) {
                // race condition: coi như đã like
            }
            return true; // sau toggle = đã like
        }
    }

    /** Lấy danh sách productId đã like của user (để tô tim). */
    @Transactional(readOnly = true)
    public Set<Long> getUserFavoriteIds(Authentication auth) {
        Long uid = currentUserId(auth);
        if (uid == null)
            return Collections.emptySet();
        return favoriteRepo.findByUserId(uid)
                .stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Lấy trực tiếp danh sách Product đã tim (để render trang /fav). */
    @Transactional(readOnly = true)
    public java.util.List<FavoriteItemRow> listFavoriteItems(Authentication auth) {
        Long uid = currentUserId(auth);
        if (uid == null)
            return java.util.List.of();
        return favoriteRepo.findFavoriteItems(uid);
    }

    /** Đồng bộ session để UI đổi màu tim ngay (session attribute: FAV_IDS). */
    public void refreshSessionFavs(Authentication auth, HttpSession session) {
        if (session == null)
            return;
        Set<Long> ids = getUserFavoriteIds(auth);
        session.setAttribute("FAV_IDS", ids);
    }
}
