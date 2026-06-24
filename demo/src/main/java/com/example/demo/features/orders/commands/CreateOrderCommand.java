package com.example.demo.features.orders.commands;

import com.example.demo.features.orders.dtos.request.OrderRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderCommand implements Command<OrderResponse> {
    private final OrderRequest request;
}
