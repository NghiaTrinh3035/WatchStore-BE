package com.example.demo.features.orders.events;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.features.orders.entities.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final OrderStatus newStatus;
    private final String note;
    private final String changedBy;

    public OrderStatusChangedEvent(Object source, Order order, OrderStatus newStatus, String note, String changedBy) {
        super(source);
        this.order = order;
        this.newStatus = newStatus;
        this.note = note;
        this.changedBy = changedBy;
    }
}
