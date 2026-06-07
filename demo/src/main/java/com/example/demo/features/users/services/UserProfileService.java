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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final StaffRepository staffRepository;

    public void syncProfileForRole(User user) {
        if (user == null || user.getId() == null || user.getRole() == null) {
            throw new IllegalArgumentException("User, id, and role are required to sync profiles");
        }

        switch (user.getRole()) {
            case CUSTOMER -> {
                ensureCustomerProfile(user);
                removeOwnerProfileIfExists(user.getId());
            }
            case OWNER -> {
                ensureOwnerProfile(user);
                removeCustomerProfileIfExists(user.getId());
            }
            case STAFF -> {
                ensureStaffProfile(user);
                removeCustomerProfileIfExists(user.getId());
                removeOwnerProfileIfExists(user.getId());
            }
        }
    }

    private void ensureCustomerProfile(User user) {
        if (customerRepository.existsById(user.getId())) {
            return;
        }

        Customer customer = new Customer();
        customer.setId(user.getId());
        customerRepository.save(customer);
    }

    private void ensureOwnerProfile(User user) {
        ownerRepository.upsertOwnerProfile(user.getId());
    }

    private void ensureStaffProfile(User user) {
        if (staffRepository.existsById(user.getId())) {
            return;
        }
        staffRepository.insertStaffProfile(user.getId());
    }

    private void removeCustomerProfileIfExists(String userId) {
        if (!customerRepository.existsById(userId)) {
            return;
        }

        try {
            customerRepository.deleteCustomerProfileById(userId);
            customerRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Cannot remove customer profile because it is referenced by business data", ex);
        }
    }

    private void removeOwnerProfileIfExists(String userId) {
        if (!ownerRepository.existsById(userId)) {
            return;
        }

        try {
            ownerRepository.deleteById(userId);
            ownerRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Cannot remove owner profile because it is referenced by business data", ex);
        }
    }
}
