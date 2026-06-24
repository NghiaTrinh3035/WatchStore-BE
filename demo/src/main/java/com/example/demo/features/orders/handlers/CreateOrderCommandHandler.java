package com.example.demo.features.orders.handlers;

import com.example.demo.core.entities.*;
import com.example.demo.core.enums.PaymentMethod;
import com.example.demo.core.enums.PaymentStatus;
import com.example.demo.core.exceptions.ResourceNotFoundException;
import com.example.demo.core.services.AccessControlService;
import com.example.demo.features.orders.commands.CreateOrderCommand;
import com.example.demo.features.orders.dtos.request.OrderRequest;
import com.example.demo.features.orders.dtos.response.OrderResponse;
import com.example.demo.features.orders.entities.Order;
import com.example.demo.features.orders.entities.OrderItem;
import com.example.demo.features.orders.entities.Payment;
import com.example.demo.features.orders.events.OrderCreatedEvent;
import com.example.demo.features.orders.repositories.OrderRepository;
import com.example.demo.features.orders.services.OrderService;
import com.example.demo.features.products.entities.Product;
import com.example.demo.features.products.repositories.ProductRepository;
import com.example.demo.features.users.entities.Customer;
import com.example.demo.features.users.repositories.CustomerRepository;
import com.example.demo.features.vouchers.entities.Voucher;
import com.example.demo.features.vouchers.services.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, OrderResponse> {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final VoucherService voucherService;
    private final AccessControlService accessControlService;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService; // to reuse toOrderResponse

    @Override
    @Transactional
    public OrderResponse handle(CreateOrderCommand command) {
        OrderRequest request = command.getRequest();
        validatePaymentBeforeCreation(request);
        accessControlService.requireCustomerAccess(request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));

        Order order = Order.builder()
                .customer(customer)
                .note(normalizeText(request.getNote()))
                .shippingAddress(resolveShippingAddress(request, customer))
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        long totalAmount = 0L;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            int quantity = itemReq.getQuantity();
            int availableStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (quantity > availableStock) {
                throw new IllegalArgumentException(
                        "Sản phẩm " + product.getName() + " chỉ còn " + availableStock + " trong kho"
                );
            }

            product.setStockQuantity(availableStock - quantity);

            long unitPrice = product.getPrice() == null ? 0L : product.getPrice();
            long subTotal = unitPrice * quantity;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .subTotal(subTotal)
                    .build();
            orderItems.add(orderItem);
            totalAmount += subTotal;
        }

        order.setOrderItems(orderItems);

        if (StringUtils.hasText(request.getVoucherCode())) {
            Voucher voucher = voucherService.consumeVoucher(request.getVoucherCode());
            order.setVoucher(voucher);
            long discount = totalAmount * voucher.getDiscountPercent() / 100;
            totalAmount -= discount;
        }

        totalAmount = Math.max(totalAmount, 0L);
        order.setTotalAmount(totalAmount);

        Payment payment = buildPayment(request, order, totalAmount);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder, customer.getUsername()));

        return orderService.toOrderResponse(savedOrder);
    }

    private void validatePaymentBeforeCreation(OrderRequest request) {
        OrderRequest.PaymentRequest paymentRequest = request.getPayment();
        if (paymentRequest == null) {
            throw new IllegalArgumentException("Payment information is required.");
        }
        if (!Boolean.TRUE.equals(paymentRequest.getIsPaid())) {
            throw new IllegalArgumentException("Order can only be created after successful payment.");
        }
        if (paymentRequest.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Payment status must be COMPLETED.");
        }
    }

    private Payment buildPayment(OrderRequest request, Order order, long amount) {
        OrderRequest.PaymentRequest paymentRequest = request.getPayment();
        PaymentMethod method = paymentRequest != null && paymentRequest.getMethod() != null
                ? paymentRequest.getMethod()
                : PaymentMethod.VNPAY;

        if (method != PaymentMethod.VNPAY &&
            method != PaymentMethod.MOMO &&
            method != PaymentMethod.PAYPAL) {
            throw new IllegalArgumentException("Chỉ hỗ trợ thanh toán trực tuyến (VNPAY, MOMO, PAYPAL).");
        }

        boolean isPaid = paymentRequest != null && Boolean.TRUE.equals(paymentRequest.getIsPaid());
        if (!isPaid) {
            throw new IllegalArgumentException("Đơn hàng chỉ được tạo sau khi thanh toán thành công.");
        }

        PaymentStatus status = paymentRequest != null && paymentRequest.getStatus() != null
                ? paymentRequest.getStatus()
                : PaymentStatus.COMPLETED;
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Trạng thái thanh toán phải là COMPLETED.");
        }

        Date paymentDate = paymentRequest != null && paymentRequest.getPaymentDate() != null
                ? paymentRequest.getPaymentDate()
                : (isPaid ? new Date() : null);

        return Payment.builder()
                .order(order)
                .amount(amount)
                .method(method)
                .status(status)
                .isPaid(isPaid)
                .paymentDate(paymentDate)
                .build();
    }

    private String resolveShippingAddress(OrderRequest request, Customer customer) {
        if (StringUtils.hasText(request.getShippingAddress())) {
            return request.getShippingAddress().trim();
        }

        OrderRequest.ShippingRequest shipping = request.getShipping();
        if (shipping != null) {
            List<String> addressParts = new ArrayList<>();
            if (StringUtils.hasText(shipping.getDetailAddress())) {
                addressParts.add(shipping.getDetailAddress().trim());
            }
            if (StringUtils.hasText(shipping.getWard())) {
                addressParts.add(shipping.getWard().trim());
            }
            if (StringUtils.hasText(shipping.getDistrict())) {
                addressParts.add(shipping.getDistrict().trim());
            }
            if (StringUtils.hasText(shipping.getProvince())) {
                addressParts.add(shipping.getProvince().trim());
            }
            if (!addressParts.isEmpty()) {
                return String.join(", ", addressParts);
            }
        }

        return normalizeText(customer.getAddress());
    }

    private String normalizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return input.trim();
    }
}
