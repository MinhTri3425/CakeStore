// repository/CouponRepository.java
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
}
