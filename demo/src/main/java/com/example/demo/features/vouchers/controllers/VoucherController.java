package com.example.demo.features.vouchers.controllers;


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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<Page<Voucher>> getAll(
            @Valid @ModelAttribute VoucherSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(voucherService.searchVouchers(request, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Voucher>> search(
            @Valid @ModelAttribute VoucherSearchRequest request,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(voucherService.searchVouchers(request, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Voucher>> getActive() {
        return ResponseEntity.ok(voucherService.findActiveVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getById(@PathVariable String id) {
        return voucherService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Voucher> getByCode(@PathVariable String code) {
        return voucherService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Voucher> create(@Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherService.createVoucher(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> update(@PathVariable String id, @Valid @RequestBody VoucherUpdateRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Voucher> updateStatus(@PathVariable String id, @Valid @RequestBody VoucherStatusUpdateRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucherStatus(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Voucher> deactivate(@PathVariable String id) {
        return ResponseEntity.ok(voucherService.disableVoucher(id));
    }

    @PostMapping("/apply/{code}")
    public ResponseEntity<Voucher> apply(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.applyVoucher(code));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
