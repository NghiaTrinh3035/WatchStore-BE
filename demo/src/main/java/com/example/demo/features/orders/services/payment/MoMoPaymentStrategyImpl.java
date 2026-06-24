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
import com.example.demo.features.orders.services.payment.config.MoMoConfig;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoMoPaymentStrategyImpl implements PaymentStrategy {

    private final OrderService orderService;
    private final OrderCommandInvoker orderCommandInvoker;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;
    private final CartService cartService;
    private final MoMoConfig moMoConfig;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, PendingMoMoSession> pendingSessions = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.MOMO;
    }

    @Override
    public PaymentResponse preparePayment(PaymentPrepareRequest request) {
        accessControlService.requireCustomerAccess(request.getCustomerId());

        customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        OrderRequest orderRequest = request.toOrderRequest();
        long totalAmount = orderService.calculateTotalAmount(orderRequest);

        String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String requestId = UUID.randomUUID().toString();

        pendingSessions.put(orderId, new PendingMoMoSession(orderId, request.getCustomerId(), orderRequest, totalAmount));

        try {
            String requestType = "captureWallet";
            String extraData = "";
            String orderInfo = "Thanh toan don hang: " + orderId;
            
            String rawHash = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + totalAmount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + moMoConfig.getIpnUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + moMoConfig.getPartnerCode() +
                    "&redirectUrl=" + moMoConfig.getRedirectUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = moMoConfig.hmacSHA256(rawHash, moMoConfig.getSecretKey());

            Map<String, Object> bodyParam = new java.util.HashMap<>();
            bodyParam.put("partnerCode", moMoConfig.getPartnerCode());
            bodyParam.put("requestId", requestId);
            bodyParam.put("amount", totalAmount);
            bodyParam.put("orderId", orderId);
            bodyParam.put("orderInfo", orderInfo);
            bodyParam.put("redirectUrl", moMoConfig.getRedirectUrl());
            bodyParam.put("ipnUrl", moMoConfig.getIpnUrl());
            bodyParam.put("requestType", requestType);
            bodyParam.put("extraData", extraData);
            bodyParam.put("lang", "vi");
            bodyParam.put("signature", signature);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(moMoConfig.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(bodyParam)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode responseBody = objectMapper.readTree(response.body());

            String payUrl = responseBody.path("payUrl").asText();

            return PaymentResponse.builder()
                    .orderId(orderId)
                    .amount(totalAmount)
                    .description(orderInfo)
                    .qrUrl(payUrl) 
                    .build();

        } catch (Exception e) {
            log.error("Error preparing MoMo payment", e);
            throw new RuntimeException("Could not generate MoMo payment URL");
        }
    }

    @Override
    public PaymentStatusResponse verifyPayment(String orderId) {
        PendingMoMoSession session = pendingSessions.get(orderId);
        if (session == null) {
            return PaymentStatusResponse.builder()
                    .orderId(orderId)
                    .status("CANCELLED")
                    .message("Phiên thanh toán MoMo không tồn tại.")
                    .build();
        }

        synchronized (session) {
            if (session.isCompleted()) {
                OrderResponse order = orderService.findById(session.getCreatedOrderId()).orElse(null);
                return PaymentStatusResponse.builder()
                        .orderId(orderId)
                        .status("SUCCESS")
                        .message("Thanh toán thành công.")
                        .order(order)
                        .build();
            }
            if (session.isCancelled()) {
                return PaymentStatusResponse.builder()
                        .orderId(orderId)
                        .status("CANCELLED")
                        .message("Đã hủy thanh toán.")
                        .build();
            }
            return PaymentStatusResponse.builder()
                    .orderId(orderId)
                    .status("PENDING")
                    .message("Đang chờ thanh toán MoMo.")
                    .build();
        }
    }

    @Override
    public void cancelPayment(String orderId) {
        PendingMoMoSession session = pendingSessions.get(orderId);
        if (session != null) {
            synchronized (session) {
                if (!session.isCompleted()) {
                    session.cancel();
                }
            }
        }
    }

    public boolean processIpn(Map<String, Object> payload) {
        try {
            String partnerCode = (String) payload.get("partnerCode");
            String orderId = (String) payload.get("orderId");
            String requestId = (String) payload.get("requestId");
            Number amountNode = (Number) payload.get("amount");
            long amount = amountNode != null ? amountNode.longValue() : 0;
            String orderInfo = (String) payload.get("orderInfo");
            String orderType = (String) payload.get("orderType");
            Number transIdNode = (Number) payload.get("transId");
            long transId = transIdNode != null ? transIdNode.longValue() : 0;
            Number resultCodeNode = (Number) payload.get("resultCode");
            int resultCode = resultCodeNode != null ? resultCodeNode.intValue() : -1;
            String message = (String) payload.get("message");
            String payType = (String) payload.get("payType");
            String responseTime = String.valueOf(payload.get("responseTime"));
            String extraData = (String) payload.get("extraData");
            String signature = (String) payload.get("signature");

            String rawHash = "accessKey=" + moMoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            String calculatedSignature = moMoConfig.hmacSHA256(rawHash, moMoConfig.getSecretKey());

            if (!calculatedSignature.equals(signature)) {
                log.error("MoMo IPN signature mismatch");
                return false;
            }

            PendingMoMoSession session = pendingSessions.get(orderId);
            if (session == null) {
                return false;
            }

            synchronized (session) {
                if (session.isCompleted()) return true;

                if (resultCode == 0) { // Success
                    OrderRequest orderRequest = session.getOrderRequest();
                    OrderRequest.PaymentRequest paymentRequest = new OrderRequest.PaymentRequest();
                    paymentRequest.setMethod(PaymentMethod.MOMO);
                    paymentRequest.setStatus(PaymentStatus.COMPLETED);
                    paymentRequest.setIsPaid(true);
                    paymentRequest.setPaymentDate(new Date());
                    orderRequest.setPayment(paymentRequest);

                    OrderResponse createdOrder = orderCommandInvoker.execute(new CreateOrderCommand(orderRequest));
                    cartService.clearCart(session.getCustomerId(), true);
                    session.complete(createdOrder.getId());
                } else {
                    session.cancel();
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error processing MoMo IPN", e);
            return false;
        }
    }

    public boolean processReturn(Map<String, String> params) {
        try {
            Map<String, Object> payload = new java.util.HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Convert known numeric fields
                if ("amount".equals(key) || "transId".equals(key) || "resultCode".equals(key)) {
                    try {
                        payload.put(key, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        payload.put(key, 0L);
                    }
                } else {
                    payload.put(key, value);
                }
            }
            return processIpn(payload);
        } catch (Exception e) {
            log.error("Error processing MoMo Return", e);
            return false;
        }
    }

    private static class PendingMoMoSession {
        private final String orderId;
        private final String customerId;
        private final OrderRequest orderRequest;
        private final long amount;
        private boolean completed = false;
        private boolean cancelled = false;
        private String createdOrderId;

        public PendingMoMoSession(String orderId, String customerId, OrderRequest orderRequest, long amount) {
            this.orderId = orderId;
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
