package com.example.demo.features.orders.dtos.request;


import com.example.demo.features.communications.controllers.*;
import com.example.demo.features.warranty.dtos.request.*;
import com.example.demo.features.communications.entities.*;
import com.example.demo.features.auth.dtos.response.*;
import com.example.demo.features.inventory.services.*;
import com.example.demo.features.orders.dtos.request.*;
import com.example.demo.features.auth.dtos.request.*;
import com.example.demo.features.reports.dtos.response.*;
import com.example.demo.features.reports.services.*;
import com.example.demo.features.orders.entities.*;
import com.example.demo.features.inventory.controllers.*;
import com.example.demo.features.vouchers.dtos.response.*;
import com.example.demo.features.communications.repositories.*;
import com.example.demo.features.inventory.dtos.request.*;
import com.example.demo.core.enums.*;
import com.example.demo.core.dtos.response.*;
import com.example.demo.features.auth.controllers.*;
import com.example.demo.features.reports.repositories.*;
import com.example.demo.core.services.*;
import com.example.demo.features.vouchers.controllers.*;
import com.example.demo.features.warranty.services.*;
import com.example.demo.features.communications.dtos.response.*;
import com.example.demo.features.orders.dtos.response.*;
import com.example.demo.core.exceptions.*;
import com.example.demo.features.reports.dtos.request.*;
import com.example.demo.features.auth.services.*;
import com.example.demo.features.users.dtos.response.*;
import com.example.demo.features.users.services.*;
import com.example.demo.features.users.controllers.*;
import com.example.demo.features.products.dtos.response.*;
import com.example.demo.core.config.*;
import com.example.demo.features.orders.services.payment.*;
import com.example.demo.features.vouchers.dtos.request.*;
import com.example.demo.features.products.services.*;
import com.example.demo.features.vouchers.services.*;
import com.example.demo.core.entities.*;
import com.example.demo.features.warranty.entities.*;
import com.example.demo.features.inventory.dtos.response.*;
import com.example.demo.features.warranty.controllers.*;
import com.example.demo.features.users.entities.*;
import com.example.demo.features.products.dtos.request.*;
import com.example.demo.features.warranty.repositories.*;
import com.example.demo.features.inventory.repositories.*;
import com.example.demo.features.communications.dtos.request.*;
import com.example.demo.features.warranty.dtos.response.*;
import com.example.demo.features.orders.controllers.*;
import com.example.demo.features.products.entities.*;
import com.example.demo.features.vouchers.entities.*;
import com.example.demo.features.products.controllers.*;
import com.example.demo.features.reports.controllers.*;
import com.example.demo.features.inventory.entities.*;
import com.example.demo.features.communications.services.*;
import com.example.demo.features.orders.services.*;
import com.example.demo.features.users.dtos.request.*;
import com.example.demo.features.reports.entities.*;
import com.example.demo.core.common.*;
import com.example.demo.features.products.repositories.*;
import com.example.demo.features.orders.repositories.*;
import com.example.demo.features.users.repositories.*;
import com.example.demo.features.vouchers.repositories.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PaymentPrepareRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 255, message = "Shipping address must not exceed 255 characters")
    private String shippingAddress;

    private String voucherCode;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderRequest.OrderItemRequest> items;

    @Valid
    private OrderRequest.ShippingRequest shipping;

    public OrderRequest toOrderRequest() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(this.customerId);
        orderRequest.setNote(this.note);
        orderRequest.setShippingAddress(this.shippingAddress);
        orderRequest.setVoucherCode(this.voucherCode);
        
        if (this.items != null) {
            orderRequest.setItems(this.items.stream().map(item -> {
                OrderRequest.OrderItemRequest copiedItem = new OrderRequest.OrderItemRequest();
                copiedItem.setProductId(item.getProductId());
                copiedItem.setQuantity(item.getQuantity());
                return copiedItem;
            }).toList());
        }

        if (this.shipping != null) {
            OrderRequest.ShippingRequest copied = new OrderRequest.ShippingRequest();
            copied.setTrackingNumber(shipping.getTrackingNumber());
            copied.setCarrierName(shipping.getCarrierName());
            copied.setCarrierPhone(shipping.getCarrierPhone());
            copied.setEstimatedDelivery(shipping.getEstimatedDelivery());
            copied.setFullName(shipping.getFullName());
            copied.setPhone(shipping.getPhone());
            copied.setProvince(shipping.getProvince());
            copied.setDistrict(shipping.getDistrict());
            copied.setWard(shipping.getWard());
            copied.setDetailAddress(shipping.getDetailAddress());
            orderRequest.setShipping(copied);
        }
        
        return orderRequest;
    }
}
