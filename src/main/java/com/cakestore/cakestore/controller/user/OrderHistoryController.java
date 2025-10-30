package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.user.OrderRepository;
import com.cakestore.cakestore.repository.user.UserRepository;
import com.cakestore.cakestore.service.user.InventoryService;
import com.cakestore.cakestore.service.user.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderHistoryController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final InventoryService inventoryService;
    private final VnpayService vnpayService;

    // ================= LIST ORDERS =================
    @GetMapping
    public String listOrders(Authentication auth, Model model) {
        // bắt buộc login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders";
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy danh sách đơn + fetch branch (repo custom của bạn)
        List<Order> orders = orderRepo.findAllByUserIdFetchBranch(currentUser.getId());

        model.addAttribute("orders", orders);
        return "order/orders"; // templates/order/orders.html
    }

    // ================= ORDER DETAIL =================
    @GetMapping("/{orderId}")
    public String orderDetail(
            @PathVariable Long orderId,
            Authentication auth,
            Model model) {

        // bắt buộc login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders/" + orderId;
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy chi tiết đơn (fetch branch + address + items + product/variant)
        Order order = orderRepo.findByIdAndUserIdFetchAll(orderId, currentUser.getId())
                .orElse(null);

        // nếu không tìm thấy hoặc không thuộc user -> quay danh sách
        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order/detail"; // templates/order/detail.html
    }

    // ================= CANCEL ORDER =================
    @PostMapping("/{orderId}/cancel")
    @Transactional
    public String cancelOrder(
            @PathVariable Long orderId,
            Authentication auth) {

        // bắt login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders/" + orderId;
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy đơn + mọi quan hệ cần thiết (branch, items, product,...)
        Order order = orderRepo.findByIdAndUserIdFetchAll(orderId, currentUser.getId())
                .orElse(null);

        if (order == null) {
            // không có đơn hoặc không thuộc user -> quay về lịch sử
            return "redirect:/orders";
        }

        // chỉ cho hủy nếu đơn còn NEW và chưa thanh toán
        boolean canCancel = order.getStatus() == Order.OrderStatus.NEW &&
                order.getPaymentStatus() == Order.PaymentStatus.UNPAID;

        if (!canCancel) {
            // không hợp lệ -> quay lại chi tiết, không đổi gì
            return "redirect:/orders/" + orderId;
        }

        // 1. Nhả lại hàng đã reserve (very important để tồn kho không bị kẹt)
        inventoryService.releaseReservedFromOrder(order);

        // 2. Cập nhật trạng thái đơn
        order.setStatus(Order.OrderStatus.CANCELED);

        // 3. Lưu lại đơn
        orderRepo.save(order);

        // xong -> quay lại chi tiết đơn
        return "redirect:/orders/" + orderId;
    }

    // ================= PAY WITH VNPAY =================
    @PostMapping("/{orderId}/pay-vnpay")
    public String payWithVnpay(
            @PathVariable Long orderId,
            Authentication auth,
            HttpServletRequest request) {
        // bắt login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders/" + orderId;
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // tìm đơn hàng thuộc user
        Order order = orderRepo.findByIdAndUserIdFetchAll(orderId, currentUser.getId())
                .orElse(null);

        if (order == null) {
            // not found hoặc không thuộc user
            return "redirect:/orders";
        }

        // chỉ cho thanh toán nếu:
        // - đơn chưa bị hủy/xử lý (status NEW),
        // - chưa thanh toán (UNPAID),
        // - phương thức thanh toán là VNPAY.
        boolean canPayNow = order.getStatus() == Order.OrderStatus.NEW &&
                order.getPaymentStatus() == Order.PaymentStatus.UNPAID &&
                order.getPaymentMethod() == Order.PaymentMethod.VNPAY;

        if (!canPayNow) {
            // ví dụ: đã thanh toán rồi, hoặc COD, hoặc đã bị cancel
            return "redirect:/orders/" + orderId;
        }

        // call VNPay service để tạo URL thanh toán
        String payUrl = vnpayService.createPaymentUrl(
                order.getTotal(), // BigDecimal
                "Thanh toan don #" + order.getId(), // vnp_OrderInfo
                String.valueOf(order.getId()), // vnp_TxnRef (unique ID)
                request // để lấy IP + locale param
        );

        // nếu tạo URL fail (ví dụ config sai) -> quay lại detail
        if (payUrl == null || payUrl.isBlank()) {
            return "redirect:/orders/" + orderId;
        }

        // redirect thẳng qua cổng VNPay sandbox
        return "redirect:" + payUrl;
    }

}
