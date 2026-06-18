package com.example.demo.features.orders.events;

import com.example.demo.features.orders.entities.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelRequestedEvent extends ApplicationEvent {
    private final Order order;
    private final String reason;
    private final String note;
    private final String requestedBy;
    private final String historyNote;

    public OrderCancelRequestedEvent(Object source, Order order, String reason, String note, String requestedBy, String historyNote) {
        super(source);
        this.order = order;
        this.reason = reason;
        this.note = note;
        this.requestedBy = requestedBy;
        this.historyNote = historyNote;
    }
}
