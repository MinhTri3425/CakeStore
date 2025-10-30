package com.cakestore.cakestore.config;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Getter
@Slf4j
public class VnpayConfig {

    // Merchant info
    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    // Endpoints / URLs
    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    // Static params
    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order-type}")
    private String orderType;

    @Value("${vnpay.curr-code}")
    private String currCode; // "VND"

    @Value("${vnpay.locale}")
    private String locale; // "vn" / "en"

    /**
     * Generate HMAC SHA512 signature (vnp_SecureHash)
     */
    public String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("HMAC input is null");
            }

            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);

            final byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            final StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Error generating HMAC SHA512", ex);
            return null;
        }
    }

    /**
     * Generate a random numeric string of given length.
     * Use SecureRandom for better security
     */
    public String getRandomNumber(int len) {
        final SecureRandom rnd = new SecureRandom();
        final String digits = "0123456789";
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(digits.charAt(rnd.nextInt(digits.length())));
        }
        return sb.toString();
    }
}