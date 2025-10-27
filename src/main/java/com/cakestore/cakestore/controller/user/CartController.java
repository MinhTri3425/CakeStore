package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.service.user.CartSessionService;
import com.cakestore.cakestore.service.user.CouponService;
import com.cakestore.cakestore.service.user.SessionCart;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartSessionService cartSvc;
    private final ProductRepository productRepo;
    private final CouponService couponService; // <-- thêm

    public CartController(CartSessionService c, ProductRepository p, CouponService couponService) {
        this.cartSvc = c;
        this.productRepo = p;
        this.couponService = couponService;
    }

    @GetMapping
    public String view(HttpSession session, Model m) {
        SessionCart cart = cartSvc.getCart(session);
        m.addAttribute("cart", cart);
        return "user/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId,
            @RequestParam(defaultValue = "1") int qty,
            HttpSession session,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        var p = productRepo.findById(productId).orElseThrow();
        if (qty < 1)
            qty = 1;
        cartSvc.getCart(session).add(p.getId(), null, p.getName(), p.getPrice(), qty);

        // Áp mã hiện tại (nếu có) cần tính lại? (không bắt buộc vì view tính dựa trên
        // subtotal hiện thời)
        return "redirect:" + (returnUrl != null ? returnUrl : "/cart");
    }

    @PostMapping("/set-qty")
    public String setQty(@RequestParam Long productId,
            @RequestParam int qty,
            HttpSession session,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        if (qty < 0)
            qty = 0; // =0 sẽ xoá dòng
        cartSvc.getCart(session).setQty(productId, null, qty);
        return "redirect:" + (returnUrl != null ? returnUrl : "/cart");
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long productId,
            HttpSession session,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        cartSvc.getCart(session).remove(productId, null);
        return "redirect:" + (returnUrl != null ? returnUrl : "/cart");
    }

    @PostMapping("/clear")
    public String clear(HttpSession session,
            @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        cartSvc.getCart(session).clear();
        // đồng thời bỏ mã giảm giá đang áp (nếu có)
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.setAttribute("COUPON_MSG", "Đã xóa giỏ hàng");
        return "redirect:" + (returnUrl != null ? returnUrl : "/cart");
    }

    // ===== ÁP / BỎ MÃ GIẢM GIÁ (SESSION) =====

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String code, HttpSession session) {
        var cart = cartSvc.getCart(session);
        var subtotal = cart.subtotal();
        var result = couponService.apply(code.trim(), subtotal);

        session.setAttribute("COUPON_MSG", result.message());
        if (result.ok()) {
            session.setAttribute("COUPON_CODE", code.trim());
            session.setAttribute("COUPON_VALUE", safe(result.discount()));
        } else {
            session.removeAttribute("COUPON_CODE");
            session.removeAttribute("COUPON_VALUE");
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove-coupon")
    public String removeCoupon(HttpSession session) {
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.setAttribute("COUPON_MSG", "Đã bỏ mã giảm giá");
        return "redirect:/cart";
    }

    private BigDecimal safe(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x.max(BigDecimal.ZERO);
    }
}
