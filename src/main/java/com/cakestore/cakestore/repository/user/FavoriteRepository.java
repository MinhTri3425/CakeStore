// src/main/java/com/cakestore/cakestore/repository/user/FavoriteRepository.java
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Favorite;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Favorite.FavoriteKey> {

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    // ✅ BỎ p.thumbnail_url, chỉ lấy ảnh đầu tiên từ product_images
    @Query(value = """
            SELECT p.id   AS id,
                   p.name AS name,
                   p.price AS price,
                   (SELECT TOP 1 i.url
                      FROM product_images i
                     WHERE i.product_id = p.id
                     ORDER BY
                       CASE WHEN i.sort_order IS NULL THEN 1 ELSE 0 END,
                       i.sort_order, i.id) AS thumbnailUrl
              FROM favorites f
              JOIN products  p ON p.id = f.product_id
             WHERE f.user_id = :uid
             ORDER BY f.created_at DESC
            """, nativeQuery = true)
    List<FavoriteItemRow> findFavoriteItems(@Param("uid") Long uid);

    interface FavoriteItemRow {
        Long getId();

        String getName();

        BigDecimal getPrice();

        String getThumbnailUrl();
    }
}
