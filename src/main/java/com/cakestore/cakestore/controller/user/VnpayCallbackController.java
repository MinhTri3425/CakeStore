package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.repository.user.OrderRepository;
import com.cakestore.cakestore.service.user.VnpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/vnpay")
public class VnpayCallbackController {

    private final VnpayService vnpayService;
    private final OrderRepository orderRepo;

    // Return URL (user redirect)
    @GetMapping("/return")
    public String vnpReturn(@RequestParam Map<String, String> allParams) {
        // thường: chỉ hiển thị kết quả tạm thời cho user
        boolean ok = vnpayService.validateReturnData(allParams);
        String status = vnpayService.getPaymentStatus(allParams);
        log.info("[VNPAY RETURN] valid={} status={} params={}", ok, status, allParams);
        return ok ? "VNPay return received: " + status : "Signature invalid";
    }

    // IPN URL (server-to-server) – chốt đơn
    // import java.math.BigDecimal; // <-- bỏ, không dùng

    @PostMapping("/ipn")
    @Transactional
    public String vnpIpn(HttpServletRequest request, @RequestParam Map<String, String> allParams) {
        try {
            log.info("[VNPAY IPN] params={}", allParams);

            // 0) check signature
            if (!vnpayService.verifyIpnSignature(allParams)) {
                return vnpayService.ipnResponse("97", "Signature invalid");
            }

            // 1) tối thiểu các field phải có
            String txnRef = allParams.get("vnp_TxnRef");
            String amountStr = allParams.get("vnp_Amount");
            String rspCode = allParams.get("vnp_ResponseCode");
            String transStatus = allParams.get("vnp_TransactionStatus");
            String tmnCode = allParams.get("vnp_TmnCode");

            if (txnRef == null || txnRef.isBlank() || amountStr == null || amountStr.isBlank()) {
                return vnpayService.ipnResponse("03", "Missing mandatory fields");
            }

            // (optional) khớp đúng merchant
            // nếu bạn muốn chắc kèo, expose getter tmn-code từ VnpayConfig qua VnpayService
            // rồi check:
            // if (!Objects.equals(tmnCode, vnpayService.getTmnCode())) return
            // vnpayService.ipnResponse("97","Merchant mismatch");

            Long orderId;
            long received;
            try {
                orderId = Long.valueOf(txnRef);
                received = Long.parseLong(amountStr);
            } catch (NumberFormatException nfe) {
                return vnpayService.ipnResponse("04", "Invalid amount/txnRef format");
            }

            Order order = orderRepo.findById(orderId).orElse(null);
            if (order == null) {
                return vnpayService.ipnResponse("01", "Order not found");
            }

            // idempotent
            if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
                return vnpayService.ipnResponse("00", "Order already paid");
            }

            // 2) đối soát amount (×100)
            long expectedX100 = order.getTotal().movePointRight(2).longValueExact();
            if (received != expectedX100) {
                return vnpayService.ipnResponse("04", "Invalid amount");
            }

            // 3) chỉ xác nhận khi thật sự thành công
            boolean success = "00".equals(rspCode) && "00".equals(transStatus);
            if (!success) {
                return vnpayService.ipnResponse("02", "Payment not successful");
            }

            // 4) ✅ chỉ set PAID
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderRepo.save(order);

            // TODO: lưu vnp_TransactionNo, bankCode, vnp_PayDate nếu bạn log transaction
            return vnpayService.ipnResponse("00", "Confirm Success");

        } catch (Exception e) {
            log.error("[VNPAY IPN] System error", e);
            return vnpayService.ipnResponse("99", "System error");
        }
    }

}
