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

import com.example.demo.features.auth.events.PasswordResetEvent;
import com.example.demo.features.auth.events.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;
    private final JwtService jwtService;
    private final UserProfileService userProfileService;
    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, PendingRegistration> pendingRegistrations = new ConcurrentHashMap<>();

    public OtpResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }
         
        RegisterRequest pendingRequest = copyRegisterRequest(request);
        pendingRequest.setPassword(passwordEncoder.encode(request.getPassword()));

        long expiresInSeconds = emailOtpService.sendRegistrationOtp(request.getEmail());
        String emailKey = normalizeEmail(request.getEmail());
        pendingRegistrations.put(emailKey,
            new PendingRegistration(pendingRequest, Instant.now().plusSeconds(expiresInSeconds)));

        return OtpResponse.builder()
                .message("OTP has been sent to email. Please verify to complete registration")
                .email(request.getEmail())
                .expiresInSeconds(expiresInSeconds)
                .build();
    }

    public Optional<AuthResponse> verifyRegisterOtp(VerifyEmailOtpRequest request) {
        String emailKey = normalizeEmail(request.getEmail());
        PendingRegistration pending = pendingRegistrations.get(emailKey);
        if (pending == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(pending.getExpiresAt())) {
            pendingRegistrations.remove(emailKey);
            return Optional.empty();
        }

        boolean validOtp = emailOtpService.verifyRegistrationOtp(request.getEmail(), request.getOtp());
        if (!validOtp) {
            return Optional.empty();
        }

        RegisterRequest registerRequest = pending.getRegisterRequest();
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            pendingRegistrations.remove(emailKey);
            throw new IllegalStateException("Email already exists");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            pendingRegistrations.remove(emailKey);
            throw new IllegalStateException("Username already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(registerRequest.getUsername());
        customer.setPassword(registerRequest.getPassword());
        customer.setFullName(registerRequest.getFullName());
        customer.setEmail(registerRequest.getEmail());
        customer.setPhone(registerRequest.getPhone());
        customer.setAddress(registerRequest.getAddress());
        customer.setGender(registerRequest.getGender());
        customer.setRole(UserRole.CUSTOMER);

        User savedUser = userRepository.save(Objects.requireNonNull(customer));
        pendingRegistrations.remove(emailKey);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser));

        return Optional.of(buildAuthResponse(savedUser, "Register successful"));
    }

    @Transactional(readOnly = true)
    public Optional<AuthResponse> login(LoginRequest request) {
        String identifier = normalizeLoginIdentifier(request.getUsernameOrEmail());
        Optional<User> optionalUser = userRepository.findByUsername(identifier);
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByEmail(normalizeEmail(identifier));
        }
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
        if (Boolean.FALSE.equals(user.getIsActive())) {
            return Optional.empty();
        }

        boolean validPassword = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!validPassword) {
            return Optional.empty();
        }

        return Optional.of(buildAuthResponse(user, "Login successful"));
    }
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        long expiresInSeconds = 600;
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            expiresInSeconds = emailOtpService.sendPasswordResetOtp(request.getEmail());
        }

        return OtpResponse.builder()
                .message("If the email exists, an OTP has been sent")
                .email(request.getEmail())
                .expiresInSeconds(expiresInSeconds)
                .build();
    }
    public boolean resetPassword(ResetPasswordRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return false;
        }

        boolean validOtp = emailOtpService.verifyPasswordResetOtp(request.getEmail(), request.getOtp());
        if (!validOtp) {
            return false;
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        eventPublisher.publishEvent(new PasswordResetEvent(this, user));
        return true;
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        String accessToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .message(message)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
            .tokenType("Bearer")
            .accessToken(accessToken)
            .expiresInSeconds(jwtService.getExpirationInSeconds())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeLoginIdentifier(String identifier) {
        return identifier == null ? "" : identifier.trim();
    }

    private RegisterRequest copyRegisterRequest(RegisterRequest request) {
        RegisterRequest copy = new RegisterRequest();
        copy.setUsername(request.getUsername());
        copy.setPassword(request.getPassword());
        copy.setFullName(request.getFullName());
        copy.setEmail(request.getEmail());
        copy.setPhone(request.getPhone());
        copy.setAddress(request.getAddress());
        copy.setGender(request.getGender());
        return copy;
    }

    private static class PendingRegistration {
        private final RegisterRequest registerRequest;
        private final Instant expiresAt;

        private PendingRegistration(RegisterRequest registerRequest, Instant expiresAt) {
            this.registerRequest = registerRequest;
            this.expiresAt = expiresAt;
        }

        private RegisterRequest getRegisterRequest() {
            return registerRequest;
        }

        private Instant getExpiresAt() {
            return expiresAt;
        }
    }
}

