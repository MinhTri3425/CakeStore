package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.BranchContextService;
import com.cakestore.cakestore.service.user.CartDbService;
import com.cakestore.cakestore.service.user.CartSessionService;
import com.cakestore.cakestore.service.user.CouponService;
import com.cakestore.cakestore.service.user.SessionCart;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartSessionService cartSvc;
    private final ProductRepository productRepo;
    private final CouponService couponService;
    private final CartDbService cartDbService;

    // NEW: để biết chi nhánh hiện tại và tồn kho chi nhánh
    private final BranchContextService branchCtx;
    private final BranchInventoryRepository branchInvRepo;

    public CartController(CartSessionService cartSvc,
            ProductRepository productRepo,
            CouponService couponService,
            CartDbService cartDbService,
            BranchContextService branchCtx,
            BranchInventoryRepository branchInvRepo) {
        this.cartSvc = cartSvc;
        this.productRepo = productRepo;
        this.couponService = couponService;
        this.cartDbService = cartDbService;
        this.branchCtx = branchCtx;
        this.branchInvRepo = branchInvRepo;
    }

    // ==== VIEW CART: KHÔNG MERGE Ở ĐÂY ====
    @GetMapping
    public String viewCart(Model model, HttpSession session, Authentication auth) {
        SessionCart cart = cartSvc.getCart(session);

        if (auth != null && auth.isAuthenticated()) {
            // luôn load từ DB cho user login, rồi sync session
            SessionCart fromDb = cartDbService.loadCartAsSessionCart(auth);
            if (fromDb != null) {
                cart = fromDb;
                cartSvc.saveCart(session, cart);
            }
        }

        model.addAttribute("cart", cart);
        return "user/cart";
    }

    /**
     * Thêm vào giỏ.
     * - Tôn trọng tồn kho của chi nhánh hiện tại: không cho vượt.
     * - Fix bug nhân đôi: nếu user đã có cart DB thì chỉ add dòng vừa thêm,
     * không merge nguyên session nữa.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(defaultValue = "1") int qty,
            HttpSession session,
            Authentication auth,
            HttpServletRequest request,
            @RequestHeader(value = "Referer", required = false) String ref) {

        // 1. lấy cart session (cho cảm giác realtime)
        SessionCart cart = cartSvc.getCart(session);

        // 2. validate sản phẩm
        var optP = productRepo.findById(productId);
        if (optP.isEmpty() || qty <= 0) {
            return redirectWith(ref, "cart_error=invalid");
        }
        Product product = optP.get();
        BigDecimal price = product.getPrice();
        String name = product.getName();

        // 3. Check tồn kho theo CHI NHÁNH hiện tại
        Integer branchAvailable = getAvailableStockForCurrentBranch(session, request, productId);

        // 3.1 Lấy hiện tại trong giỏ (cùng productId + variantId)
        int currentQtyInCart = cart.items().stream()
                .filter(l -> Objects.equals(l.getProductId(), productId)
                        && Objects.equals(l.getVariantId(), variantId))
                .mapToInt(SessionCart.Line::getQty)
                .sum();

        // 3.2 Nếu branch có limit và (đang có + muốn thêm) > limit -> chặn
        if (branchAvailable != null) {
            if (currentQtyInCart + qty > branchAvailable) {
                return redirectWith(ref, "cart_error=out_of_stock_branch");
            }
        } else {
            // fallback: nếu branch không resolve được (null) thì có thể dùng stock
            // tổng/quốc
            Integer availableGlobal = tryGetStock(product);
            if (availableGlobal != null && currentQtyInCart + qty > availableGlobal) {
                return redirectWith(ref, "cart_error=out_of_stock");
            }
        }

        // 4. update session cart cho UI thấy ngay
        cart.add(productId, variantId, name, price, qty);

        // 5. guest -> chỉ session, xong
        if (auth == null || !auth.isAuthenticated()) {
            cartSvc.saveCart(session, cart);
            return "redirect:" + (ref != null ? ref : "/cart");
        }

        // ==== user đã đăng nhập từ đây trở xuống ====

        // lấy cart từ DB để biết trạng thái ban đầu
        SessionCart dbCart = cartDbService.loadCartAsSessionCart(auth);
        boolean dbCartEmpty = (dbCart == null || dbCart.items().isEmpty());

        if (dbCartEmpty) {
            // DB chưa có gì -> merge toàn bộ session vào DB (lần đầu)
            cartDbService.mergeCart(auth, cart);
        } else {
            // DB đã có -> chỉ add đúng món vừa thêm, tránh nhân đôi các dòng cũ
            cartDbService.addItem(auth, productId, variantId, name, price, qty);
        }

        // 6. sync lại session = DB (DB là nguồn thật sau đăng nhập)
        SessionCart fresh = cartDbService.loadCartAsSessionCart(auth);
        if (fresh != null) {
            cartSvc.saveCart(session, fresh);
        } else {
            cartSvc.saveCart(session, cart); // fallback
        }

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    /**
     * Đặt qty tuyệt đối cho 1 dòng (ví dụ trong trang /cart user sửa số lượng).
     * - Cũng phải tôn trọng tồn kho chi nhánh.
     */
    @PostMapping("/set-qty")
    public String setQty(@RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam int qty,
            HttpSession session,
            Authentication auth,
            HttpServletRequest request,
            @RequestHeader(value = "Referer", required = false) String ref) {

        SessionCart cart = cartSvc.getCart(session);

        // clamp min 0
        int desiredQty = Math.max(0, qty);

        // check tồn kho theo branch
        Integer branchAvailable = getAvailableStockForCurrentBranch(session, request, productId);

        // fallback: lấy giá và global stock để clamp
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
            // đặt thẳng qty trong DB (không merge, không cộng dồn)
            cartDbService.setDbQty(auth, productId, variantId, desiredQty, priceForDb);

            // sync session từ DB
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

    /** Xoá một dòng sản phẩm khỏi giỏ */
    @PostMapping("/remove")
    public String remove(@RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            HttpSession session,
            Authentication auth,
            @RequestHeader(value = "Referer", required = false) String ref) {

        // xóa khỏi session trước cho phản hồi nhanh
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

    /** Xoá cả giỏ */
    @PostMapping("/clear")
    public String clear(HttpSession session,
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

        return "redirect:" + (ref != null ? ref : "/cart");
    }

    // ===== Helpers =====

    /**
     * Lấy tồn khả dụng (available) của product tại chi nhánh hiện tại
     * Nếu không xác định được chi nhánh => trả về null.
     * null nghĩa là "không biết", để caller tự fallback.
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
                .map(inv -> inv.getAvailable()) // quantity - reserved
                .orElse(0);
    }

    /**
     * fallback stock tổng quát (Product.getStock(), getQuantity(),
     * getAvailable()...)
     * nếu bạn có field như vậy trong entity Product.
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
