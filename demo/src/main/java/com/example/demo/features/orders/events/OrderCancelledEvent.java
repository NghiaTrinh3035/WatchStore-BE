package com.example.demo.features.orders.events;

import com.example.demo.features.orders.entities.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelledEvent extends ApplicationEvent {
    private final Order order;
    private final String reason;
    private final String note;
    private final boolean paidOrder;
    private final boolean restockIssue;
    private final String changedBy;
    private final String historyNote;

    public OrderCancelledEvent(Object source, Order order, String reason, String note, boolean paidOrder, boolean restockIssue, String changedBy, String historyNote) {
        super(source);
        this.order = order;
        this.reason = reason;
        this.note = note;
        this.paidOrder = paidOrder;
        this.restockIssue = restockIssue;
        this.changedBy = changedBy;
        this.historyNote = historyNote;
    }
}
