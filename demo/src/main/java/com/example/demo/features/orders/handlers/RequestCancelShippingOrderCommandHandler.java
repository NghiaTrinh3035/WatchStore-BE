package com.example.demo.features.orders.handlers;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.core.services.AccessControlService;
import com.example.demo.features.orders.commands.RequestCancelShippingOrderCommand;
import com.example.demo.features.orders.dtos.request.CancelOrderRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.entities.Order;
import com.example.demo.features.orders.events.OrderCancelRequestedEvent;
import com.example.demo.features.orders.repositories.OrderRepository;
import com.example.demo.features.orders.services.OrderService;
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
public class RequestCancelShippingOrderCommandHandler implements CommandHandler<RequestCancelShippingOrderCommand, OrderResponse> {

    private final OrderRepository orderRepository;
    private final AccessControlService accessControlService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService;

    @Override
    @Transactional
    public OrderResponse handle(RequestCancelShippingOrderCommand command) {
        String id = command.getOrderId();
        CancelOrderRequest request = command.getRequest();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        accessControlService.requireCustomerAccess(order.getCustomer().getId());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng này đã được hủy.");
        }
        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new IllegalStateException("Chỉ có thể gửi yêu cầu hủy khi đơn hàng đang giao.");
        }

        String reason = normalizeCancelReason(request != null ? request.getReason() : null);
        String note = normalizeText(request != null ? request.getNote() : null);
        String historyNote = "Yêu cầu hủy khi đang giao. Lý do: " + reason;
        if (StringUtils.hasText(note)) {
            historyNote += "; Ghi chú: " + note;
        }

        eventPublisher.publishEvent(new OrderCancelRequestedEvent(this, order, reason, note, getCurrentUsername(), historyNote));

        return orderService.toOrderResponse(order);
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
