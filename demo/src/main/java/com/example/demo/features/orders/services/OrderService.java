package com.example.demo.features.orders.services;


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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.EnumSet;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final String REFUND_PROCESSING_MESSAGE = "Hoàn tiền sẽ được xử lý trong 3-7 ngày làm việc.";
    private static final long DEFAULT_CANCEL_WINDOW_HOURS = 24L;
    private static final Set<OrderStatus> CUSTOMER_CAN_CANCEL_STATUSES = EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherService voucherService;
    private final AccessControlService accessControlService;
    private final OrderStatusHistoryRepository historyRepository;

    @Value("${order.cancel.self-window-hours:24}")
    private long selfCancelWindowHours;

    @Transactional(readOnly = true)
    public Optional<OrderResponse> findById(String id) {
        return orderRepository.findById(id)
                .map(order -> {
                    accessControlService.requireCustomerAccess(order.getCustomer().getId());
                    return toOrderResponse(order);
                });
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(String customerId) {
        accessControlService.requireCustomerAccess(customerId);
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findByCustomerId(String customerId, Pageable pageable) {
        accessControlService.requireCustomerAccess(customerId);
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .toList();
        return new PageImpl<>(responses, pageable, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        accessControlService.requirePrivilegedRole();
        return orderRepository.findByStatus(status);
    }

    public OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build())
                .toList();

        List<OrderStatusHistory> historyEntries = historyRepository.findByOrderIdOrderByChangedAtAsc(order.getId());

        List<OrderStatusHistoryResponse> timeline = historyEntries.stream()
                .map(h -> OrderStatusHistoryResponse.builder()
                        .status(h.getStatus().name())
                        .note(h.getNote())
                        .changedAt(h.getChangedAt())
                        .changedBy(h.getChangedBy())
                        .build())
                .toList();

        if (timeline.isEmpty()) {
            timeline = List.of(
                    OrderStatusHistoryResponse.builder()
                            .status(order.getStatus().name())
                            .note("Đơn hàng hiện tại")
                            .changedAt(order.getOrderDate())
                            .changedBy(order.getCustomer() != null ? order.getCustomer().getUsername() : "system")
                            .build()
            );
        }

        OrderResponse.PaymentResponse paymentResponse = null;
        if (order.getPayment() != null) {
            paymentResponse = OrderResponse.PaymentResponse.builder()
                    .method(order.getPayment().getMethod())
                    .status(order.getPayment().getStatus())
                    .isPaid(order.getPayment().getIsPaid())
                    .paymentDate(order.getPayment().getPaymentDate())
                    .amount(order.getPayment().getAmount())
                    .build();
        }

        String latestCancellationNote = historyEntries.stream()
                .filter(item -> item.getStatus() == OrderStatus.CANCELLED)
                .reduce((first, second) -> second)
                .map(OrderStatusHistory::getNote)
                .orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .customerId(order.getCustomer().getId())
                .customerUsername(order.getCustomer().getUsername())
                .customerFullName(order.getCustomer().getFullName())
                .customerPhone(order.getCustomer().getPhone())
                .customerAddress(order.getCustomer().getAddress())
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .orderItems(itemResponses)
                .timeline(timeline)
                .payment(paymentResponse)
                .canCancel(isSelfCancelable(order))
                .canRequestCancel(order.getStatus() == OrderStatus.SHIPPING)
                .refundRequired(isRefundRequired(order))
                .refundMessage(isRefundRequired(order) ? REFUND_PROCESSING_MESSAGE : null)
                .cancellationReason(extractCancellationReason(latestCancellationNote))
                .cancellationNote(latestCancellationNote)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAllOrders(Pageable pageable) {
        accessControlService.requirePrivilegedRole();
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(this::toOrderResponse)
                .toList();
        return new PageImpl<>(responses, pageable, orderPage.getTotalElements());
    }


    public void validateCustomerCancellation(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng này đã được hủy.");
        }
        if (order.getStatus() == OrderStatus.SHIPPING) {
            throw new IllegalStateException("Đơn hàng đang được giao. Bạn không thể tự hủy, vui lòng gửi yêu cầu hủy đến nhân viên.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.RETURNED) {
            throw new IllegalStateException("Đơn hàng đã giao/hoàn tất không thể hủy. Vui lòng liên hệ hotline để được hỗ trợ.");
        }
        if (!CUSTOMER_CAN_CANCEL_STATUSES.contains(order.getStatus())) {
            throw new IllegalStateException("Không thể hủy đơn ở trạng thái: " + order.getStatus());
        }
        if (!isWithinSelfCancelWindow(order)) {
            throw new IllegalStateException("Đã quá thời gian cho phép hủy đơn tự động. Vui lòng liên hệ hotline để được hỗ trợ.");
        }
    }


    private boolean isSelfCancelable(Order order) {
        return CUSTOMER_CAN_CANCEL_STATUSES.contains(order.getStatus()) && isWithinSelfCancelWindow(order);
    }

    private boolean isWithinSelfCancelWindow(Order order) {
        if (order.getOrderDate() == null) {
            return true;
        }
        long configuredWindowHours = selfCancelWindowHours > 0 ? selfCancelWindowHours : DEFAULT_CANCEL_WINDOW_HOURS;
        Instant deadline = order.getOrderDate().toInstant().plus(configuredWindowHours, ChronoUnit.HOURS);
        return !Instant.now().isAfter(deadline);
    }

    private boolean isRefundRequired(Order order) {
        return order.getStatus() == OrderStatus.CANCELLED && isPaidOrder(order);
    }

    public boolean isPaidOrder(Order order) {
        return order.getPayment() != null && Boolean.TRUE.equals(order.getPayment().getIsPaid());
    }



    public long calculateTotalAmount(OrderRequest request) {
        long totalAmount = 0L;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            int quantity = itemReq.getQuantity() == null ? 0 : itemReq.getQuantity();
            int availableStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than 0.");
            }
            if (quantity > availableStock) {
                throw new IllegalArgumentException("Product " + product.getName() + " only has " + availableStock + " in stock.");
            }

            long unitPrice = product.getPrice() == null ? 0L : product.getPrice();
            totalAmount += unitPrice * quantity;
        }

        if (StringUtils.hasText(request.getVoucherCode())) {
            Voucher voucher = voucherService.applyVoucher(request.getVoucherCode());
            long discount = totalAmount * voucher.getDiscountPercent() / 100;
            totalAmount -= discount;
        }

        return Math.max(totalAmount, 0L);
    }



    private String extractCancellationReason(String note) {
        if (!StringUtils.hasText(note)) {
            return null;
        }
        String prefix = "Lý do:";
        if (!note.startsWith(prefix)) {
            return null;
        }
        String body = note.substring(prefix.length()).trim();
        int noteIndex = body.indexOf(";");
        return noteIndex >= 0 ? body.substring(0, noteIndex).trim() : body;
    }

}

