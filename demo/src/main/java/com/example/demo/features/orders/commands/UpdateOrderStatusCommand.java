package com.example.demo.features.orders.commands;

import com.example.demo.core.enums.OrderStatus;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateOrderStatusCommand implements Command<OrderResponse> {
    private final String orderId;
    private final OrderStatus status;
}
