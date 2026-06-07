package com.example.demo.features.inventory.services;


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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportReceiptService {

    private final ImportReceiptRepository importReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final OwnerRepository ownerRepository;
    private final AccessControlService accessControlService;

    public ImportReceiptResponse createImportReceipt(ImportReceiptRequest request) {
        accessControlService.requireOwnerRole();
        User currentUser = accessControlService.getCurrentUserOrThrow();

        ownerRepository.upsertOwnerProfile(currentUser.getId());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.getSupplierId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Import items are required");
        }

        ImportReceipt receipt = ImportReceipt.builder()
                .supplier(supplier)
                .owner(currentUser)
                .note(request.getNote())
                .importDetails(new ArrayList<>())
                .build();

        for (ImportReceiptItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            ImportDetail detail = ImportDetail.builder()
                    .importReceipt(receipt)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .importPrice(itemRequest.getImportPrice())
                    .build();
            receipt.getImportDetails().add(detail);

            product.setStockQuantity(product.getStockQuantity() + itemRequest.getQuantity());
            // product.setUnitCost(itemRequest.getImportPrice());
            productRepository.save(product);
        }

        ImportReceipt saved = importReceiptRepository.save(receipt);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ImportReceiptResponse> findAll(
            String supplierId,
            Date fromDate,
            Date toDate,
            String keyword
    ) {
        accessControlService.requireOwnerRole();
        User currentUser = accessControlService.getCurrentUserOrThrow();
        String ownerId = currentUser.getId();

        List<ImportReceipt> receipts = StringUtils.hasText(supplierId)
                ? importReceiptRepository.findByOwnerIdAndSupplierId(ownerId, supplierId)
                : importReceiptRepository.findByOwnerId(ownerId);

        Date start = normalizeStart(fromDate);
        Date end = normalizeEnd(toDate);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim().toLowerCase() : null;

        return receipts.stream()
                .filter(receipt -> isInDateRange(receipt.getImportDate(), start, end))
                .filter(receipt -> containsKeyword(receipt, normalizedKeyword))
                .sorted(Comparator.comparing(ImportReceipt::getImportDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportReceiptResponse findById(String id) {
        accessControlService.requireOwnerRole();
        User currentUser = accessControlService.getCurrentUserOrThrow();

        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import receipt not found: " + id));

        if (!currentUser.getId().equals(receipt.getOwner().getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not allowed to access this import receipt");
        }

        return toResponse(receipt);
    }

    private ImportReceiptResponse toResponse(ImportReceipt receipt) {
        List<ImportReceiptItemResponse> items = receipt.getImportDetails().stream()
                .map(detail -> ImportReceiptItemResponse.builder()
                        .id(detail.getId())
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .quantity(detail.getQuantity())
                        .importPrice(detail.getImportPrice())
                        .lineTotal(detail.getImportPrice() * detail.getQuantity())
                        .build())
                .toList();

        long totalAmount = items.stream()
                .mapToLong(item -> item.getLineTotal() == null ? 0L : item.getLineTotal())
                .sum();

        return ImportReceiptResponse.builder()
                .id(receipt.getId())
                .importDate(receipt.getImportDate())
                .note(receipt.getNote())
                .supplierId(receipt.getSupplier().getId())
                .supplierName(receipt.getSupplier().getName())
                .ownerId(receipt.getOwner().getId())
                .items(items)
                .totalAmount(totalAmount)
                .totalCost(totalAmount)
                .build();
    }

    private boolean isInDateRange(Date value, Date start, Date end) {
        if (value == null) {
            return false;
        }
        if (start != null && value.before(start)) {
            return false;
        }
        return end == null || !value.after(end);
    }

    private boolean containsKeyword(ImportReceipt receipt, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        if (containsIgnoreCase(receipt.getId(), keyword)
                || containsIgnoreCase(receipt.getSupplier().getName(), keyword)
                || containsIgnoreCase(receipt.getNote(), keyword)) {
            return true;
        }

        return receipt.getImportDetails().stream()
                .anyMatch(detail -> containsIgnoreCase(detail.getProduct().getName(), keyword));
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private Date normalizeStart(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date normalizeEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
