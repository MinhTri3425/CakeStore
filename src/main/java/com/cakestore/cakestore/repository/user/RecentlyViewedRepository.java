package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {

    // Lấy log xem gần đây nhất -> cũ hơn, có fetch product để tránh N+1.
    @Query("""
                select rv
                from RecentlyViewed rv
                join fetch rv.product p
                where rv.user.id = :userId
                order by rv.viewedAt desc
            """)
    List<RecentlyViewed> findAllByUserOrderByViewedAtDesc(Long userId);

    // Lấy id của tất cả log (cũng newest -> oldest) để dọn rác
    @Query("""
                select rv.id
                from RecentlyViewed rv
                where rv.user.id = :userId
                order by rv.viewedAt desc
            """)
    List<Long> findAllIdsForUserOrderByViewedAtDesc(Long userId);
}
