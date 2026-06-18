package com.example.demo.features.orders.events;

import com.example.demo.features.orders.entities.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    private final Order order;
    private final String changedBy;

    public OrderCreatedEvent(Object source, Order order, String changedBy) {
        super(source);
        this.order = order;
        this.changedBy = changedBy;
    }
}
