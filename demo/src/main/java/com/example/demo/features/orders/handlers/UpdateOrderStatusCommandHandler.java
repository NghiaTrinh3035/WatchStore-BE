package com.example.demo.features.orders.handlers;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.core.services.AccessControlService;
import com.example.demo.features.orders.commands.UpdateOrderStatusCommand;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.entities.Order;
import com.example.demo.features.orders.events.OrderStatusChangedEvent;
import com.example.demo.features.orders.repositories.OrderRepository;
import com.example.demo.features.orders.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderStatusCommandHandler implements CommandHandler<UpdateOrderStatusCommand, OrderResponse> {

    private final OrderRepository orderRepository;
    private final AccessControlService accessControlService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService;

    @Override
    @Transactional
    public OrderResponse handle(UpdateOrderStatusCommand command) {
        String id = command.getOrderId();
        OrderStatus status = command.getStatus();

        accessControlService.requirePrivilegedRole();
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Use /api/orders/{id}/cancel to cancel an order");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        order = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, status, "Cập nhật bởi nhân viên", getCurrentUsername()));

        return orderService.toOrderResponse(order);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        boolean isValid = switch (current) {
            case PENDING -> target == OrderStatus.CONFIRMED || target == OrderStatus.CANCELLED;
            case CONFIRMED -> target == OrderStatus.SHIPPING || target == OrderStatus.CANCELLED;
            case SHIPPING -> target == OrderStatus.DELIVERED;
            case DELIVERED -> target == OrderStatus.COMPLETED || target == OrderStatus.RETURNED;
            default -> false;
        };

        if (!isValid) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", current, target)
            );
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
