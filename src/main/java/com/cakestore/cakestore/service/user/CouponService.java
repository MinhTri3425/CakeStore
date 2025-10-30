package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.repository.user.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository repo;

    public CouponService(CouponRepository r) {
        this.repo = r;
    }

    // Kết quả trả về sau khi thử áp mã
    public record Result(
            boolean ok,
            String message,
            Coupon coupon,
            BigDecimal discount) {
    }

    public Result apply(String rawCode, BigDecimal rawSubtotal) {
        // normalize input
        String code = rawCode == null ? "" : rawCode.trim();
        if (code.isEmpty()) {
            return new Result(false, "Bạn chưa nhập mã", null, BigDecimal.ZERO);
        }

        BigDecimal subtotal = safe(rawSubtotal);

        // lấy coupon theo code (không phân biệt hoa/thường) và đang active
        Coupon cp = repo.findByCodeIgnoreCaseAndIsActiveTrue(code).orElse(null);
        if (cp == null) {
            return new Result(false,
                    "Mã không tồn tại hoặc đã bị khóa",
                    null,
                    BigDecimal.ZERO);
        }

        // check thời gian / còn lượt
        LocalDateTime now = LocalDateTime.now();
        if (!cp.isActiveNow(now)) {
            return new Result(false,
                    "Mã đã hết hạn / chưa tới thời gian áp dụng / hết lượt",
                    cp,
                    BigDecimal.ZERO);
        }

        // check min subtotal
        if (cp.getMinSubtotal() != null && subtotal.compareTo(cp.getMinSubtotal()) < 0) {
            return new Result(false,
                    "Chưa đạt giá trị tối thiểu " + cp.getMinSubtotal() + "đ",
                    cp,
                    BigDecimal.ZERO);
        }

        // tính số tiền giảm
        BigDecimal discount = switch (cp.getType()) {
            case PERCENT -> {
                // % trên subtotal, ví dụ 10%
                BigDecimal raw = subtotal
                        .multiply(safe(cp.getValue()))
                        .divide(BigDecimal.valueOf(100));
                yield raw;
            }
            case AMOUNT -> {
                // giảm thẳng X đ
                yield safe(cp.getValue());
            }
            case SHIPPING_OFF -> {
                // giảm ship, không giảm subtotal.
                // => ta coi như 0 ở đây, còn lúc checkout sẽ miễn phí ship.
                yield BigDecimal.ZERO;
            }
        };

        // clamp maxDiscount nếu có
        if (cp.getMaxDiscount() != null && discount.compareTo(cp.getMaxDiscount()) > 0) {
            discount = cp.getMaxDiscount();
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        return new Result(true, "Áp mã thành công", cp, discount);
    }

    private BigDecimal safe(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }
}
