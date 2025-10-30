package com.cakestore.cakestore.service.admin;

import com.cakestore.cakestore.entity.Coupon;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {
	Page<Coupon> search(String q, Coupon.Type type, Boolean active, Long branchId, Pageable pageable);
    Coupon findById(Long id);
    Coupon save(Coupon coupon);
    void deleteById(Long id);
    boolean existsByCode(String code);
}