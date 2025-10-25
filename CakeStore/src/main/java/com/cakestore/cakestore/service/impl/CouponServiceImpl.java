package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.repository.CouponRepository;
import com.cakestore.cakestore.service.CouponService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon findById(Long id) {
        return couponRepository.findById(id).orElse(null);
    }

    @Override
    public Coupon save(Coupon coupon) {
        // TODO: Thêm validation (code unique, dates hợp lệ)
        return couponRepository.save(coupon);
    }

    @Override
    public void deleteById(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return couponRepository.existsByCode(code);
    }
}