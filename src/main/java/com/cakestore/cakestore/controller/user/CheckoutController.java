package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.repository.user.AddressRepository;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
import com.cakestore.cakestore.repository.user.BranchRepository;
import com.cakestore.cakestore.repository.user.CouponRepository;
import com.cakestore.cakestore.repository.user.OrderRepository;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.repository.user.ProductVariantRepository;
import com.cakestore.cakestore.service.user.CartSessionService;
import com.cakestore.cakestore.service.user.SessionCart;
import com.cakestore.cakestore.service.user.SessionCart.Line;
import jakarta.servlet.http.Cookie;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartSessionService cartSvc;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final BranchRepository branchRepo;
    private final AddressRepository addressRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository productVariantRepo;
    private final BranchInventoryRepository branchInventoryRepo;

    // để đọc lại coupon ở bước checkout
    private final CouponRepository couponRepo;

    // ===================== GET /checkout =====================
    @GetMapping("/checkout")
    public String checkoutPage(
            HttpServletRequest request,
            HttpSession session,
            Authentication auth,
            Model model) {

        // bắt login vì Order.user NOT NULL
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

        // đồng bộ session.activeBranchId từ cookie BRANCH_ID (cookie thắng)
        syncBranchFromCookie(request, session);

        // build model attribute cho view (lúc GET)
        prepareCheckoutModel(session, currentUser, cart, model);

        return "checkout/checkout"; // templates/checkout/checkout.html
    }

    // ===================== POST /checkout =====================
    @PostMapping("/checkout")
    @Transactional
    public String placeOrder(
            HttpSession session,
            Authentication auth,
            Model model,

            @RequestParam("addressId") Long addressId,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethodStr) {

        // login check
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

        // branch giao hàng lấy từ session (đã sync ở GET bằng cookie)
        Long activeBranchId = (Long) session.getAttribute("activeBranchId");
        Branch branch = (activeBranchId != null)
                ? branchRepo.findById(activeBranchId).orElse(null)
                : null;
        if (branch == null) {
            model.addAttribute("error", "Chọn chi nhánh giao hàng trước khi đặt đơn.");
            // rebuild model để hiển thị lại trang với thông báo lỗi
            prepareCheckoutModel(session, currentUser, cart, model);
            return "checkout/checkout";
        }

        // địa chỉ user đã chọn phải thuộc về user
        Address chosen = addressRepo.findById(addressId)
                .filter(a -> a.getUser().getId().equals(currentUser.getId()))
                .orElse(null);
        if (chosen == null) {
            model.addAttribute("error", "Địa chỉ giao hàng không hợp lệ.");
            prepareCheckoutModel(session, currentUser, cart, model);
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

        BigDecimal total = subtotal
                .subtract(discount)
                .add(shippingFee);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        // ================== BUILD ORDER ==================
        Order order = new Order(currentUser, branch);

        // nếu DB cho phép AddressId NULL, KHÔNG setAddress để tránh phụ thuộc động
        // order.setAddress(chosen);

        // snapshot cố định địa chỉ giao hàng
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

        // clear coupon sau khi dùng, để không reuse cho đơn tiếp theo
        session.removeAttribute("COUPON_CODE");
        session.removeAttribute("COUPON_VALUE");
        session.removeAttribute("COUPON_MSG");

        // redirect sang chi tiết đơn
        return "redirect:/orders/" + order.getId();
    }

    // ===================== helpers =====================

    /**
     * Đồng bộ chi nhánh vào session.activeBranchId dựa trên cookie BRANCH_ID.
     * Cookie luôn thắng. Nếu cookie không có -> xoá session.activeBranchId.
     * Kết quả: checkout luôn hiển thị đúng chi nhánh user vừa chọn ở header.
     */
    private void syncBranchFromCookie(HttpServletRequest request, HttpSession session) {
        Long cookieBranchId = readBranchIdFromCookie(request);
        if (cookieBranchId != null) {
            session.setAttribute("activeBranchId", cookieBranchId);
        } else {
            session.removeAttribute("activeBranchId");
        }
    }

    private Long readBranchIdFromCookie(HttpServletRequest request) {
        if (request == null)
            return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        return Arrays.stream(cookies)
                .filter(c -> "BRANCH_ID".equals(c.getName()))
                .findFirst()
                .map(c -> {
                    try {
                        return Long.valueOf(c.getValue());
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * Chuẩn bị attribute cho checkout.html (GET /checkout và POST lỗi).
     * Đã tính giảm giá + phí ship (bao gồm freeship nếu coupon loại SHIPPING_OFF).
     */
    private void prepareCheckoutModel(
            HttpSession session,
            User currentUser,
            SessionCart cart,
            Model model) {

        // branch hiện tại trong session
        Long activeBranchId = (Long) session.getAttribute("activeBranchId");
        Branch activeBranch = null;
        if (activeBranchId != null) {
            activeBranch = branchRepo.findById(activeBranchId).orElse(null);
        }

        BigDecimal subtotal = calcSubTotal(cart);

        // áp coupon (giảm hàng + freeship nếu có)
        CouponCalcResult coup = computeCouponEffect(session, activeBranch, subtotal);

        BigDecimal discount = coup.discount();
        BigDecimal shippingFee = coup.shippingFeeOverride() != null
                ? coup.shippingFeeOverride()
                : calcShippingFeeDefault(activeBranch, cart);

        BigDecimal total = subtotal
                .subtract(discount)
                .add(shippingFee);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

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

    /**
     * Kết quả tính toán coupon ở bước checkout:
     * - discount: tiền giảm trên hàng
     * - shippingFeeOverride: nếu != null thì dùng giá trị này làm phí ship
     * (ví dụ 0 cho freeship)
     */
    private record CouponCalcResult(BigDecimal discount, BigDecimal shippingFeeOverride) {
    }

    /**
     * Đọc coupon từ session (COUPON_CODE) rồi validate lại với:
     * - branch hiện tại
     * - thời gian hiệu lực
     * - minSubtotal
     *
     * Nếu hợp lệ:
     * - Nếu coupon là % hoặc AMOUNT => trả discount tương ứng, shippingFeeOverride
     * = null
     * - Nếu coupon là SHIPPING_OFF => discount = 0, shippingFeeOverride = 0
     *
     * Nếu không hợp lệ hoặc không có mã => discount = 0, shippingFeeOverride = null
     */
    private CouponCalcResult computeCouponEffect(
            HttpSession session,
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

        // check thời gian / lượt
        LocalDateTime now = LocalDateTime.now();
        if (!coupon.isActiveNow(now)) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }

        // check ràng buộc chi nhánh
        if (coupon.getBranch() != null) {
            if (currentBranch == null ||
                    !coupon.getBranch().getId().equals(currentBranch.getId())) {
                return new CouponCalcResult(BigDecimal.ZERO, null);
            }
        }

        // check minSubtotal
        if (coupon.getMinSubtotal() != null &&
                subtotal.compareTo(coupon.getMinSubtotal()) < 0) {
            return new CouponCalcResult(BigDecimal.ZERO, null);
        }

        // tính theo loại coupon
        switch (coupon.getType()) {
            case PERCENT: {
                BigDecimal percent = safe(coupon.getValue());
                BigDecimal raw = subtotal
                        .multiply(percent)
                        .divide(BigDecimal.valueOf(100));

                // cap maxDiscount nếu có
                if (coupon.getMaxDiscount() != null &&
                        raw.compareTo(coupon.getMaxDiscount()) > 0) {
                    raw = coupon.getMaxDiscount();
                }
                if (raw.compareTo(BigDecimal.ZERO) < 0) {
                    raw = BigDecimal.ZERO;
                }
                // không giảm quá subtotal
                if (raw.compareTo(subtotal) > 0) {
                    raw = subtotal;
                }

                return new CouponCalcResult(raw, null);
            }

            case AMOUNT: {
                BigDecimal flat = safe(coupon.getValue());
                if (flat.compareTo(BigDecimal.ZERO) < 0) {
                    flat = BigDecimal.ZERO;
                }
                // cap theo maxDiscount nếu có
                if (coupon.getMaxDiscount() != null &&
                        flat.compareTo(coupon.getMaxDiscount()) > 0) {
                    flat = coupon.getMaxDiscount();
                }
                // không giảm quá subtotal
                if (flat.compareTo(subtotal) > 0) {
                    flat = subtotal;
                }

                return new CouponCalcResult(flat, null);
            }

            case SHIPPING_OFF: {
                // freeship: discount hàng = 0
                // báo phí ship = 0
                return new CouponCalcResult(BigDecimal.ZERO, BigDecimal.ZERO);
            }

            default:
                return new CouponCalcResult(BigDecimal.ZERO, null);
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

    /**
     * phí ship mặc định (nếu coupon không free ship)
     */
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
