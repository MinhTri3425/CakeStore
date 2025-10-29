package com.cakestore.cakestore.service.user.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.cakestore.cakestore.config.VnpayConfig;
import com.cakestore.cakestore.service.user.VnpayService;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig vnPayConfig;

    @Override
    public String createPaymentUrl(BigDecimal amount, String orderInfo, String orderId, HttpServletRequest request) {
        try {
            BigDecimal minimalAmount = BigDecimal.valueOf(1000);
            BigDecimal amountSafe = (amount == null || amount.compareTo(minimalAmount) < 0)
                    ? minimalAmount
                    : amount.setScale(0, RoundingMode.DOWN);

            long amountIn = amountSafe.multiply(BigDecimal.valueOf(100)).longValue();
            log.info("[VNPAY DEBUG] amountIn (VND*100) = {}", amountIn);

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
            vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
            vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf(amountIn));
            vnp_Params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
            vnp_Params.put("vnp_TxnRef", orderId);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());

            String finalLocale = Optional.ofNullable(request.getParameter("language"))
                    .filter(s -> !s.isBlank())
                    .orElse(vnPayConfig.getLocale());
            vnp_Params.put("vnp_Locale", finalLocale);
            vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", getClientIp(request));

            // Timestamps (GMT+7)
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", fmt.format(cld.getTime()));
            cld.add(Calendar.MINUTE, 15);
            vnp_Params.put("vnp_ExpireDate", fmt.format(cld.getTime()));

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = vnp_Params.get(key);
                if (value == null || value.isEmpty())
                    continue;

                // hashData dùng giá trị raw, encode US-ASCII để VNPay nhận đúng
                String encodedForHash = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                hashData.append(key).append("=").append(encodedForHash);

                // query string encode UTF-8 cho URL
                String encodedForQuery = URLEncoder.encode(value, StandardCharsets.UTF_8);
                query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                        .append("=").append(encodedForQuery);

                if (i < fieldNames.size() - 1) {
                    hashData.append("&");
                    query.append("&");
                }
            }

            String secureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            String finalUrl = vnPayConfig.getPayUrl() + "?" + query.toString();
            return finalUrl;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean validateReturnData(Map<String, String> params) {
        try {
            if (params == null || params.isEmpty())
                return false;

            String vnp_SecureHash = params.get("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isBlank())
                return false;

            Map<String, String> data = new HashMap<>(params);
            data.remove("vnp_SecureHash");
            data.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(data.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = data.get(key);
                if (value == null || value.isEmpty())
                    continue;
                hashData.append(key).append('=').append(encodeVnp(value));
                if (i < fieldNames.size() - 1)
                    hashData.append('&');
            }

            String mySign = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            log.info("[VNPAY DEBUG] mySign = {}", mySign);

            return mySign.equalsIgnoreCase(vnp_SecureHash);

        } catch (Exception e) {
            log.error("[VNPAY] Validate error", e);
            return false;
        }
    }

    @Override
    public String getPaymentStatus(Map<String, String> params) {
        if (params == null)
            return "FAILED";

        String respCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(respCode)) {
            if (txnStatus == null || "00".equals(txnStatus))
                return "SUCCESS";
        }
        return "FAILED";
    }

    // Helpers
    private String getClientIp(HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                int comma = ip.indexOf(',');
                return (comma > 0) ? ip.substring(0, comma).trim() : ip.trim();
            }
            ip = request.getHeader("X-Real-IP");
            return (ip != null && !ip.isBlank()) ? ip : request.getRemoteAddr();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    private String encodeVnp(String s) {
        String encoded = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encoded.replace("+", "%20");
    }
}