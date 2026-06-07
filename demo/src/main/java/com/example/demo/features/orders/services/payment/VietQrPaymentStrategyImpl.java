package com.example.demo.features.orders.services.payment;


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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class VietQrPaymentStrategyImpl implements PaymentStrategy {

    private static final String QR_PAYMENT_STATUS_PENDING = "PENDING";
    private static final String QR_PAYMENT_STATUS_SUCCESS = "SUCCESS";
    private static final String QR_PAYMENT_STATUS_WRONG_AMOUNT = "WRONG_AMOUNT";
    private static final String QR_PAYMENT_STATUS_CANCELLED = "CANCELLED";
    private static final String QR_BASE_URL = "https://qr.sepay.vn/img";

    private final OrderService orderService;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, PendingQrPaymentSession> pendingQrPaymentSessions = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Value("${app.qr.account-number:${payment.qr.account-number:0359537981}}")
    private String qrAccountNumber;

    @Value("${app.qr.bank-code:${payment.qr.bank-code:MB}}")
    private String qrBankCode;

    @Value("${app.rio.poll-url:${payment.qr.verify-url:https://thanhdat050625.onrender.com/api/rio/cnpm/sent}}")
    private String qrVerifyUrl;

    @Value("${app.rio.timeout-ms:8000}")
    private long qrVerifyTimeoutMs;

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public PaymentResponse preparePayment(PaymentPrepareRequest request) {
        accessControlService.requireCustomerAccess(request.getCustomerId());

        customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        OrderRequest orderRequest = request.toOrderRequest();
        long totalAmount = orderService.calculateTotalAmount(orderRequest);

        String orderId = UUID.randomUUID().toString();
        pendingQrPaymentSessions.put(
                orderId,
                new PendingQrPaymentSession(orderId, request.getCustomerId(), orderRequest, totalAmount)
        );

        return PaymentResponse.builder()
                .orderId(orderId)
                .accountNumber(qrAccountNumber)
                .bankCode(qrBankCode)
                .amount(totalAmount)
                .description(orderId)
                .qrUrl(buildQrUrl(orderId, totalAmount))
                .build();
    }

    @Override
    public PaymentStatusResponse verifyPayment(String orderId) {
        PendingQrPaymentSession session = pendingQrPaymentSessions.get(orderId);
        if (session == null) {
            return PaymentStatusResponse.builder()
                    .orderId(orderId)
                    .status(QR_PAYMENT_STATUS_CANCELLED)
                    .expectedAmount(0L)
                    .receivedAmount(null)
                    .message("Phiên thanh toán đã không còn tồn tại. Vui lòng tạo lại mã QR.")
                    .order(null)
                    .build();
        }

        accessControlService.requireCustomerAccess(session.customerId());

        synchronized (session) {
            if (session.isCancelled()) {
                return buildQrStatusResponse(session, QR_PAYMENT_STATUS_CANCELLED, session.getLastMessage(), null);
            }

            if (session.isCompleted()) {
                OrderResponse order = findOrderResponse(session.getCreatedOrderId());
                return buildQrStatusResponse(session, QR_PAYMENT_STATUS_SUCCESS, "Thanh toán thành công.", order);
            }

            RioPaymentResult rioPayment = queryRioPayment(orderId);
            session.setReceivedAmount(rioPayment.amount());

            if (!rioPayment.paid()) {
                return buildQrStatusResponse(session, QR_PAYMENT_STATUS_PENDING, "Chưa nhận được thanh toán.", null);
            }

            if (rioPayment.amount() != session.expectedAmount()) {
                return buildQrStatusResponse(
                        session,
                        QR_PAYMENT_STATUS_WRONG_AMOUNT,
                        "Đã nhận thanh toán nhưng sai số tiền. Vui lòng chuyển đúng số tiền của đơn hàng.",
                        null
                );
            }

            OrderResponse createdOrder = orderService.createOrder(buildPaidOrderRequest(session.orderRequest()));
            cartService.clearCart(session.customerId(), true);
            session.complete(createdOrder.getId(), rioPayment.amount());
            return buildQrStatusResponse(session, QR_PAYMENT_STATUS_SUCCESS, "Thanh toán thành công.", createdOrder);
        }
    }

    @Override
    public void cancelPayment(String orderId) {
        PendingQrPaymentSession session = pendingQrPaymentSessions.get(orderId);
        if (session == null) {
            return;
        }

        accessControlService.requireCustomerAccess(session.customerId());

        synchronized (session) {
            if (session.isCompleted()) {
                throw new IllegalStateException("Đơn hàng đã được thanh toán thành công, không thể hủy phiên.");
            }
            session.cancel("Bạn đã hủy thanh toán.");
        }
    }

    private OrderRequest buildPaidOrderRequest(OrderRequest source) {
        OrderRequest.PaymentRequest paymentRequest = new OrderRequest.PaymentRequest();
        paymentRequest.setMethod(PaymentMethod.BANK_TRANSFER);
        paymentRequest.setStatus(PaymentStatus.COMPLETED);
        paymentRequest.setIsPaid(true);
        paymentRequest.setPaymentDate(new Date());
        source.setPayment(paymentRequest);
        return source;
    }

    private String buildQrUrl(String orderId, long amount) {
        String description = URLEncoder.encode(orderId, StandardCharsets.UTF_8);
        return QR_BASE_URL + "?acc=" + qrAccountNumber + "&bank=" + qrBankCode + "&amount=" + amount + "&des=" + description;
    }

    private PaymentStatusResponse buildQrStatusResponse(
            PendingQrPaymentSession session,
            String status,
            String message,
            OrderResponse order
    ) {
        return PaymentStatusResponse.builder()
                .orderId(session.orderId())
                .status(status)
                .expectedAmount(session.expectedAmount())
                .receivedAmount(session.getReceivedAmount())
                .message(message)
                .order(order)
                .build();
    }

    private OrderResponse findOrderResponse(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return null;
        }
        return orderService.findById(orderId).orElse(null);
    }

    private RioPaymentResult queryRioPayment(String orderId) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of("order_id", orderId));
            long timeoutMs = qrVerifyTimeoutMs > 0 ? qrVerifyTimeoutMs : 8000L;
            HttpRequest request = HttpRequest.newBuilder(URI.create(qrVerifyUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("QR verify gateway responded {} for order {}", response.statusCode(), orderId);
                return new RioPaymentResult(false, 0L);
            }
            JsonNode body = objectMapper.readTree(response.body());
            boolean paid = body.path("status").asBoolean(false);
            long amount = body.path("amount").asLong(0L);
            return new RioPaymentResult(paid, amount);
        } catch (Exception ex) {
            log.warn("Failed to verify QR payment for order {}", orderId, ex);
            return new RioPaymentResult(false, 0L);
        }
    }

    private record RioPaymentResult(boolean paid, long amount) {
    }

    private enum QrSessionState {
        PENDING,
        COMPLETED,
        CANCELLED
    }

    private static final class PendingQrPaymentSession {
        private final String orderId;
        private final String customerId;
        private final OrderRequest orderRequest;
        private final long expectedAmount;
        private QrSessionState state = QrSessionState.PENDING;
        private String createdOrderId;
        private Long receivedAmount;
        private String lastMessage;

        private PendingQrPaymentSession(
                String orderId,
                String customerId,
                OrderRequest orderRequest,
                long expectedAmount
        ) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.orderRequest = orderRequest;
            this.expectedAmount = expectedAmount;
        }

        private String orderId() {
            return orderId;
        }

        private String customerId() {
            return customerId;
        }

        private OrderRequest orderRequest() {
            return orderRequest;
        }

        private long expectedAmount() {
            return expectedAmount;
        }

        private void complete(String orderId, Long amount) {
            this.state = QrSessionState.COMPLETED;
            this.createdOrderId = orderId;
            this.receivedAmount = amount;
            this.lastMessage = "Payment completed.";
        }

        private void cancel(String message) {
            this.state = QrSessionState.CANCELLED;
            this.lastMessage = message;
        }

        private boolean isCompleted() {
            return this.state == QrSessionState.COMPLETED;
        }

        private boolean isCancelled() {
            return this.state == QrSessionState.CANCELLED;
        }

        private void setReceivedAmount(Long receivedAmount) {
            this.receivedAmount = receivedAmount;
        }

        private Long getReceivedAmount() {
            return receivedAmount;
        }

        private String getCreatedOrderId() {
            return createdOrderId;
        }

        private String getLastMessage() {
            return lastMessage;
        }
    }
}
