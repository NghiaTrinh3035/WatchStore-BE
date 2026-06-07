package com.example.demo.features.warranty.controllers;


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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warranties")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping
    public ResponseEntity<Page<WarrantyResponse>> getAll(
            @Valid @ModelAttribute WarrantySearchRequest request,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(warrantyService.searchWarrantyRequests(request, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<WarrantyResponse>> search(
            @Valid @ModelAttribute WarrantySearchRequest request,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(warrantyService.searchWarrantyRequests(request, pageable));
    }

    @GetMapping("/customer")
    public ResponseEntity<Page<WarrantyResponse>> getMyWarranties(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(warrantyService.getMyWarrantyRequests(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarrantyResponse> getById(@PathVariable String id) {
        return warrantyService.getWarrantyRequestDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<WarrantyResponse> create(@Valid @RequestBody WarrantyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warrantyService.createWarrantyRequest(request));
    }

    @PostMapping("/customer/create")
    public ResponseEntity<WarrantyResponse> createForCustomer(@Valid @RequestBody CustomerWarrantyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warrantyService.createCustomerWarrantyRequest(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WarrantyResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody WarrantyProcessRequest request) {
        return ResponseEntity.ok(warrantyService.processWarrantyRequest(id, request));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<WarrantyResponse> approve(
            @PathVariable String id,
            @RequestParam(required = false) String technicianNote) {
        return ResponseEntity.ok(warrantyService.approveWarrantyRequest(id, technicianNote));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<WarrantyResponse> reject(
            @PathVariable String id,
            @RequestParam String rejectReason) {
        return ResponseEntity.ok(warrantyService.rejectWarrantyRequest(id, rejectReason));
    }
}

