package com.example.demo.features.orders.controllers;

import com.example.demo.features.orders.services.payment.MoMoPaymentStrategyImpl;
import com.example.demo.features.orders.services.payment.PayPalPaymentStrategyImpl;
import com.example.demo.features.orders.services.payment.VnPayPaymentStrategyImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final VnPayPaymentStrategyImpl vnPayStrategy;
    private final MoMoPaymentStrategyImpl moMoStrategy;
    private final PayPalPaymentStrategyImpl payPalStrategy;

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("Received VNPAY IPN: {}", params);
        boolean success = vnPayStrategy.processIpn(params);
        if (success) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid Signature or Checksum"));
        }
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<String> momoIpn(@RequestBody Map<String, Object> payload) {
        log.info("Received MoMo IPN: {}", payload);
        boolean success = moMoStrategy.processIpn(payload);
        if (success) {
            return ResponseEntity.ok("MoMo IPN processed successfully");
        } else {
            return ResponseEntity.badRequest().body("MoMo IPN processing failed");
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("Received VNPAY Return: {}", params);
        boolean success = vnPayStrategy.processIpn(params);
        if (success) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("RspCode", "97", "Message", "Invalid Signature or Checksum"));
        }
    }

    @GetMapping("/momo-return")
    public ResponseEntity<Map<String, String>> momoReturn(@RequestParam Map<String, String> params) {
        log.info("Received MoMo Return: {}", params);
        boolean success = moMoStrategy.processReturn(params);
        if (success) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("RspCode", "97", "Message", "Invalid Signature or Checksum"));
        }
    }

    @GetMapping("/paypal/capture")
    public ResponseEntity<Map<String, String>> paypalCapture(@RequestParam("token") String token, @RequestParam(value = "PayerID", required = false) String payerId) {
        log.info("Received PayPal Capture for token: {}", token);
        boolean success = payPalStrategy.captureOrder(token);
        if (success) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("RspCode", "97", "Message", "PayPal capture failed"));
        }
    }

    @GetMapping("/paypal/cancel")
    public ResponseEntity<Map<String, String>> paypalCancel(@RequestParam("token") String token) {
        log.info("Received PayPal Cancel for token: {}", token);
        payPalStrategy.cancelPayment(token);
        return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Cancelled successfully"));
    }
}
