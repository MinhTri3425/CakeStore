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
     * Must match vnpay.return-url in application.properties
     * e.g. vnpay.return-url=http://localhost:8080/payment/vnpay-return
     */
    @GetMapping("/payment/vnpay-return")
    @Transactional
    public String handleVnpayReturn(@RequestParam Map<String, String> allParams) {

        log.info("[VNPAY-RETURN] Raw params: {}", allParams);

        // 1) Verify signature
        boolean validSignature = vnpayService.validateReturnData(new HashMap<>(allParams));
        if (!validSignature) {
            log.warn("[VNPAY-RETURN] Invalid signature");
            return "redirect:/orders";
        }

        // 2) Parse core fields
        String paymentStatus = vnpayService.getPaymentStatus(allParams); // "SUCCESS" | "PENDING_OR_FAIL"
        String orderIdStr = allParams.get("vnp_TxnRef"); // set = orderId when creating URL
        String amountStr = allParams.get("vnp_Amount"); // VND x100

        if (orderIdStr == null || orderIdStr.isBlank())
            return "redirect:/orders";

        Long orderId;
        try {
            orderId = Long.valueOf(orderIdStr);
        } catch (NumberFormatException ex) {
            log.warn("[VNPAY-RETURN] Invalid order id format: {}", orderIdStr);
            return "redirect:/orders";
        }

        // 3) Load order
        Optional<Order> optOrder = orderRepo.findById(orderId);
        if (optOrder.isEmpty()) {
            log.warn("[VNPAY-RETURN] Order not found: {}", orderId);
            return "redirect:/orders";
        }

        Order order = optOrder.get();

        // 4) If VNPay reports success → mark PAID only (do NOT change OrderStatus)
        if ("SUCCESS".equalsIgnoreCase(paymentStatus)
                && order.getPaymentStatus() == Order.PaymentStatus.UNPAID) {

            // Optional: double-check amount
            try {
                long paidTimes100 = Long.parseLong(amountStr);
                BigDecimal paidVnd = new BigDecimal(paidTimes100).divide(new BigDecimal("100"));
                if (paidVnd.compareTo(order.getTotal()) != 0) {
                    log.warn("[VNPAY-RETURN] Amount mismatch. expected={}, actual={}",
                            order.getTotal(), paidVnd);
                    // policy tùy bạn: ở đây chỉ log để tiện test
                }
            } catch (Exception ex) {
                log.warn("[VNPAY-RETURN] Cannot parse amount '{}'", amountStr, ex);
            }

            order.setPaymentStatus(Order.PaymentStatus.PAID); // ✅ chỉ set PAID
            orderRepo.save(order);
            log.info("[VNPAY-RETURN] Marked order {} as PAID via Return", orderId);
        }

        // 5) Redirect user back to order detail
        return "redirect:/orders/" + orderId;
    }
}
