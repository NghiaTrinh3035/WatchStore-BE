package com.example.demo.features.orders.services.payment;


import com.example.demo.core.enums.OrderStatus;
import com.example.demo.core.enums.PaymentMethod;
import com.example.demo.core.enums.PaymentStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.features.orders.dtos.request.OrderRequest;
import com.example.demo.features.orders.dtos.request.PaymentPrepareRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.dtos.response.PaymentResponse;
import com.example.demo.features.orders.dtos.response.PaymentStatusResponse;
import com.example.demo.features.orders.services.OrderService;
import com.example.demo.features.orders.services.OrderCommandInvoker;
import com.example.demo.features.orders.commands.CreateOrderCommand;
import com.example.demo.features.users.repositories.CustomerRepository;
import com.example.demo.core.services.AccessControlService;
import com.example.demo.features.orders.services.payment.config.VnPayConfig;
import com.example.demo.features.orders.services.CartService;
import com.example.demo.features.users.entities.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayPaymentStrategyImpl implements PaymentStrategy {

    private final OrderService orderService;
    private final OrderCommandInvoker orderCommandInvoker;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;
    private final CartService cartService;
    private final VnPayConfig vnPayConfig;

    private final ConcurrentMap<String, PendingVnPaySession> pendingSessions = new ConcurrentHashMap<>();

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentResponse preparePayment(PaymentPrepareRequest request) {
        accessControlService.requireCustomerAccess(request.getCustomerId());

        customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        OrderRequest orderRequest = request.toOrderRequest();
        long totalAmount = orderService.calculateTotalAmount(orderRequest);

        String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        pendingSessions.put(orderId, new PendingVnPaySession(orderId, request.getCustomerId(), orderRequest, totalAmount));

        String paymentUrl = createPaymentUrl(orderId, totalAmount);

        return PaymentResponse.builder()
                .orderId(orderId)
                .amount(totalAmount)
                .description("Thanh toan don hang VNPAY: " + orderId)
                .qrUrl(paymentUrl) // Reuse qrUrl field for redirect URL
                .build();
    }

    private String createPaymentUrl(String orderId, long amount) {
        long vnp_Amount = amount * 100;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getPayUrl() + "?" + queryUrl;
    }

    @Override
    public PaymentStatusResponse verifyPayment(String orderId) {
        PendingVnPaySession session = pendingSessions.get(orderId);
        if (session == null) {
            return PaymentStatusResponse.builder()
                    .orderId(orderId)
                    .status("CANCELLED")
                    .message("Phiên thanh toán VNPAY không tồn tại hoặc đã hết hạn.")
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
                    .message("Đang chờ thanh toán VNPAY.")
                    .build();
        }
    }

    @Override
    public void cancelPayment(String orderId) {
        PendingVnPaySession session = pendingSessions.get(orderId);
        if (session != null) {
            synchronized (session) {
                if (!session.isCompleted()) {
                    session.cancel();
                }
            }
        }
    }

    public boolean processIpn(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        if (!calculatedHash.equals(secureHash)) {
            log.error("VNPAY IPN signature mismatch");
            return false;
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        PendingVnPaySession session = pendingSessions.get(txnRef);
        if (session == null) {
            return false;
        }

        synchronized (session) {
            if (session.isCompleted()) return true; // Already processed
            if ("00".equals(responseCode)) {
                // Success
                OrderRequest orderRequest = session.getOrderRequest();
                OrderRequest.PaymentRequest paymentRequest = new OrderRequest.PaymentRequest();
                paymentRequest.setMethod(PaymentMethod.VNPAY);
                paymentRequest.setStatus(PaymentStatus.COMPLETED);
                paymentRequest.setIsPaid(true);
                paymentRequest.setPaymentDate(new Date());
                orderRequest.setPayment(paymentRequest);

                try {
                    OrderResponse createdOrder = orderCommandInvoker.execute(new CreateOrderCommand(orderRequest));
                    cartService.clearCart(session.getCustomerId(), true);
                    session.complete(createdOrder.getId());
                } catch (Exception e) {
                    log.error("Error creating order from VNPAY IPN", e);
                    return false;
                }
            } else {
                session.cancel();
            }
        }

        return true;
    }

    private static class PendingVnPaySession {
        private final String txnRef;
        private final String customerId;
        private final OrderRequest orderRequest;
        private final long amount;
        private boolean completed = false;
        private boolean cancelled = false;
        private String createdOrderId;

        public PendingVnPaySession(String txnRef, String customerId, OrderRequest orderRequest, long amount) {
            this.txnRef = txnRef;
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
