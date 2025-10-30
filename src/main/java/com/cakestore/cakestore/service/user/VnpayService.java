package com.cakestore.cakestore.service.user;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

public interface VnpayService {

    String createPaymentUrl(BigDecimal amount, String orderInfo, String orderId, HttpServletRequest request);

    // Verify chữ ký cho cả return (GET) lẫn IPN (POST)
    boolean validateReturnData(Map<String, String> params);

    // Bạn đang dùng ở màn return để show trạng thái tạm thời
    String getPaymentStatus(Map<String, String> params);

    // === NEW: dành cho IPN ===
    boolean verifyIpnSignature(Map<String, String> params);

    // === NEW: trả JSON cho VNPay theo chuẩn IPN ===
    String ipnResponse(String code, String message);
}
