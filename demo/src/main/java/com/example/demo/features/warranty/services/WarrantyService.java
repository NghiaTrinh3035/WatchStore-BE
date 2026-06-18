package com.example.demo.features.warranty.services;


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

import com.example.demo.features.warranty.events.WarrantyStatusUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;
    private final ApplicationEventPublisher eventPublisher;

    public WarrantyResponse createWarrantyRequest(WarrantyRequest request) {
        accessControlService.requirePrivilegedRole();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.getProductId()));

        Warranty warranty = new Warranty();
        warranty.setUserId(null);
        warranty.setCustomerId(normalizeBlankToNull(request.getCustomerId()));
        warranty.setOrderId(null);
        warranty.setOrderItemId(null);
        warranty.setCustomerPhone(request.getCustomerPhone().trim());
        warranty.setCustomerName(request.getCustomerName().trim());
        warranty.setIssueDescription(request.getIssueDescription().trim());
        warranty.setReceivedDate(request.getReceivedDate());
        warranty.setExpectedReturnDate(request.getExpectedReturnDate());
        warranty.setQuantity(request.getQuantity());
        warranty.setTechnicianNote(normalizeBlankToNull(request.getTechnicianNote()));
        warranty.setStatus(request.getStatus() == null ? WarrantyStatus.RECEIVED : request.getStatus());
        warranty.setProduct(product);

        return DtoMapper.toWarrantyResponse(warrantyRepository.save(warranty));
    }

    public WarrantyResponse createCustomerWarrantyRequest(CustomerWarrantyRequest request) {
        User currentUser = accessControlService.getCurrentUserOrThrow();
        if (currentUser.getRole() != UserRole.CUSTOMER) {
            throw new org.springframework.security.access.AccessDeniedException("Only CUSTOMER is allowed for this action");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to create warranty for this order");
        }

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ tạo bảo hành cho đơn đã giao hoặc hoàn tất.");
        }

        OrderItem selectedItem = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + request.getOrderItemId()));

        Warranty warranty = new Warranty();
        warranty.setUserId(currentUser.getId());
        warranty.setCustomerId(currentUser.getId());
        warranty.setOrderId(order.getId());
        warranty.setOrderItemId(selectedItem.getId());
        warranty.setCustomerPhone(order.getCustomer().getPhone().trim());
        warranty.setCustomerName(order.getCustomer().getFullName().trim());
        warranty.setIssueDescription(request.getDescription().trim());
        warranty.setReceivedDate(new Date());
        warranty.setExpectedReturnDate(new Date(System.currentTimeMillis() + 2L * 24 * 60 * 60 * 1000));
        warranty.setQuantity(selectedItem.getQuantity());
        warranty.setTechnicianNote(null);
        warranty.setStatus(WarrantyStatus.RECEIVED);
        warranty.setProduct(selectedItem.getProduct());

        Warranty saved = warrantyRepository.save(warranty);
        eventPublisher.publishEvent(new WarrantyStatusUpdatedEvent(this, saved, currentUser, saved.getStatus()));
        return DtoMapper.toWarrantyResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<WarrantyResponse> getWarrantyRequestDetail(String id) {
        accessControlService.requirePrivilegedRole();
        return warrantyRepository.findById(id).map(DtoMapper::toWarrantyResponse);
    }

    @Transactional(readOnly = true)
    public Page<WarrantyResponse> searchWarrantyRequests(WarrantySearchRequest request, Pageable pageable) {
        accessControlService.requirePrivilegedRole();
        return warrantyRepository.searchWarrantyRequests(normalizeSearchText(request.getKeyword()), request.getStatus(), pageable)
                .map(DtoMapper::toWarrantyResponse);
    }

    @Transactional(readOnly = true)
    public Page<WarrantyResponse> getMyWarrantyRequests(Pageable pageable) {
        User currentUser = accessControlService.getCurrentUserOrThrow();
        if (currentUser.getRole() != UserRole.CUSTOMER) {
            throw new org.springframework.security.access.AccessDeniedException("Only CUSTOMER is allowed for this action");
        }

        return warrantyRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(DtoMapper::toWarrantyResponse);
    }

    public WarrantyResponse approveWarrantyRequest(String id, String technicianNote) {
        WarrantyProcessRequest processRequest = new WarrantyProcessRequest();
        processRequest.setStatus(WarrantyStatus.COMPLETED);
        processRequest.setTechnicianNote(technicianNote);
        return processWarrantyRequest(id, processRequest);
    }

    public WarrantyResponse rejectWarrantyRequest(String id, String rejectReason) {
        WarrantyProcessRequest processRequest = new WarrantyProcessRequest();
        processRequest.setStatus(WarrantyStatus.REJECTED);
        processRequest.setRejectReason(rejectReason);
        return processWarrantyRequest(id, processRequest);
    }

    public WarrantyResponse processWarrantyRequest(String id, WarrantyProcessRequest request) {
        accessControlService.requirePrivilegedRole();

        Warranty warranty = warrantyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warranty request not found: " + id));

        validateTransition(warranty.getStatus(), request.getStatus());
        if (request.getStatus() == WarrantyStatus.REJECTED
                && (request.getRejectReason() == null || request.getRejectReason().isBlank())) {
            throw new IllegalArgumentException("Reject reason is required when rejecting warranty request");
        }

        User processor = accessControlService.getCurrentUserOrThrow();
        String processedNote = buildProcessedNote(request, processor);
        String processedRejectReason = buildProcessedRejectReason(request, processor);

        warranty.setStatus(request.getStatus());
        warranty.setTechnicianNote(processedNote);
        warranty.setRejectReason(processedRejectReason);

        Warranty saved = warrantyRepository.save(warranty);
        eventPublisher.publishEvent(new WarrantyStatusUpdatedEvent(this, saved, processor, request.getStatus()));
        return DtoMapper.toWarrantyResponse(saved);
    }

    private void validateTransition(WarrantyStatus currentStatus, WarrantyStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Warranty status is required");
        }
        if (currentStatus == WarrantyStatus.COMPLETED || currentStatus == WarrantyStatus.REJECTED) {
            throw new IllegalStateException("Cannot process warranty request with final status: " + currentStatus);
        }
    }

    private String buildProcessedNote(WarrantyProcessRequest request, User processor) {
        String actor = processor.getFullName() == null || processor.getFullName().isBlank()
                ? processor.getUsername()
                : processor.getFullName();

        if (request.getTechnicianNote() != null && !request.getTechnicianNote().isBlank()) {
            return "Processed by " + actor + ": " + request.getTechnicianNote().trim();
        }

        return "Processed by " + actor;
    }

    private String buildProcessedRejectReason(WarrantyProcessRequest request, User processor) {
        if (request.getStatus() != WarrantyStatus.REJECTED) {
            return null;
        }

        String actor = processor.getFullName() == null || processor.getFullName().isBlank()
                ? processor.getUsername()
                : processor.getFullName();

        return "Rejected by " + actor + ": " + request.getRejectReason().trim();
    }


    private String normalizeSearchText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
