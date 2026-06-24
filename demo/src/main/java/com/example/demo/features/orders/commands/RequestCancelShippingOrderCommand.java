package com.example.demo.features.orders.commands;

import com.example.demo.features.orders.dtos.request.CancelOrderRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestCancelShippingOrderCommand implements Command<OrderResponse> {
    private final String orderId;
    private final CancelOrderRequest request;
}
