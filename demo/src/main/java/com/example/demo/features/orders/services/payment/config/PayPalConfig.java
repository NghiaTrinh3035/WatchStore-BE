package com.example.demo.features.orders.services.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Getter
public class PayPalConfig {

    @Value("${PAYPAL_CLIENT_ID:}")
    private String clientId;

    @Value("${PAYPAL_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${PAYPAL_ENVIRONMENT:sandbox}")
    private String environment;

    @Value("${PAYPAL_RETURN_URL:}")
    private String returnUrl;

    @Value("${PAYPAL_CANCEL_URL:}")
    private String cancelUrl;

    public String getBaseUrl() {
        if ("live".equalsIgnoreCase(environment)) {
            return "https://api-m.paypal.com";
        }
        return "https://api-m.sandbox.paypal.com";
    }

    public String getBasicAuthHeader() {
        String auth = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
