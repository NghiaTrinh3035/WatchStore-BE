package com.example.demo.features.orders.services.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
@Getter
public class MoMoConfig {

    @Value("${MOMO_PARTNER_CODE:}")
    private String partnerCode;

    @Value("${MOMO_ACCESS_KEY:}")
    private String accessKey;

    @Value("${MOMO_SECRET_KEY:}")
    private String secretKey;

    @Value("${MOMO_ENDPOINT:}")
    private String endpoint;

    @Value("${MOMO_REDIRECT_URL:}")
    private String redirectUrl;

    @Value("${MOMO_IPN_URL:}")
    private String ipnUrl;

    public String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
