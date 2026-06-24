package com.example.demo.features.orders.handlers;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.core.enums.PaymentStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.core.services.AccessControlService;
import com.example.demo.features.orders.commands.CancelOrderCommand;
import com.example.demo.features.orders.dtos.request.CancelOrderRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.entities.Order;
import com.example.demo.features.orders.entities.OrderItem;
import com.example.demo.features.orders.events.OrderCancelledEvent;
import com.example.demo.features.orders.repositories.OrderRepository;
import com.example.demo.features.orders.services.OrderService;
import com.example.demo.features.products.entities.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelOrderCommandHandler implements CommandHandler<CancelOrderCommand, OrderResponse> {

    private final OrderRepository orderRepository;
    private final AccessControlService accessControlService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService;

    @Override
    @Transactional
    public OrderResponse handle(CancelOrderCommand command) {
        String id = command.getOrderId();
        CancelOrderRequest request = command.getRequest();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        accessControlService.requireCustomerAccess(order.getCustomer().getId());
        
        orderService.validateCustomerCancellation(order);

        String reason = normalizeCancelReason(request != null ? request.getReason() : null);
        String note = normalizeText(request != null ? request.getNote() : null);
        boolean paidOrder = orderService.isPaidOrder(order);

        boolean restockIssue = restockOrderItems(order);

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPayment() != null && paidOrder) {
            order.getPayment().setStatus(PaymentStatus.PROCESSING);
        }
        order = orderRepository.save(order);

        String historyNote = buildCancellationHistoryNote(reason, note, restockIssue);

        eventPublisher.publishEvent(new OrderCancelledEvent(this, order, reason, note, paidOrder, restockIssue, getCurrentUsername(), historyNote));

        return orderService.toOrderResponse(order);
    }

    private boolean restockOrderItems(Order order) {
        boolean hasIssue = false;
        for (OrderItem item : order.getOrderItems()) {
            try {
                Product product = item.getProduct();
                if (product == null) {
                    throw new IllegalStateException("Missing product on order item " + item.getId());
                }
                int currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
                int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                product.setStockQuantity(currentStock + Math.max(quantity, 0));
            } catch (Exception ex) {
                hasIssue = true;
                log.error("Failed to restock product for order item {}", item.getId(), ex);
            }
        }
        return hasIssue;
    }

    private String buildCancellationHistoryNote(String reason, String note, boolean restockIssue) {
        StringBuilder builder = new StringBuilder("Lý do: ").append(reason);
        if (StringUtils.hasText(note)) {
            builder.append("; Ghi chú: ").append(note);
        }
        if (restockIssue) {
            builder.append("; Cảnh báo: lỗi hoàn kho, cần xử lý thủ công.");
        }
        return builder.toString();
    }

    private String normalizeCancelReason(String rawReason) {
        if (!StringUtils.hasText(rawReason)) {
            return "Khác";
        }
        String normalized = rawReason.trim().toUpperCase();
        return switch (normalized) {
            case "WRONG_PRODUCT" -> "Đặt nhầm sản phẩm";
            case "BETTER_PRICE" -> "Tìm thấy giá tốt hơn";
            case "DONT_NEED_ANYMORE" -> "Không cần nữa";
            case "CHANGED_MIND" -> "Thay đổi ý định";
            case "DELIVERY_TOO_LONG" -> "Thời gian giao hàng quá lâu";
            case "OTHER" -> "Khác";
            default -> rawReason.trim();
        };
    }

    private String normalizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return input.trim();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
