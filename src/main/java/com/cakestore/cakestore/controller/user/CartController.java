package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import com.cakestore.cakestore.repository.user.BranchRepository;
import com.cakestore.cakestore.repository.user.CouponRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.BranchContextService;
import com.cakestore.cakestore.service.user.CartDbService;
import com.cakestore.cakestore.service.user.CartSessionService;
import com.cakestore.cakestore.service.user.SessionCart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartSessionService cartSvc;
    private final ProductRepository productRepo;
    private final CartDbService cartDbService;

    private final BranchContextService branchCtx;
    private final BranchInventoryRepository branchInvRepo;

    // coupon deps
    private final CouponRepository couponRepo;
    private final BranchRepository branchRepo; // có thể không còn dùng, nhưng giữ nguyên để không ảnh hưởng nơi khác

    public CartController(
            CartSessionService cartSvc,
            ProductRepository productRepo,
            CartDbService cartDbService,
            BranchContextService branchCtx,
            BranchInventoryRepository branchInvRepo,
            CouponRepository couponRepo,
            BranchRepository branchRepo) {
        this.cartSvc = cartSvc;
        this.productRepo = productRepo;
        this.cartDbService = cartDbService;
        this.branchCtx = branchCtx;
        this.branchInvRepo = branchInvRepo;
        this.couponRepo = couponRepo;
        this.branchRepo = branchRepo;
    }

    // ==== VIEW CART ====
    @GetMapping
    public String viewCart(Model model, HttpSession session, Authentication auth) {
        SessionCart cart = cartSvc.getCart(session);

        if (auth != null && auth.isAuthenticated()) {
            // luôn sync với DB giỏ hàng thật
            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth);
            if (fromDb != null) {
                cart = fromDb;
                cartSvc.saveCart(session, cart);
            }
        }

        // subtotal thô
        BigDecimal subtotal = cart != null ? cart.subtotal() : BigDecimal.ZERO;
        if (subtotal == null)
            subtotal = BigDecimal.ZERO;

        // lấy coupon đã apply trong session (nếu có)
        BigDecimal couponValue = (BigDecimal) session.getAttribute("COUPON_VALUE");
        if (couponValue == null)
            couponValue = BigDecimal.ZERO;

        // tổng sau giảm (chưa tính ship vì trang cart của bạn chưa có phí ship)
        BigDecimal total = subtotal.subtract(couponValue);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", couponValue); // để th:if hiển thị "Giảm:"
        model.addAttribute("total", total);

        // để `<p class="coupon-note"... th:text="${session.COUPON_MSG}"></p>`
        model.addAttribute("couponMsg", session.getAttribute("COUPON_MSG"));
        model.addAttribute("couponCode", session.getAttribute("COUPON_CODE"));

        return "user/cart";
    }

    /**
     * Áp mã giảm giá.
     * Lấy code từ form, validate coupon với branch + thời gian + minSubtotal,
     * tính số tiền giảm, nhét vào session để cart.html render.
     */
    @PostMapping("/apply-coupon")
    public String applyCoupon(
            @RequestParam("code") String rawCode,
            HttpSession session,
            Authentication auth,
            HttpServletRequest request) { // thêm request để đọc cookie qua branchCtx
        // luôn clear message cũ trước
        session.removeAttribute("COUPON_MSG");

        // 1. cần có giỏ
        SessionCart cart = cartSvc.getCart(session);
        if (auth != null && auth.isAuthenticated()) {
            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth);
            if (fromDb != null) {
                cart = fromDb;
                cartSvc.saveCart(session, cart);
            }
        }
        if (cart == null || cart.isEmpty()) {
            setCouponFail(session, "Giỏ hàng trống.");
            return "redirect:/cart";
        }

        // 2. lấy branch hiện tại từ cookie/session qua BranchContextService
        Optional<Branch> currentBranchOpt = branchCtx.resolveCurrentBranch(session, request);
        Branch currentBranch = currentBranchOpt.orElse(null);

        // 3. tìm coupon theo code
        String code = rawCode == null ? "" : rawCode.trim();
        Optional<Coupon> couponOpt = couponRepo.findByCodeIgnoreCaseAndIsActiveTrue(code);
        if (couponOpt.isEmpty()) {
            setCouponFail(session, "Mã không tồn tại hoặc đã ngừng.");
            return "redirect:/cart";
        }

        Coupon coupon = couponOpt.get();

        // 4. check thời gian / số lượt còn hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActiveNow(now)) {
            setCouponFail(session, "Mã đã hết hạn hoặc hết lượt.");
            return "redirect:/cart";
        }

        // 5. check ràng buộc chi nhánh (nếu coupon gắn branch)
        if (coupon.getBranch() != null) {
            if (currentBranch == null ||
                    !coupon.getBranch().getId().equals(currentBranch.getId())) {
                setCouponFail(session, "Mã chỉ dùng tại chi nhánh khác.");
                return "redirect:/cart";
            }
        }

        // 6. subtotal hiện tại
        BigDecimal subtotal = cart.subtotal();
        if (subtotal == null)
            subtotal = BigDecimal.ZERO;

        // 7. minSubtotal
        if (coupon.getMinSubtotal() != null &&
                subtotal.compareTo(coupon.getMinSubtotal()) < 0) {
            setCouponFail(session, "Chưa đạt giá trị tối thiểu để dùng mã.");
            return "redirect:/cart";
        }

        // 8. tính số tiền giảm
        BigDecimal discountAmt = calcDiscountAmountForCart(coupon, subtotal);

        if (discountAmt == null || discountAmt.compareTo(BigDecimal.ZERO) <= 0) {
            setCouponFail(session, "Mã không áp dụng cho đơn này.");
            return "redirect:/cart";
        }

        // 9. Lưu session để cart.html render được
        session.setAttribute("COUPON_CODE", coupon.getCode());
        session.setAttribute("COUPON_VALUE", discountAmt);
        session.setAttribute("COUPON_MSG", "Áp dụng mã thành công!");

        return "redirect:/cart";
    }

    private void setCouponFail(HttpSession session, String msg) {
        // clear coupon info cũ
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.setAttribute("COUPON_MSG", msg);
    }

    /**
     * Tính số tiền giảm của coupon dựa trên subtotal.
     * PERCENT: value = % giảm, có thể cap bằng maxDiscount.
     * AMOUNT: value = số tiền trừ thẳng.
     * SHIPPING_OFF: hiện tạm coi là 0 ở giỏ (phí ship tính ở checkout).
     */
    private BigDecimal calcDiscountAmountForCart(Coupon coupon, BigDecimal subtotal) {
        if (subtotal == null)
            subtotal = BigDecimal.ZERO;
        BigDecimal zero = BigDecimal.ZERO;

        switch (coupon.getType()) {
            case PERCENT: {
                BigDecimal percent = coupon.getValue(); // ví dụ 10 nghĩa là 10%
                if (percent == null)
                    return zero;

                // raw = subtotal * percent / 100
                BigDecimal raw = subtotal
                        .multiply(percent)
                        .divide(BigDecimal.valueOf(100));

                // cap theo maxDiscount nếu có
                if (coupon.getMaxDiscount() != null &&
                        raw.compareTo(coupon.getMaxDiscount()) > 0) {
                    raw = coupon.getMaxDiscount();
                }

                // không âm
                return raw.max(zero);
            }

            case AMOUNT: {
                BigDecimal flat = coupon.getValue();
                if (flat == null)
                    return zero;
                if (flat.compareTo(zero) < 0)
                    return zero;

                // không giảm quá subtotal
                if (flat.compareTo(subtotal) > 0) {
                    return subtotal;
                }
                return flat;
            }

            case SHIPPING_OFF: {
                // Giỏ hàng chưa tính ship, nên tạm thời để 0.
                // Ở checkout bạn có phí ship -> lúc đó bạn có thể apply free-ship.
                return zero;
            }

            default:
                return zero;
        }
    }

    /**
     * Thêm vào giỏ.
     * - Tôn trọng tồn kho của chi nhánh hiện tại: không cho vượt.
     * - Không double-merge cart DB.
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(defaultValue = "1") int qty,
            HttpSession session,
            Authentication auth,
            HttpServletRequest request,
            @RequestHeader(value = "Referer", required = false) String ref) {

        // lấy cart session
        SessionCart cart = cartSvc.getCart(session);

        // check sp
        var optP = productRepo.findById(productId);
        if (optP.isEmpty() || qty <= 0) {
            return redirectWith(ref, "cart_error=invalid");
        }
        Product product = optP.get();
        BigDecimal price = product.getPrice();
        String name = product.getName();

        // tồn kho chi nhánh
        Integer branchAvailable = getAvailableStockForCurrentBranch(session, request, productId);

        // hiện tại trong giỏ
        int currentQtyInCart = cart.items().stream()
                .filter(l -> Objects.equals(l.getProductId(), productId)
                        && Objects.equals(l.getVariantId(), variantId))
                .mapToInt(SessionCart.Line::getQty)
                .sum();

        if (branchAvailable != null) {
            if (currentQtyInCart + qty > branchAvailable) {
                return redirectWith(ref, "cart_error=out_of_stock_branch");
            }
        } else {
            Integer availableGlobal = tryGetStock(product);
            if (availableGlobal != null && currentQtyInCart + qty > availableGlobal) {
                return redirectWith(ref, "cart_error=out_of_stock");
            }
        }

        // update session cart
        cart.add(productId, variantId, name, price, qty);

        // guest
        if (auth == null || !auth.isAuthenticated()) {
            cartSvc.saveCart(session, cart);
            return "redirect:" + (ref != null ? ref : "/cart");
        }

        // user đã đăng nhập:
        SessionCart dbCart = cartDbService.loadCartAsSessionCart(auth);
        boolean dbCartEmpty = (dbCart == null || dbCart.items().isEmpty());

        if (dbCartEmpty) {
            // DB chưa có gì -> merge toàn bộ session lần đầu
            cartDbService.mergeCart(auth, cart);
        } else {
            // DB đã có -> chỉ thêm dòng mới
            cartDbService.addItem(auth, productId, variantId, name, price, qty);
        }

        // sync lại session từ DB
        SessionCart fresh = cartDbService.loadCartAsSessionCart(auth);
        if (fresh != null) {
            cartSvc.saveCart(session, fresh);
        } else {
            cartSvc.saveCart(session, cart);
        }

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    /**
     * User chỉnh số lượng dòng trong trang cart.
     */
    @PostMapping("/set-qty")
    public String setQty(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam int qty,
            HttpSession session,
            Authentication auth,
            HttpServletRequest request,
            @RequestHeader(value = "Referer", required = false) String ref) {

        SessionCart cart = cartSvc.getCart(session);

        // clamp >=0
        int desiredQty = Math.max(0, qty);

        // check tồn kho chi nhánh
        Integer branchAvailable = getAvailableStockForCurrentBranch(session, request, productId);

        // fallback để biết price
        BigDecimal priceForDb = null;
        var opt = productRepo.findById(productId);
        if (opt.isPresent()) {
            Product p = opt.get();
            priceForDb = p.getPrice();

            if (branchAvailable != null) {
                if (desiredQty > branchAvailable)
                    desiredQty = branchAvailable;
            } else {
                Integer availableGlobal = tryGetStock(p);
                if (availableGlobal != null && desiredQty > availableGlobal)
                    desiredQty = availableGlobal;
            }
        }

        if (auth != null && auth.isAuthenticated()) {
            // đặt qty tuyệt đối trong DB
            cartDbService.setDbQty(auth, productId, variantId, desiredQty, priceForDb);

            // sync session
            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth);
            if (fromDb != null)
                cartSvc.saveCart(session, fromDb);
        } else {
            // guest
            cart.setQty(productId, variantId, desiredQty);
            cartSvc.saveCart(session, cart);
        }

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    @PostMapping("/remove")
    public String remove(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            HttpSession session,
            Authentication auth,
            @RequestHeader(value = "Referer", required = false) String ref) {

        SessionCart cart = cartSvc.getCart(session);
        cart.remove(productId, variantId);

        if (auth != null && auth.isAuthenticated()) {
            cartDbService.removeDbItem(auth, productId, variantId);

            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth);
            if (fromDb != null)
                cartSvc.saveCart(session, fromDb);
        } else {
            cartSvc.saveCart(session, cart);
        }

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    @PostMapping("/clear")
    public String clear(
            HttpSession session,
            Authentication auth,
            @RequestHeader(value = "Referer", required = false) String ref) {

        SessionCart cart = cartSvc.getCart(session);
        cart.clear();
        cartSvc.saveCart(session, cart);

        if (auth != null && auth.isAuthenticated()) {
            cartDbService.clearDbCart(auth);

            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth); // thường rỗng
            if (fromDb != null)
                cartSvc.saveCart(session, fromDb);
        }

        // clear luôn coupon khi clear giỏ
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.setAttribute("COUPON_MSG", "Giỏ hàng đã được xóa.");

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    // ===== Helpers =====

    /**
     * Lấy tồn khả dụng (available) của product tại chi nhánh hiện tại.
     * Nếu không xác định được chi nhánh => trả về null.
     */
    private Integer getAvailableStockForCurrentBranch(HttpSession session,
            HttpServletRequest request,
            Long productId) {
        Optional<Branch> branchOpt = branchCtx.resolveCurrentBranch(session, request);
        if (branchOpt.isEmpty() || productId == null)
            return null;

        Branch b = branchOpt.get();
        return branchInvRepo
                .findByBranch_IdAndProduct_Id(b.getId(), productId)
                .map(inv -> inv.getAvailable())
                .orElse(0);
    }

    /**
     * Thử đọc stock "global" từ entity Product nếu có mấy field như getStock().
     */
    private Integer tryGetStock(Object product) {
        try {
            try {
                var m = product.getClass().getMethod("getStock");
                Object v = m.invoke(product);
                if (v instanceof Number)
                    return ((Number) v).intValue();
            } catch (NoSuchMethodException ignored) {
            }

            try {
                var m = product.getClass().getMethod("getQuantity");
                Object v = m.invoke(product);
                if (v instanceof Number)
                    return ((Number) v).intValue();
            } catch (NoSuchMethodException ignored) {
            }

            try {
                var m = product.getClass().getMethod("getAvailable");
                Object v = m.invoke(product);
                if (v instanceof Number)
                    return ((Number) v).intValue();
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String redirectWith(String ref, String query) {
        String safe = (ref != null ? ref : "/cart");
        return "redirect:" + safe + (safe.contains("?") ? "&" : "?") + query;
    }
}
