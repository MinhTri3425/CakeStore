package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.dto.user.ProductCardDto;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.RecentlyViewed;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.repository.user.RecentlyViewedRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecentlyViewedService {

    private final RecentlyViewedRepository rvRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    /**
     * Gọi mỗi lần user mở /product/{id} (khi đã đăng nhập).
     */
    @Transactional
    public void recordView(String userEmail, Long productId) {
        if (userEmail == null) {
            // guest thì thôi, mình đang track chỉ user login
            return;
        }

        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user == null)
            return;

        Product product = productRepo.findById(productId).orElse(null);
        if (product == null)
            return;

        RecentlyViewed rv = new RecentlyViewed();
        rv.setUser(user);
        rv.setProduct(product);
        rvRepo.save(rv);

        // Dọn rác: giữ tối đa 200 log / user
        trimUserLogs(user.getId(), 200);
    }

    /**
     * Lấy danh sách sản phẩm đã xem gần đây (dedup theo product, mới -> cũ),
     * rồi map sang ProductCardDto để UI xài (có thumbnailUrl).
     */
    @Transactional
    public List<ProductCardDto> getRecentlyViewedDtos(String userEmail, int limit) {
        if (userEmail == null)
            return List.of();

        User user = userRepo.findByEmail(userEmail).orElse(null);
        if (user == null)
            return List.of();

        List<RecentlyViewed> logs = rvRepo.findAllByUserOrderByViewedAtDesc(user.getId());

        // Giữ thứ tự mới -> cũ, nhưng không trùng product
        List<ProductCardDto> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        for (RecentlyViewed rv : logs) {
            Product p = rv.getProduct();
            if (p == null)
                continue;
            Long pid = p.getId();
            if (pid == null)
                continue;

            if (seen.contains(pid))
                continue;
            seen.add(pid);

            // Lấy thumbnailUrl = ảnh đầu tiên (nếu có)
            String thumb = null;
            // Product.getImages() là Set<ProductImage>, dùng stream để lấy phần tử đầu
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                // LinkedHashSet nên giữ insertion order; ta lấy phần tử đầu tiên.
                var firstImg = p.getImages().iterator().next();
                if (firstImg != null) {
                    // GIẢ ĐỊNH ProductImage có getUrl()
                    thumb = firstImg.getUrl();
                }
            }

            // Rating / soldCount nếu có ProductStats
            Integer soldCount = null;
            java.math.BigDecimal ratingAvg = null;
            Integer ratingCount = null;
            if (p.getProductStats() != null) {
                var st = p.getProductStats();
                soldCount = st.getSoldCount(); // đổi theo field thực tế
                ratingAvg = st.getRatingAvg(); // đổi theo field thực tế
                ratingCount = st.getRatingCount(); // đổi theo field thực tế
            }

            ProductCardDto dto = new ProductCardDto(
                    p.getId(),
                    p.getName(),
                    p.getSlug(),
                    p.getPrice(),
                    thumb,
                    soldCount,
                    ratingAvg,
                    ratingCount);

            result.add(dto);

            if (result.size() >= limit)
                break;
        }

        return result;
    }

    /**
     * Giữ tối đa maxKeep log mới nhất, xóa phần dư.
     */
    @Transactional
    protected void trimUserLogs(Long userId, int maxKeep) {
        List<Long> allIdsDesc = rvRepo.findAllIdsForUserOrderByViewedAtDesc(userId);
        if (allIdsDesc.size() <= maxKeep) {
            return;
        }

        // từ index maxKeep trở đi là cũ → xóa
        List<Long> staleIds = allIdsDesc.subList(maxKeep, allIdsDesc.size());
        rvRepo.deleteAllById(staleIds);
    }
}
