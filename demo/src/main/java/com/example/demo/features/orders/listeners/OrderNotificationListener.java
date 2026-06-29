package com.example.demo.features.orders.listeners;

import com.example.demo.features.communications.services.NotificationService;
import com.example.demo.features.orders.events.OrderCancelRequestedEvent;
import com.example.demo.features.orders.events.OrderCancelledEvent;
import com.example.demo.features.orders.events.OrderCreatedEvent;
import com.example.demo.features.orders.events.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            notificationService.sendOrderSuccessNotification(event.getOrder().getCustomer(), event.getOrder().getId());
        } catch (Exception ex) {
            log.warn("Order success notification failed for order {}", event.getOrder().getId(), ex);
        }

        try {
            notificationService.notifyStoreAboutNewOrder(event.getOrder().getCustomer(), event.getOrder().getId());
        } catch (Exception ex) {
            log.warn("Store notification for new order {} failed", event.getOrder().getId(), ex);
        }
    }

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        try {
            String statusText = switch (event.getNewStatus()) {
                case CONFIRMED -> "Đã được xác nhận";
                case SHIPPING -> "Đang giao hàng";
                case DELIVERED -> "Đã được giao";
                case COMPLETED -> "Đã hoàn thành";
                case RETURNED -> "Đã trả hàng";
                case CANCELLED -> "Đã hủy";
                default -> event.getNewStatus().name();
            };
            switch (event.getNewStatus()) {
                case CONFIRMED -> notificationService.sendOrderConfirmedNotification(event.getChangedBy(), event.getOrder().getCustomer(), event.getOrder().getId());
                case DELIVERED -> notificationService.sendOrderDeliveredNotification(event.getChangedBy(), event.getOrder().getCustomer(), event.getOrder().getId());
                default -> notificationService.sendOrderStatusUpdateNotification(
                        event.getChangedBy(),
                        event.getOrder().getCustomer(),
                        event.getOrder().getId(),
                        null,
                        statusText
                );
            }
        } catch (Exception ex) {
            log.warn("Order status notification failed for order {}", event.getOrder().getId(), ex);
        }
    }

    @Async
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        try {
            notificationService.notifyCustomerAboutOrderCancellation(event.getOrder().getCustomer(), event.getOrder().getId(), event.getReason(), event.isPaidOrder());
        } catch (Exception ex) {
            log.warn("Customer cancel notification failed for order {}", event.getOrder().getId(), ex);
        }

        try {
            notificationService.notifyStoreAboutCustomerCancellation(
                    event.getOrder().getCustomer(),
                    event.getOrder().getId(),
                    event.getReason(),
                    event.isPaidOrder(),
                    event.isRestockIssue()
            );
        } catch (Exception ex) {
            log.warn("Store cancel notification failed for order {}", event.getOrder().getId(), ex);
        }
    }

    @Async
    @EventListener
    public void onOrderCancelRequested(OrderCancelRequestedEvent event) {
        try {
            notificationService.notifyStoreAboutCancellationRequest(event.getOrder().getCustomer(), event.getOrder().getId(), event.getReason(), event.getNote());
        } catch (Exception ex) {
            log.warn("Store cancellation request notification failed for order {}", event.getOrder().getId(), ex);
        }
    }
}
