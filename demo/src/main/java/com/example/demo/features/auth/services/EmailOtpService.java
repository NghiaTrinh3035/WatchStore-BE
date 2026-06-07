package com.example.demo.features.auth.services;


import com.example.demo.features.communications.controllers.*;
import com.example.demo.features.warranty.dtos.request.*;
import com.example.demo.features.communications.entities.*;
import com.example.demo.features.auth.dtos.response.*;
import com.example.demo.features.inventory.services.*;
import com.example.demo.features.orders.dtos.request.*;
import com.example.demo.features.auth.dtos.request.*;
import com.example.demo.features.reports.dtos.response.*;
import com.example.demo.features.reports.services.*;
import com.example.demo.features.orders.entities.*;
import com.example.demo.features.inventory.controllers.*;
import com.example.demo.features.vouchers.dtos.response.*;
import com.example.demo.features.communications.repositories.*;
import com.example.demo.features.inventory.dtos.request.*;
import com.example.demo.core.enums.*;
import com.example.demo.core.dtos.response.*;
import com.example.demo.features.auth.controllers.*;
import com.example.demo.features.reports.repositories.*;
import com.example.demo.core.services.*;
import com.example.demo.features.vouchers.controllers.*;
import com.example.demo.features.warranty.services.*;
import com.example.demo.features.communications.dtos.response.*;
import com.example.demo.features.orders.dtos.response.*;
import com.example.demo.core.exceptions.*;
import com.example.demo.features.reports.dtos.request.*;
import com.example.demo.features.auth.services.*;
import com.example.demo.features.users.dtos.response.*;
import com.example.demo.features.users.services.*;
import com.example.demo.features.users.controllers.*;
import com.example.demo.features.products.dtos.response.*;
import com.example.demo.core.config.*;
import com.example.demo.features.orders.services.payment.*;
import com.example.demo.features.vouchers.dtos.request.*;
import com.example.demo.features.products.services.*;
import com.example.demo.features.vouchers.services.*;
import com.example.demo.core.entities.*;
import com.example.demo.features.warranty.entities.*;
import com.example.demo.features.inventory.dtos.response.*;
import com.example.demo.features.warranty.controllers.*;
import com.example.demo.features.users.entities.*;
import com.example.demo.features.products.dtos.request.*;
import com.example.demo.features.warranty.repositories.*;
import com.example.demo.features.inventory.repositories.*;
import com.example.demo.features.communications.dtos.request.*;
import com.example.demo.features.warranty.dtos.response.*;
import com.example.demo.features.orders.controllers.*;
import com.example.demo.features.products.entities.*;
import com.example.demo.features.vouchers.entities.*;
import com.example.demo.features.products.controllers.*;
import com.example.demo.features.reports.controllers.*;
import com.example.demo.features.inventory.entities.*;
import com.example.demo.features.communications.services.*;
import com.example.demo.features.orders.services.*;
import com.example.demo.features.users.dtos.request.*;
import com.example.demo.features.reports.entities.*;
import com.example.demo.core.common.*;
import com.example.demo.features.products.repositories.*;
import com.example.demo.features.orders.repositories.*;
import com.example.demo.features.users.repositories.*;
import com.example.demo.features.vouchers.repositories.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class EmailOtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_SECONDS = 600;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final String REGISTER_PURPOSE = "REGISTER_EMAIL";
    private static final String RESET_PASSWORD_PURPOSE = "RESET_PASSWORD";

    private final JavaMailSender mailSender;
    private final String mailFromAddress;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpPayload> otpStore = new ConcurrentHashMap<>();

    public EmailOtpService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${spring.mail.username:}}") String mailFromAddress
    ) {
        this.mailSender = mailSender;
        this.mailFromAddress = mailFromAddress;
    }

    public long sendRegistrationOtp(String email) {
        return sendOtp(email, REGISTER_PURPOSE, "Email verification OTP");
    }

    public boolean verifyRegistrationOtp(String email, String otp) {
        return verifyOtp(email, otp, REGISTER_PURPOSE);
    }

    public long sendPasswordResetOtp(String email) {
        return sendOtp(email, RESET_PASSWORD_PURPOSE, "Password reset OTP");
    }

    public boolean verifyPasswordResetOtp(String email, String otp) {
        return verifyOtp(email, otp, RESET_PASSWORD_PURPOSE);
    }

    private long sendOtp(String email, String purpose, String subject) {
        String otp = generateNumericOtp();
        Instant expiresAt = Instant.now().plusSeconds(OTP_TTL_SECONDS);

        otpStore.put(buildKey(email, purpose), new OtpPayload(otp, expiresAt, 0));
        sendOtpEmail(email, otp, subject);
        return OTP_TTL_SECONDS;
    }

    private boolean verifyOtp(String email, String otp, String purpose) {
        String key = buildKey(email, purpose);
        OtpPayload payload = otpStore.get(key);

        if (payload == null) {
            return false;
        }

        if (Instant.now().isAfter(payload.getExpiresAt())) {
            otpStore.remove(key);
            return false;
        }

        if (payload.getFailedAttempts() >= MAX_VERIFY_ATTEMPTS) {
            otpStore.remove(key);
            return false;
        }

        if (payload.getOtp().equals(otp)) {
            otpStore.remove(key);
            return true;
        }

        payload.setFailedAttempts(payload.getFailedAttempts() + 1);
        if (payload.getFailedAttempts() >= MAX_VERIFY_ATTEMPTS) {
            otpStore.remove(key);
        }
        return false;
    }

    private String generateNumericOtp() {
        int upperBound = (int) Math.pow(10, OTP_LENGTH);
        int lowerBound = (int) Math.pow(10, OTP_LENGTH - 1);
        int otpValue = secureRandom.nextInt(upperBound - lowerBound) + lowerBound;
        return String.valueOf(otpValue);
    }

    private void sendOtpEmail(String email, String otp, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFromAddress != null && !mailFromAddress.isBlank()) {
            message.setFrom(mailFromAddress.trim());
        }
        message.setTo(email);
        message.setSubject(subject);
        message.setText("Your OTP code is: " + otp + "\nThis code will expire in 10 minutes.");

        try {
            // Log OTP for development/debugging so it can be verified without email delivery
            log.info("Generated OTP for {}: {}", email, otp);

            // Send email asynchronously to avoid blocking the registration request
            new Thread(() -> {
                try {
                    mailSender.send(message);
                    log.info("OTP email sent to {}", email);
                } catch (MailException ex) {
                    log.error("Failed to send OTP email to {}", email, ex);
                }
            }, "otp-email-sender").start();
        } catch (Exception ex) {
            // Ensure that email delivery problems do not break registration flow
            log.error("Failed to schedule OTP email to {}", email, ex);
        }
    }

    private String buildKey(String email, String purpose) {
        return purpose + ":" + email.toLowerCase();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class OtpPayload {
        private String otp;
        private Instant expiresAt;
        private int failedAttempts;
    }
}



