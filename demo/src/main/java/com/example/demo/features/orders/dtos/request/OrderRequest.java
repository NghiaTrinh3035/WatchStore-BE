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
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 255, message = "Shipping address must not exceed 255 characters")
    private String shippingAddress;

    private String voucherCode;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Valid
    private PaymentRequest payment;

    @Valid
    private ShippingRequest shipping;

    @Data
    public static class OrderItemRequest {

        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Data
    public static class PaymentRequest {

        @NotNull(message = "Payment method is required")
        private PaymentMethod method;

        private PaymentStatus status;

        private Boolean isPaid;

        private Date paymentDate;
    }

    @Data
    public static class ShippingRequest {

        @Size(max = 100, message = "Tracking number must not exceed 100 characters")
        private String trackingNumber;

        @Size(max = 100, message = "Carrier name must not exceed 100 characters")
        private String carrierName;

        @Size(max = 15, message = "Carrier phone must not exceed 15 characters")
        private String carrierPhone;

        private Date estimatedDelivery;

        @Size(max = 120, message = "Recipient name must not exceed 120 characters")
        private String fullName;

        @Size(max = 20, message = "Recipient phone must not exceed 20 characters")
        private String phone;

        @Size(max = 120, message = "Province must not exceed 120 characters")
        private String province;

        @Size(max = 120, message = "District must not exceed 120 characters")
        private String district;

        @Size(max = 120, message = "Ward must not exceed 120 characters")
        private String ward;

        @Size(max = 255, message = "Detail address must not exceed 255 characters")
        private String detailAddress;
    }
}
