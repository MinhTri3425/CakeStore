package com.cakestore.cakestore.service.user.Impl;

import com.cakestore.cakestore.config.VnpayConfig;
import com.cakestore.cakestore.service.user.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayServiceImpl implements VnpayService {

    private final VnpayConfig vnPayConfig;

    // ========= Public APIs =========

    @Override
    public String createPaymentUrl(BigDecimal amount, String orderInfo, String orderId, HttpServletRequest request) {
        try {
            long amountX100 = amount.movePointRight(2).longValueExact(); // fail nếu không “đẹp”
            Map<String, String> p = new HashMap<>();
            p.put("vnp_Version", vnPayConfig.getVersion());
            p.put("vnp_Command", vnPayConfig.getCommand());
            p.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            p.put("vnp_Amount", String.valueOf(amountX100));
            p.put("vnp_CurrCode", vnPayConfig.getCurrCode());
            p.put("vnp_TxnRef", orderId);
            p.put("vnp_OrderInfo", orderInfo);
            p.put("vnp_OrderType", vnPayConfig.getOrderType());
            p.put("vnp_Locale", resolveLocale(request));
            p.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
            p.put("vnp_IpAddr", clientIp(request));

            // timestamps Asia/Ho_Chi_Minh
            TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            fmt.setTimeZone(tz);
            Calendar cal = Calendar.getInstance(tz);
            p.put("vnp_CreateDate", fmt.format(cal.getTime()));
            cal.add(Calendar.MINUTE, 15);
            p.put("vnp_ExpireDate", fmt.format(cal.getTime()));

            // ký: encode ASCII cả key & value, sort key ASCII, nối &
            String hashData = buildHashData(p);
            String secureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            String query = buildQueryForRedirect(p) + "&vnp_SecureHash=" + secureHash;
            return vnPayConfig.getPayUrl() + "?" + query;
        } catch (Exception e) {
            log.error("[VNPAY] createPaymentUrl error", e);
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

            String hashData = buildHashData(data);
            String mySign = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            return mySign.equalsIgnoreCase(vnp_SecureHash);
        } catch (Exception e) {
            log.error("[VNPAY] validateReturnData error", e);
            return false;
        }
    }

    @Override
    public String getPaymentStatus(Map<String, String> params) {
        String resp = params.get("vnp_ResponseCode");
        String trans = params.get("vnp_TransactionStatus");
        return ("00".equals(resp) && "00".equals(trans)) ? "SUCCESS" : "PENDING_OR_FAIL";
    }

    @Override
    public boolean verifyIpnSignature(Map<String, String> params) {
        if (params == null || params.isEmpty())
            return false;
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null || vnp_SecureHash.isBlank())
            return false;

        Map<String, String> data = new HashMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");

        String hashData = buildHashData(data);
        String mySign = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        return mySign.equalsIgnoreCase(vnp_SecureHash);
    }

    @Override
    public String ipnResponse(String code, String message) {
        return "{\"RspCode\":\"" + code + "\",\"Message\":\"" + message + "\"}";
    }

    // ========= Helpers =========

    private String resolveLocale(HttpServletRequest request) {
        String lang = request.getParameter("language");
        return (lang != null && !lang.isBlank()) ? lang : vnPayConfig.getLocale();
    }

    private static String encAscii(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }

    private static String buildHashData(Map<String, String> data) {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        StringJoiner sj = new StringJoiner("&");
        for (String k : keys) {
            String v = data.get(k);
            if (v == null || v.isEmpty())
                continue;
            sj.add(encAscii(k) + "=" + encAscii(v));
        }
        return sj.toString();
    }

    private static String buildQueryForRedirect(Map<String, String> data) {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        StringJoiner sj = new StringJoiner("&");
        for (String k : keys) {
            String v = data.get(k);
            if (v == null)
                continue;
            sj.add(URLEncoder.encode(k, StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8));
        }
        return sj.toString();
    }

    private String clientIp(HttpServletRequest req) {
        String[] headers = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
        };
        for (String h : headers) {
            String ip = req.getHeader(h);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }
}
