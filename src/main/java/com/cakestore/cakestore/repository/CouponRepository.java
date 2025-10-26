package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // Tìm coupon theo Code (để kiểm tra trùng hoặc áp dụng)
    boolean existsByCode(String code);
}