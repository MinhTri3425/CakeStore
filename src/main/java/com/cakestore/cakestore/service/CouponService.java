// service/CouponService.java
package com.cakestore.cakestore.service;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.repository.CouponRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CouponService {
    private final CouponRepository repo;

    public CouponService(CouponRepository r) {
        this.repo = r;
    }

    public record Result(boolean ok, String message, Coupon coupon, BigDecimal discount) {
    }

    public Result apply(String code, BigDecimal subtotal) {
        var cp = repo.findByCodeAndIsActiveTrue(code).orElse(null);
        if (cp == null)
            return new Result(false, "Mã không tồn tại/không hoạt động", null, BigDecimal.ZERO);
        var now = LocalDateTime.now();
        if (!cp.isActiveNow(now))
            return new Result(false, "Mã đã hết hạn/chưa hiệu lực", null, BigDecimal.ZERO);
        if (cp.getMinSubtotal() != null && subtotal.compareTo(cp.getMinSubtotal()) < 0)
            return new Result(false, "Chưa đạt giá trị tối thiểu", null, BigDecimal.ZERO);

        BigDecimal discount = switch (cp.getType()) {
            case PERCENT -> subtotal.multiply(cp.getValue()).divide(BigDecimal.valueOf(100));
            case AMOUNT -> cp.getValue();
            case SHIPPING_OFF -> BigDecimal.ZERO; // xử lý ở phần phí ship nếu có
        };
        if (cp.getMaxDiscount() != null && discount.compareTo(cp.getMaxDiscount()) > 0)
            discount = cp.getMaxDiscount();
        if (discount.compareTo(BigDecimal.ZERO) < 0)
            discount = BigDecimal.ZERO;
        return new Result(true, "Áp mã thành công", cp, discount);
    }
}
