package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.repository.user.*;
import com.cakestore.cakestore.service.user.BranchContextService;
import com.cakestore.cakestore.service.user.CartSessionService;
import com.cakestore.cakestore.service.user.SessionCart;
import com.cakestore.cakestore.service.user.SessionCart.Line;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout") // giữ nguyên: /checkout
public class CheckoutController {

    private final CartSessionService cartSvc;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final BranchRepository branchRepo;
    private final AddressRepository addressRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository productVariantRepo;
    private final BranchInventoryRepository branchInventoryRepo;
    private final CouponRepository couponRepo;

    // đọc branch từ cookie/session
    private final BranchContextService branchCtx;

    // ===================== GET /checkout =====================
    @GetMapping
    public String checkoutPage(HttpServletRequest request,
            HttpSession session,
            Authentication auth,
            Model model) {

        // bắt login → next=/checkout
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/checkout";
        }

        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // giỏ hàng phải có hàng
        SessionCart cart = cartSvc.getCart(session);
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        // resolve branch hiện tại từ cookie/session
        Optional<Branch> activeBranchOpt = branchCtx.resolveCurrentBranch(session, request);
        Branch activeBranch = activeBranchOpt.orElse(null);

        // build model attribute cho view
        prepareCheckoutModel(session, currentUser, cart, activeBranch, model);

        return "checkout/checkout"; // templates/checkout/checkout.html
    }

    // ===================== POST /checkout =====================
    @PostMapping
    @Transactional
    public String placeOrder(HttpServletRequest request,
            HttpSession session,
            Authentication auth,
            Model model,
            @RequestParam("addressId") Long addressId,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethodStr) {

        // login check → next=/checkout
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/checkout";
        }

        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // giỏ phải có hàng
        SessionCart cart = cartSvc.getCart(session);
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart";
        }

        // branch giao hàng từ context
        Optional<Branch> activeBranchOpt = branchCtx.resolveCurrentBranch(session, request);
        Branch branch = activeBranchOpt.orElse(null);
        if (branch == null) {
            model.addAttribute("error", "Chọn chi nhánh giao hàng trước khi đặt đơn.");
            prepareCheckoutModel(session, currentUser, cart, null, model);
            return "checkout/checkout";
        }

        // địa chỉ user đã chọn phải thuộc về user
        Address chosen = addressRepo.findById(addressId)
                .filter(a -> a.getUser().getId().equals(currentUser.getId()))
                .orElse(null);
        if (chosen == null) {
            model.addAttribute("error", "Địa chỉ giao hàng không hợp lệ.");
            prepareCheckoutModel(session, currentUser, cart, branch, model);
            return "checkout/checkout";
        }

        // ======== TÍNH TIỀN FINAL (CÓ COUPON) ========
        BigDecimal subtotal = calcSubTotal(cart);

        // đọc coupon từ session và verify lại (branch, hạn dùng, minSubtotal,...)
        CouponCalcResult coup = computeCouponEffect(session, branch, subtotal);

        BigDecimal discount = coup.discount(); // tiền giảm trên hàng
        BigDecimal shippingFee = coup.shippingFeeOverride() != null
                ? coup.shippingFeeOverride()
                : calcShippingFeeDefault(branch, cart); // nếu coupon free ship thì 0

        BigDecimal total = subtotal.subtract(discount).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        // ================== BUILD ORDER ==================
        Order order = new Order(currentUser, branch);

        // snapshot cố định địa chỉ giao hàng (tránh phụ thuộc động)
        order.snapshotShippingFrom(chosen);

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingFee(shippingFee);
        order.setTotal(total);

        order.setPaymentMethod(safePaymentMethod(paymentMethodStr));
        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
        order.setStatus(Order.OrderStatus.NEW); // NEW = chờ duyệt
        order.setNote(note);

        // map cart -> order items + reserve tồn kho
        for (Line line : cart.items()) {
            Product product = productRepo.findById(line.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + line.getProductId()));

            ProductVariant variant = null;
            if (line.getVariantId() != null) {
                variant = productVariantRepo.findById(line.getVariantId()).orElse(null);
            }

            int qtyWanted = line.getQty();

            // kiểm tra / giữ tồn kho chi nhánh
            BranchInventory inv = branchInventoryRepo
                    .findByBranch_IdAndProduct_Id(branch.getId(), product.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy tồn kho cho chi nhánh " + branch.getId()
                                    + " & SP " + product.getId()));

            int available = inv.getAvailable(); // quantity - reserved
            if (available < qtyWanted) {
                throw new RuntimeException(
                        "Sản phẩm '" + line.getName()
                                + "' không đủ số lượng tại chi nhánh " + branch.getName());
            }

            inv.setReserved(inv.getReserved() + qtyWanted);
            branchInventoryRepo.save(inv);

            // tạo OrderItem
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setVariant(variant);

            oi.setQuantity(qtyWanted);
            oi.setUnitPrice(line.getPrice()); // đơn giá tại thời điểm mua
            oi.setNameSnapshot(line.getName()); // snapshot tên hiển thị tại thời điểm mua

            order.addItem(oi);
        }

        // lưu order (cascade OrderItem)
        orderRepo.save(order);

        // clear cart trong session
        cartSvc.clearCart(session);

        // clear coupon sau khi dùng
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.removeAttribute("COUPON_MSG");

        // 🔁 GIỮ NGUYÊN LEGACY ROUTE: /orders/{id}
        return "redirect:/orders/" + order.getId();
    }

    // ===================== helpers =====================

    /**
     * Chuẩn bị attribute cho checkout.html (GET /checkout & POST lỗi).
     * Đã tính giảm giá + phí ship (bao gồm freeship nếu coupon loại SHIPPING_OFF).
     */
    private void prepareCheckoutModel(HttpSession session,
            User currentUser,
            SessionCart cart,
            Branch activeBranch,
            Model model) {

        BigDecimal subtotal = calcSubTotal(cart);

        // áp coupon (giảm hàng + freeship nếu có)
        CouponCalcResult coup = computeCouponEffect(session, activeBranch, subtotal);

        BigDecimal discount = coup.discount();
        BigDecimal shippingFee = coup.shippingFeeOverride() != null
                ? coup.shippingFeeOverride()
                : calcShippingFeeDefault(activeBranch, cart);

        BigDecimal total = subtotal.subtract(discount).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        // danh sách địa chỉ user, default lên trước
        List<Address> addrList = addressRepo
                .findByUserIdOrderByIsDefaultDescUpdatedAtDesc(currentUser.getId());

        Long selectedAddressId = null;
        if (!addrList.isEmpty()) {
            selectedAddressId = addrList.get(0).getId(); // địa chỉ default đứng đầu list
        }

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);

        model.addAttribute("activeBranch", activeBranch);
        model.addAttribute("addresses", addrList);
        model.addAttribute("selectedAddressId", selectedAddressId);

        // show lại coupon/code/message nếu muốn
        model.addAttribute("couponMsg", session.getAttribute("COUPON_MSG"));
        model.addAttribute("couponCode", session.getAttribute("COUPON_CODE"));
    }

    // ---------- Coupon logic tái sử dụng ở checkout ----------
    private record CouponCalcResult(BigDecimal discount, BigDecimal shippingFeeOverride) {
    }

    private CouponCalcResult computeCouponEffect(HttpSession session,
            Branch currentBranch,
            BigDecimal subtotalRaw) {

        BigDecimal subtotal = (subtotalRaw == null) ? BigDecimal.ZERO : subtotalRaw;

        Object codeObj = session.getAttribute("COUPON_CODE");
        if (!(codeObj instanceof String couponCode) || couponCode.isBlank()) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }

        Optional<Coupon> couponOpt = couponRepo.findByCodeIgnoreCaseAndIsActiveTrue(couponCode);
        if (couponOpt.isEmpty()) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }
        Coupon coupon = couponOpt.get();

        // thời gian / lượt
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActiveNow(now)) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }

        // ràng buộc chi nhánh
        if (coupon.getBranch() != null) {
            if (currentBranch == null ||
                    !coupon.getBranch().getId().equals(currentBranch.getId())) {
                return new CouponCalcResult(BigDecimal.ZERO, null);
            }
        }

        // minSubtotal
        if (coupon.getMinSubtotal() != null &&
                subtotal.compareTo(coupon.getMinSubtotal()) < 0) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }

        // tính theo loại
        switch (coupon.getType()) {
            case PERCENT -> {
                BigDecimal percent = safe(coupon.getValue());
                BigDecimal raw = subtotal.multiply(percent).divide(BigDecimal.valueOf(100));

                if (coupon.getMaxDiscount() != null && raw.compareTo(coupon.getMaxDiscount()) > 0) {
                    raw = coupon.getMaxDiscount();
                }
                if (raw.compareTo(BigDecimal.ZERO) < 0)
                    raw = BigDecimal.ZERO;
                if (raw.compareTo(subtotal) > 0)
                    raw = subtotal;

                return new CouponCalcResult(raw, null);
            }
            case AMOUNT -> {
                BigDecimal flat = safe(coupon.getValue());
                if (flat.compareTo(BigDecimal.ZERO) < 0)
                    flat = BigDecimal.ZERO;
                if (coupon.getMaxDiscount() != null && flat.compareTo(coupon.getMaxDiscount()) > 0) {
                    flat = coupon.getMaxDiscount();
                }
                if (flat.compareTo(subtotal) > 0)
                    flat = subtotal;

                return new CouponCalcResult(flat, null);
            }
            case SHIPPING_OFF -> {
                // freeship: discount hàng = 0, phí ship = 0
                return new CouponCalcResult(BigDecimal.ZERO, BigDecimal.ZERO);
            }
            default -> {
                return new CouponCalcResult(BigDecimal.ZERO, null);
            }
        }
    }

    private BigDecimal safe(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    // ---------- tiền cơ bản ----------
    private BigDecimal calcSubTotal(SessionCart cart) {
        if (cart == null)
            return BigDecimal.ZERO;
        BigDecimal x = cart.subtotal();
        return x == null ? BigDecimal.ZERO : x;
    }

    /** phí ship mặc định (nếu coupon không free ship) */
    private BigDecimal calcShippingFeeDefault(Branch branch, SessionCart cart) {
        // TODO: tính phí ship động theo branch / khoảng cách
        return new BigDecimal("20000");
    }

    private Order.PaymentMethod safePaymentMethod(String raw) {
        if (raw == null || raw.isBlank())
            return null;
        return Order.PaymentMethod.of(raw);
    }
}
