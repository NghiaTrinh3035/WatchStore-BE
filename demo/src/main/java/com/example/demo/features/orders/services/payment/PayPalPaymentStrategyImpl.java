package com.example.demo.features.orders.services.payment;

import com.example.demo.core.enums.PaymentMethod;
import com.example.demo.core.enums.PaymentStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.features.orders.services.CartService;
import com.example.demo.features.orders.dtos.request.OrderRequest;
import com.example.demo.features.orders.dtos.request.PaymentPrepareRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.dtos.response.PaymentResponse;
import com.example.demo.features.orders.dtos.response.PaymentStatusResponse;
import com.example.demo.features.orders.services.OrderService;
import com.example.demo.features.orders.services.OrderCommandInvoker;
import com.example.demo.features.orders.commands.CreateOrderCommand;
import com.example.demo.features.orders.services.payment.config.PayPalConfig;
import com.example.demo.features.users.repositories.CustomerRepository;
import com.example.demo.core.services.AccessControlService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayPalPaymentStrategyImpl implements PaymentStrategy {

    private final OrderService orderService;
    private final OrderCommandInvoker orderCommandInvoker;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;
    private final CartService cartService;
    private final PayPalConfig payPalConfig;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, PendingPayPalSession> pendingSessions = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public PaymentResponse preparePayment(PaymentPrepareRequest request) {
        accessControlService.requireCustomerAccess(request.getCustomerId());

        customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        OrderRequest orderRequest = request.toOrderRequest();
        long totalAmountVND = orderService.calculateTotalAmount(orderRequest);

        // Convert VND to USD (approximate rate 1 USD = 25000 VND)
        double totalAmountUSD = totalAmountVND / 25000.0;
        String amountStr = String.format(java.util.Locale.US, "%.2f", totalAmountUSD);

        try {
            Map<String, Object> body = Map.of(
                    "intent", "CAPTURE",
                    "purchase_units", List.of(
                            Map.of(
                                    "amount", Map.of(
                                            "currency_code", "USD",
                                            "value", amountStr
                                    )
                            )
                    ),
                    "application_context", Map.of(
                            "return_url", payPalConfig.getReturnUrl(),
                            "cancel_url", payPalConfig.getCancelUrl()
                    )
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(payPalConfig.getBaseUrl() + "/v2/checkout/orders"))
                    .header("Authorization", payPalConfig.getBasicAuthHeader())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("PayPal API Error: {}", response.body());
                throw new RuntimeException("Error communicating with PayPal API");
            }

            JsonNode responseBody = objectMapper.readTree(response.body());
            String payPalOrderId = responseBody.path("id").asText();

            String approveUrl = null;
            for (JsonNode link : responseBody.path("links")) {
                if ("approve".equals(link.path("rel").asText())) {
                    approveUrl = link.path("href").asText();
                    break;
                }
            }

            if (approveUrl == null) {
                throw new RuntimeException("No approve URL returned from PayPal");
            }

            // We store the session keyed by PayPal's Order ID
            pendingSessions.put(payPalOrderId, new PendingPayPalSession(payPalOrderId, request.getCustomerId(), orderRequest, totalAmountVND));

            return PaymentResponse.builder()
                    .orderId(payPalOrderId)
                    .amount(totalAmountVND)
                    .description("Thanh toan don hang bang PayPal")
                    .qrUrl(approveUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error preparing PayPal payment", e);
            throw new RuntimeException("Could not generate PayPal payment URL");
        }
    }

    @Override
    public PaymentStatusResponse verifyPayment(String payPalOrderId) {
        PendingPayPalSession session = pendingSessions.get(payPalOrderId);
        if (session == null) {
            return PaymentStatusResponse.builder()
                    .orderId(payPalOrderId)
                    .status("CANCELLED")
                    .message("Phiên thanh toán PayPal không tồn tại.")
                    .build();
        }

        synchronized (session) {
            if (session.isCompleted()) {
                OrderResponse order = orderService.findById(session.getCreatedOrderId()).orElse(null);
                return PaymentStatusResponse.builder()
                        .orderId(payPalOrderId)
                        .status("SUCCESS")
                        .message("Thanh toán thành công.")
                        .order(order)
                        .build();
            }
            if (session.isCancelled()) {
                return PaymentStatusResponse.builder()
                        .orderId(payPalOrderId)
                        .status("CANCELLED")
                        .message("Đã hủy thanh toán.")
                        .build();
            }
            return PaymentStatusResponse.builder()
                    .orderId(payPalOrderId)
                    .status("PENDING")
                    .message("Đang chờ thanh toán PayPal.")
                    .build();
        }
    }

    @Override
    public void cancelPayment(String payPalOrderId) {
        PendingPayPalSession session = pendingSessions.get(payPalOrderId);
        if (session != null) {
            synchronized (session) {
                if (!session.isCompleted()) {
                    session.cancel();
                }
            }
        }
    }

    public boolean captureOrder(String payPalOrderId) {
        PendingPayPalSession session = pendingSessions.get(payPalOrderId);
        if (session == null) return false;

        synchronized (session) {
            if (session.isCompleted()) return true;

            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + payPalOrderId + "/capture"))
                        .header("Authorization", payPalConfig.getBasicAuthHeader())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    log.error("PayPal Capture Error: {}", response.body());
                    return false;
                }

                JsonNode responseBody = objectMapper.readTree(response.body());
                String status = responseBody.path("status").asText();

                if ("COMPLETED".equals(status)) {
                    OrderRequest orderRequest = session.getOrderRequest();
                    OrderRequest.PaymentRequest paymentRequest = new OrderRequest.PaymentRequest();
                    paymentRequest.setMethod(PaymentMethod.PAYPAL);
                    paymentRequest.setStatus(PaymentStatus.COMPLETED);
                    paymentRequest.setIsPaid(true);
                    paymentRequest.setPaymentDate(new Date());
                    orderRequest.setPayment(paymentRequest);

                    OrderResponse createdOrder = orderCommandInvoker.execute(new CreateOrderCommand(orderRequest));
                    cartService.clearCart(session.getCustomerId(), true);
                    session.complete(createdOrder.getId());
                    return true;
                } else {
                    log.warn("PayPal order capture returned status: {}", status);
                    return false;
                }

            } catch (Exception e) {
                log.error("Error capturing PayPal payment", e);
                return false;
            }
        }
    }

    private static class PendingPayPalSession {
        private final String payPalOrderId;
        private final String customerId;
        private final OrderRequest orderRequest;
        private final long amount;
        private boolean completed = false;
        private boolean cancelled = false;
        private String createdOrderId;

        public PendingPayPalSession(String payPalOrderId, String customerId, OrderRequest orderRequest, long amount) {
            this.payPalOrderId = payPalOrderId;
            this.customerId = customerId;
            this.orderRequest = orderRequest;
            this.amount = amount;
        }

        public String getCustomerId() { return customerId; }
        public OrderRequest getOrderRequest() { return orderRequest; }
        public boolean isCompleted() { return completed; }
        public boolean isCancelled() { return cancelled; }
        public String getCreatedOrderId() { return createdOrderId; }

        public void complete(String createdOrderId) {
            this.completed = true;
            this.createdOrderId = createdOrderId;
        }

        public void cancel() {
            this.cancelled = true;
        }
    }
}
