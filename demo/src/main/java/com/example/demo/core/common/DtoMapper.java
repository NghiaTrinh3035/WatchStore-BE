package com.example.demo.core.common;



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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static StaffResponse toStaffResponse(Staff staff) {
        if (staff == null) {
            return null;
        }
        return StaffResponse.builder()
                .id(staff.getId())
                .username(staff.getUsername())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phone(staff.getPhone())
                .address(staff.getAddress())
                .gender(staff.getGender())
                .role(staff.getRole())
                .isActive(staff.getIsActive())
                .createdAt(staff.getCreatedAt())
                .build();
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .gender(customer.getGender())
                .role(customer.getRole())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    public static SupplierResponse toSupplierResponse(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contractInfo(supplier.getContractInfo())
                .address(supplier.getAddress())
                .build();
    }

    public static ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        List<Category> categories = getEffectiveCategories(product);
        String legacyCategoryId = categories.isEmpty() ? null : categories.get(0).getId();
        String legacyCategoryName = categories.isEmpty() ? null : categories.get(0).getName();

        return ProductResponse.builder()
                .id(product.getId())
                .brand(product.getBrand())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .movementType(product.getMovementType())
                .glassMaterial(product.getGlassMaterial())
                .waterResistance(product.getWaterResistance())
                .faceSize(product.getFaceSize())
                .wireMaterial(product.getWireMaterial())
                .wireColor(product.getWireColor())
                .caseColor(product.getCaseColor())
                .faceColor(product.getFaceColor())
                .color(firstNonBlank(product.getWireColor(), product.getCaseColor(), product.getFaceColor()))
                .size(product.getFaceSize())
                .specs(buildSpecs(product))
                .status(product.getStatus())
                .categoryId(legacyCategoryId)
                .categoryName(legacyCategoryName)
                .categoryIds(categories.stream().map(Category::getId).toList())
                .categoryNames(categories.stream().map(Category::getName).toList())
                .categories(categories.stream()
                        .map(category -> ProductCategoryResponse.builder().id(category.getId()).name(category.getName()).build())
                        .toList())
                .imageUrls(mapImageUrls(product.getImages()))
                .averageRating(calculateAverageRating(product))
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static WarrantyResponse toWarrantyResponse(Warranty warranty) {
        if (warranty == null) {
            return null;
        }
        return WarrantyResponse.builder()
                .id(warranty.getId())
            .userId(warranty.getUserId())
            .customerId(warranty.getCustomerId())
            .orderId(warranty.getOrderId())
            .orderItemId(warranty.getOrderItemId())
                .customerPhone(warranty.getCustomerPhone())
                .customerName(warranty.getCustomerName())
                .issueDescription(warranty.getIssueDescription())
                .receivedDate(warranty.getReceivedDate())
                .expectedReturnDate(warranty.getExpectedReturnDate())
            .createdAt(warranty.getCreatedAt())
            .updatedAt(warranty.getUpdatedAt())
                .status(warranty.getStatus())
                .technicianNote(warranty.getTechnicianNote())
                .rejectReason(warranty.getRejectReason())
                .quantity(warranty.getQuantity())
                .productId(warranty.getProductId())
                .productName(warranty.getProduct() != null ? warranty.getProduct().getName() : null)
                .build();
    }

    public static VoucherResponse toVoucherResponse(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountPercent(voucher.getDiscountPercent())
                .usageCount(voucher.getUsageCount())
                .validFrom(voucher.getValidFrom())
                .validTo(voucher.getValidTo())
                .createdAt(voucher.getCreatedAt())
                .quantity(voucher.getQuantity())
                .status(voucher.getStatus())
                .active(voucher.getStatus() != null && voucher.getStatus().name().equals("ACTIVE"))
                .build();
    }

    private static List<Category> getEffectiveCategories(Product product) {
        List<Category> categories = new ArrayList<>();
        if (product.getCategories() != null) {
            categories.addAll(product.getCategories());
        }
        if (categories.isEmpty() && product.getCategory() != null) {
            categories.add(product.getCategory());
        }
        return categories;
    }

    private static List<String> mapImageUrls(List<ProductImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(ProductImage::getImageUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Double calculateAverageRating(Product product) {
        if (product.getReviews() == null || product.getReviews().isEmpty()) {
            return null;
        }
        return product.getReviews().stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(r -> r.getRating())
                .average()
                .orElse(0D);
    }

    private static String buildSpecs(Product product) {
        return List.of(product.getMovementType(), product.getGlassMaterial(), product.getWaterResistance(), product.getWireMaterial())
                .stream()
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(", "));
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

