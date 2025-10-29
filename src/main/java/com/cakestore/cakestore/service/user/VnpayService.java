package com.cakestore.cakestore.service.user;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public interface VnpayService {

    /**
     * Create VNPay payment URL
     * 
     * @param amount    Amount in VND
     * @param orderInfo Order information
     * @param orderId   Order ID
     * @param request   HTTP request
     * @return VNPay payment URL
     */
    String createPaymentUrl(BigDecimal amount, String orderInfo, String orderId, HttpServletRequest request);

    /**
     * Validate VNPay return data
     * 
     * @param params Query parameters from VNPay
     * @return true if valid
     */
    boolean validateReturnData(Map<String, String> params);

    /**
     * Get payment status from return params
     * 
     * @param params Query parameters from VNPay
     * @return Payment status code
     */
    String getPaymentStatus(Map<String, String> params);
}