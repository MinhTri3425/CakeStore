package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.repository.CouponRepository;
import com.cakestore.cakestore.service.CouponService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class CouponServiceImpl implements CouponService {

	private final CouponRepository couponRepository;

	public CouponServiceImpl(CouponRepository couponRepository) {
		this.couponRepository = couponRepository;
	}

	@Override
	public Page<Coupon> search(String q, Coupon.Type type, Boolean active, Long branchId, Pageable pageable) {
		String keyword = (q == null || q.isBlank()) ? null : q.trim();
		return couponRepository.search(keyword, type, active, branchId, pageable);
	}

	@Override
	public Coupon findById(Long id) {
		return couponRepository.findById(id).orElse(null);
	}

	@Override
	public Coupon save(Coupon c) {
		// --- Validate cơ bản ---
		if (c.getCode() == null || c.getCode().isBlank())
			throw new IllegalArgumentException("Code không được trống.");

		// code unique (khi tạo mới hoặc sửa sang code khác)
		couponRepository.findAll().stream().filter(x -> x.getCode().equalsIgnoreCase(c.getCode()))
				.filter(x -> c.getId() == null || !x.getId().equals(c.getId())).findAny().ifPresent(x -> {
					throw new IllegalArgumentException("Code đã tồn tại.");
				});

		if (c.getType() == null)
			throw new IllegalArgumentException("Phải chọn loại mã (PERCENT/AMOUNT/SHIPPING_OFF).");

		if (c.getValue() == null || c.getValue().signum() <= 0)
			throw new IllegalArgumentException("Giá trị mã phải > 0.");

		if (c.getStartsAt() == null || c.getEndsAt() == null)
			throw new IllegalArgumentException("Khoảng hiệu lực không hợp lệ.");
		if (c.getEndsAt().isBefore(c.getStartsAt()))
			throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");

		if (c.getQuantity() != null && c.getQuantity() < 0)
			throw new IllegalArgumentException("Số lượng không được âm.");

		// Nếu là % thì gợi ý value <= 100
		if (c.getType() == Coupon.Type.PERCENT && c.getValue().intValue() > 100)
			throw new IllegalArgumentException("Giá trị phần trăm không được > 100.");

		// maxDiscount không bắt buộc, nhưng nếu có thì phải >0
		if (c.getMaxDiscount() != null && c.getMaxDiscount().signum() < 0)
			throw new IllegalArgumentException("MaxDiscount không hợp lệ.");

		return couponRepository.save(c);
	}

	@Override
	public void deleteById(Long id) {
		// tuỳ nghiệp vụ: nếu đã dùng trong order thì cấm xoá, chỉ set inactive
		couponRepository.deleteById(id);
	}

	@Override
	public boolean existsByCode(String code) {
		return couponRepository.existsByCode(code);
	}
}