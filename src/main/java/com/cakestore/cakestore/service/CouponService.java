package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Coupon;
import java.util.List;

public interface CouponService {
    List<Coupon> findAll();
    Coupon findById(Long id);
    Coupon save(Coupon coupon);
    void deleteById(Long id);
    boolean existsByCode(String code);
}