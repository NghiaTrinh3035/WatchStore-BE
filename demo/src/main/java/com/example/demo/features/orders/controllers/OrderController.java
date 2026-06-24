package com.example.demo.features.orders.controllers;


import com.example.demo.core.enums.OrderStatus;
import com.example.demo.core.enums.PaymentMethod;
import com.example.demo.features.orders.commands.CancelOrderCommand;
import com.example.demo.features.orders.commands.CreateOrderCommand;
import com.example.demo.features.orders.commands.RequestCancelShippingOrderCommand;
import com.example.demo.features.orders.commands.UpdateOrderStatusCommand;
import com.example.demo.features.orders.dtos.request.CancelOrderRequest;
import com.example.demo.features.orders.dtos.request.OrderRequest;
import com.example.demo.features.orders.dtos.request.PaymentPrepareRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.dtos.response.PaymentResponse;
import com.example.demo.features.orders.dtos.response.PaymentStatusResponse;
import com.example.demo.features.orders.services.OrderCommandInvoker;
import com.example.demo.features.orders.services.OrderService;
import com.example.demo.features.orders.services.payment.PaymentStrategyFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderCommandInvoker orderCommandInvoker;
    private final PaymentStrategyFactory paymentStrategyFactory;


    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 100, sort = "orderDate,desc") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getByCustomer(
            @PathVariable String customerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.findByCustomerId(customerId, pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderCommandInvoker.execute(new CreateOrderCommand(request)));
    }

    @PostMapping("/payment/prepare")
    public ResponseEntity<PaymentResponse> preparePayment(@Valid @RequestBody PaymentPrepareRequest request) {
        return ResponseEntity.ok(paymentStrategyFactory.getStrategy(request.getMethod()).preparePayment(request));
    }

    @PostMapping("/payment/{orderId}/verify")
    public ResponseEntity<PaymentStatusResponse> verifyPayment(
            @PathVariable String orderId,
            @RequestParam PaymentMethod method) {
        return ResponseEntity.ok(paymentStrategyFactory.getStrategy(method).verifyPayment(orderId));
    }

    @DeleteMapping("/payment/{orderId}")
    public ResponseEntity<Void> cancelPayment(
            @PathVariable String orderId,
            @RequestParam PaymentMethod method) {
        paymentStrategyFactory.getStrategy(method).cancelPayment(orderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderCommandInvoker.execute(new UpdateOrderStatusCommand(id, status)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable String id,
            @RequestBody(required = false) CancelOrderRequest request) {
        return ResponseEntity.ok(orderCommandInvoker.execute(new CancelOrderCommand(id, request)));
    }

    @PatchMapping("/{id}/cancel-request")
    public ResponseEntity<OrderResponse> requestCancel(
            @PathVariable String id,
            @RequestBody(required = false) CancelOrderRequest request) {
        return ResponseEntity.ok(orderCommandInvoker.execute(new RequestCancelShippingOrderCommand(id, request)));
    }
}
