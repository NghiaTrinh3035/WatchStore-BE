package com.example.demo.features.orders.listeners;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.features.orders.entities.OrderStatusHistory;
import com.example.demo.features.orders.events.OrderCancelRequestedEvent;
import com.example.demo.features.orders.events.OrderCancelledEvent;
import com.example.demo.features.orders.events.OrderCreatedEvent;
import com.example.demo.features.orders.events.OrderStatusChangedEvent;
import com.example.demo.features.orders.repositories.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryListener {

    private final OrderStatusHistoryRepository historyRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onOrderCreated(OrderCreatedEvent event) {
        saveHistory(event.getOrder(), OrderStatus.PENDING, "Đơn hàng được tạo", event.getChangedBy());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        saveHistory(event.getOrder(), event.getNewStatus(), event.getNote(), event.getChangedBy());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onOrderCancelled(OrderCancelledEvent event) {
        saveHistory(event.getOrder(), OrderStatus.CANCELLED, event.getHistoryNote(), event.getChangedBy());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRED)
    public void onOrderCancelRequested(OrderCancelRequestedEvent event) {
        saveHistory(event.getOrder(), event.getOrder().getStatus(), event.getHistoryNote(), event.getRequestedBy());
    }

    private void saveHistory(com.example.demo.features.orders.entities.Order order, OrderStatus status, String note, String changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .note(note)
                .changedBy(changedBy)
                .build();
        historyRepository.save(history);
        log.debug("Saved OrderStatusHistory for order {} - Status: {}", order.getId(), status);
    }
}
