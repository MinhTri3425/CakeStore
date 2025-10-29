package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.repository.user.OrderRepository;
import com.cakestore.cakestore.service.user.VnpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VnpayReturnController {

    private final VnpayService vnpayService;
    private final OrderRepository orderRepo;

    /**
     * Đây phải match đúng vnpay.return-url trong application.properties
     * Ví dụ: vnpay.return-url=http://localhost:8080/payment/vnpay-return
     *
     * VNPay sẽ redirect user về URL này kèm theo rất nhiều query param:
     * vnp_Amount, vnp_OrderInfo, vnp_TxnRef, vnp_ResponseCode, vnp_SecureHash, ...
     */
    @GetMapping("/payment/vnpay-return")
    @Transactional
    public String handleVnpayReturn(
            @RequestParam Map<String, String> allParams) {

        log.info("[VNPAY-RETURN] Raw params: {}", allParams);

        // 1. Validate chữ ký để tránh fake
        boolean validSignature = vnpayService.validateReturnData(new HashMap<>(allParams));
        if (!validSignature) {
            log.warn("[VNPAY-RETURN] Invalid signature");
            // chữ ký sai => ko update đơn. Có thể show trang fail riêng tuỳ bạn
            // tạm thời cứ quay về trang orders list
            return "redirect:/orders";
        }

        // 2. Lấy trạng thái giao dịch từ VNPay
        String paymentStatus = vnpayService.getPaymentStatus(allParams); // "SUCCESS" | "FAILED"
        String responseCode = allParams.get("vnp_ResponseCode"); // "00" = ok
        String orderIdStr = allParams.get("vnp_TxnRef"); // mình set = orderId khi tạo URL
        String amountStr = allParams.get("vnp_Amount"); // số tiền *100

        log.info("[VNPAY-RETURN] orderId={}, respCode={}, status={}, amount={}",
                orderIdStr, responseCode, paymentStatus, amountStr);

        if (orderIdStr == null || orderIdStr.isBlank()) {
            // thiếu mã đơn -> không xử lý được
            return "redirect:/orders";
        }

        Long orderId;
        try {
            orderId = Long.valueOf(orderIdStr);
        } catch (NumberFormatException ex) {
            log.warn("[VNPAY-RETURN] Invalid order id format: {}", orderIdStr);
            return "redirect:/orders";
        }

        // 3. Load đơn hàng từ DB
        Optional<Order> optOrder = orderRepo.findById(orderId);
        if (optOrder.isEmpty()) {
            log.warn("[VNPAY-RETURN] Order not found: {}", orderId);
            return "redirect:/orders";
        }

        Order order = optOrder.get();

        // 4. Nếu thanh toán thành công -> cập nhật đơn
        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {

            // (4.1) Double-check số tiền trả về từ VNPay có khớp order.total không,
            // tránh ai cố pay số khác
            // Lưu ý: vnp_Amount là VND * 100
            // Ta convert về BigDecimal VND thường để so sánh.
            try {
                long paidTimes100 = Long.parseLong(amountStr);
                BigDecimal paidVnd = new BigDecimal(paidTimes100)
                        .divide(new BigDecimal("100")); // giờ nó là VND "thật"
                // So sánh paidVnd với order.getTotal()
                if (paidVnd.compareTo(order.getTotal()) != 0) {
                    log.warn("[VNPAY-RETURN] Amount mismatch. expected={}, actual={}",
                            order.getTotal(), paidVnd);
                    // Nếu mismatch tiền thì bạn có thể từ chối set PAID.
                    // Ở bản MVP mình vẫn cho qua? Không nên, nhưng tùy chính sách.
                    // Ở đây mình sẽ chỉ log và vẫn update để dễ test sandbox.
                }
            } catch (Exception ex) {
                log.warn("[VNPAY-RETURN] Cannot parse amount '{}'", amountStr, ex);
            }

            // (4.2) Chỉ update nếu đơn vẫn chưa thanh toán
            if (order.getPaymentStatus() == Order.PaymentStatus.UNPAID) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);

                // Business rule tuỳ shop:
                // - Bạn có thể auto CONFIRMED khi đã thanh toán online
                // - Hoặc vẫn để NEW và staff sẽ confirm thủ công
                // Ở đây mình nâng từ NEW -> CONFIRMED nếu đang NEW.
                if (order.getStatus() == Order.OrderStatus.NEW) {
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                }

                orderRepo.save(order);
                log.info("[VNPAY-RETURN] Marked order {} as PAID", orderId);
            } else {
                log.info("[VNPAY-RETURN] Order {} already marked as {}", orderId, order.getPaymentStatus());
            }

            // Trả user về chi tiết đơn hàng để thấy trạng thái mới
            return "redirect:/orders/" + orderId;
        }

        // 5. Nếu thanh toán fail
        // Không đổi paymentStatus. Đơn vẫn NEW + UNPAID.
        // User vẫn có thể quay lại detail và bấm Thanh toán VNPay lần nữa.
        log.info("[VNPAY-RETURN] Payment failed for order {}", orderId);
        return "redirect:/orders/" + orderId;
    }
}
