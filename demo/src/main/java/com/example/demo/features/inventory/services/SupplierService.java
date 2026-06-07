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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AccessControlService accessControlService;

    public SupplierResponse createSupplier(SupplierRequest request) {
        accessControlService.requireOwnerRole();
        validateSupplierUnique(request, null);

        Supplier supplier = new Supplier();
        applySupplierRequest(supplier, request);
        return DtoMapper.toSupplierResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse updateSupplier(String id, SupplierRequest request) {
        accessControlService.requireOwnerRole();

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));

        validateSupplierUnique(request, id);
        applySupplierRequest(supplier, request);

        return DtoMapper.toSupplierResponse(supplierRepository.save(supplier));
    }

    public void deleteSupplier(String id) {
        accessControlService.requireOwnerRole();

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));

        if (supplierRepository.existsRelatedRecords(id)) {
            throw new IllegalStateException("Không thể xóa nhà cung cấp vì đã có hóa đơn nhập hàng");
        }

        supplierRepository.delete(supplier);
    }

    @Transactional(readOnly = true)
    public Optional<SupplierResponse> findById(String id) {
        accessControlService.requireOwnerRole();
        return supplierRepository.findById(id).map(DtoMapper::toSupplierResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliers(SupplierSearchRequest request, Pageable pageable) {
        accessControlService.requireOwnerRole();
        return supplierRepository.searchSuppliers(
                        normalizeSearchText(request.getKeyword()),
                        normalizeSearchText(request.getName()),
                        normalizeSearchText(request.getContractInfo()),
                        normalizeSearchText(request.getAddress()),
                        pageable)
                .map(DtoMapper::toSupplierResponse);
    }

    private void validateSupplierUnique(SupplierRequest request, String supplierId) {
        String normalizedName = request.getName().trim();

        if (supplierId == null) {
            if (supplierRepository.existsByName(normalizedName)) {
                throw new IllegalStateException("Supplier name already exists");
            }
            return;
        }

        if (supplierRepository.existsByNameAndIdNot(normalizedName, supplierId)) {
            throw new IllegalStateException("Supplier name already exists");
        }
    }

    private void applySupplierRequest(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.getName().trim());
        supplier.setContractInfo(normalizeBlankToNull(request.getContractInfo()));
        supplier.setAddress(normalizeBlankToNull(request.getAddress()));
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
