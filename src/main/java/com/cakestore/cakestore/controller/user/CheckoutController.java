package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.*;
import com.cakestore.cakestore.repository.user.*;
import com.cakestore.cakestore.repository.UserRepository;
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
import java.util.Arrays;
import java.util.List;

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

        // đồng bộ branchId vào session (session -> nếu null thì lấy cookie)
        syncBranchFromCookieIfNeeded(request, session);

        // build model attribute cho view
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

        // branch giao hàng: sau GET thì session đã có (hoặc user cố tình skip)
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

        // snapshot địa chỉ lúc đặt (để đơn giữ nguyên kể cả user đổi sau đó)
        Address shipAddr = new Address();
        shipAddr.setUser(currentUser);
        shipAddr.setFullName(chosen.getFullName());
        shipAddr.setPhone(chosen.getPhone());
        shipAddr.setLine1(chosen.getLine1());
        shipAddr.setWard(chosen.getWard());
        shipAddr.setDistrict(chosen.getDistrict());
        shipAddr.setCity(chosen.getCity());
        shipAddr.setDefault(false);
        addressRepo.save(shipAddr);

        // tính tiền final từ server
        BigDecimal subtotal = calcSubTotal(cart);
        BigDecimal discount = calcDiscount(cart, currentUser);
        BigDecimal shippingFee = calcShippingFee(branch, cart);
        BigDecimal total = subtotal
                .subtract(discount)
                .add(shippingFee);

        // build Order
        Order order = new Order(currentUser, branch);
        order.setAddress(shipAddr);

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

        // clear cart
        cartSvc.clearCart(session);

        // redirect sang chi tiết đơn
        return "redirect:/orders/" + order.getId();
    }

    // ===================== helper =====================

    /**
     * Đọc BRANCH_ID từ cookie và bỏ vào session.activeBranchId nếu session chưa có.
     * Dùng trong GET /checkout để "đồng bộ state đầu phiên".
     */
    private void syncBranchFromCookieIfNeeded(HttpServletRequest request, HttpSession session) {
        Long activeBranchId = (Long) session.getAttribute("activeBranchId");
        if (activeBranchId != null) {
            return; // session đã có -> thôi khỏi đọc cookie
        }
        Long cookieBranchId = readBranchIdFromCookie(request);
        if (cookieBranchId != null) {
            session.setAttribute("activeBranchId", cookieBranchId);
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
     * Chuẩn bị mọi attribute mà checkout.html cần.
     * Dùng cho cả GET (sau sync cookie -> session) và POST khi có lỗi.
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
        BigDecimal discount = calcDiscount(cart, currentUser);
        BigDecimal shippingFee = calcShippingFee(activeBranch, cart);
        BigDecimal total = subtotal
                .subtract(discount)
                .add(shippingFee);

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
    }

    private BigDecimal calcSubTotal(SessionCart cart) {
        if (cart == null)
            return BigDecimal.ZERO;
        BigDecimal x = cart.subtotal();
        return x == null ? BigDecimal.ZERO : x;
    }

    private BigDecimal calcShippingFee(Branch branch, SessionCart cart) {
        // TODO: tính phí ship động theo branch / khoảng cách
        return new BigDecimal("20000");
    }

    private BigDecimal calcDiscount(SessionCart cart, User user) {
        // TODO: voucher/loyalty/coupon
        return BigDecimal.ZERO;
    }

    private Order.PaymentMethod safePaymentMethod(String raw) {
        if (raw == null || raw.isBlank())
            return null;
        return Order.PaymentMethod.of(raw);
    }
}
