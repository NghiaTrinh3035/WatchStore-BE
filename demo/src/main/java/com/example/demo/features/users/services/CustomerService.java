package com.example.demo.features.users.services;


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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final AccessControlService accessControlService;

    public void deleteCustomer(String id) {
        accessControlService.requirePrivilegedRole();

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        if (customerRepository.existsRelatedOrders(id)) {
            throw new IllegalStateException("Cannot delete customer because related orders exist");
        }

        customerRepository.delete(customer);
    }

    public CustomerResponse promoteCustomerToStaff(String id) {
        accessControlService.requireOwnerRole();

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalStateException("User is not a customer");
        }

        if (customerRepository.existsRelatedOrders(id)
                || customerRepository.existsRelatedReviews(id)) {
            throw new IllegalStateException("Không thể nâng quyền vì khách hàng đang có dữ liệu đơn hàng/đánh giá");
        }

        customer.setRole(UserRole.STAFF);
        customer.setIsActive(true);
        Customer promotedUser = customerRepository.saveAndFlush(customer);

        if (!staffRepository.existsById(id)) {
            staffRepository.insertStaffProfile(id);
        }

        CustomerResponse response = DtoMapper.toCustomerResponse(promotedUser);
        try {
            if (cartRepository.existsByCustomerId(id)) {
                cartRepository.deleteItemsByCustomerId(id);
                cartRepository.deleteByCustomerId(id);
            }
            customerRepository.deleteCustomerProfileById(id);
            customerRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Không thể xóa hồ sơ customer cũ sau khi nâng quyền", ex);
        }

        return response;
    }

    public CustomerResponse lockCustomer(String id) {
        return updateCustomerActiveStatus(id, false);
    }

    public CustomerResponse unlockCustomer(String id) {
        return updateCustomerActiveStatus(id, true);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerResponse> findById(String id) {
        accessControlService.requireCustomerAccess(id);
        return customerRepository.findById(id)
                .filter(customer -> customer.getRole() == UserRole.CUSTOMER)
                .map(DtoMapper::toCustomerResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(CustomerSearchRequest request, Pageable pageable) {
        accessControlService.requirePrivilegedRole();
        return customerRepository.searchCustomers(
                        UserRole.CUSTOMER,
                        normalizeSearchText(request.getFullName()),
                        normalizeSearchText(request.getEmail()),
                        normalizeSearchText(request.getPhone()),
                        normalizeSearchText(request.getAddress()),
                        pageable)
                .map(DtoMapper::toCustomerResponse);
    }

    private String normalizeSearchText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private CustomerResponse updateCustomerActiveStatus(String customerId, boolean isActive) {
        accessControlService.requirePrivilegedRole();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        customer.setIsActive(isActive);
        return DtoMapper.toCustomerResponse(customerRepository.save(customer));
    }
}

