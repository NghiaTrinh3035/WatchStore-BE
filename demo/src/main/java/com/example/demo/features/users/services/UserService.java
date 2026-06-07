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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;
    private final UserProfileService userProfileService;

    public User createUser(User user) {
        accessControlService.requireOwnerRole();
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (user.getRole() == UserRole.CUSTOMER) {
            throw new IllegalStateException("Use /api/auth/register to create CUSTOMER accounts");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        userProfileService.syncProfileForRole(savedUser);
        return savedUser;
    }

    public User updateUser(String id, User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User currentUser = accessControlService.getCurrentUserOrThrow();
        boolean isOwner = accessControlService.isOwner(currentUser);
        boolean isSelf = currentUser.getId().equals(existing.getId());

        if (!isOwner && !isSelf) {
            throw new org.springframework.security.access.AccessDeniedException("You are not allowed to update this user");
        }

        if (!existing.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }
        if (!existing.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }

        // Password updates are intentionally handled by auth flow (OTP/reset-password).
        existing.setUsername(user.getUsername());
        existing.setFullName(user.getFullName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setAddress(user.getAddress());
        existing.setGender(user.getGender());
        if (isOwner) {
            if (user.getRole() != null) {
                existing.setRole(user.getRole());
            }
        } else if (user.getRole() != null && user.getRole() != existing.getRole()) {
            throw new org.springframework.security.access.AccessDeniedException("Only OWNER can change user role");
        }

        User updatedUser = userRepository.save(existing);
        userProfileService.syncProfileForRole(updatedUser);
        return updatedUser;
    }

    public void changePassword(String id, ChangePasswordRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User currentUser = accessControlService.getCurrentUserOrThrow();

        if (!currentUser.getId().equals(existing.getId())) {
            throw new AccessDeniedException("You are not allowed to change this password");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), existing.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        existing.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(existing);
    }

    public User lockStaff(String id) {
        return updateStaffActiveStatus(id, false);
    }

    public User unlockStaff(String id) {
        return updateStaffActiveStatus(id, true);
    }

    public void deleteUser(String id) {
        accessControlService.requireOwnerRole();
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        accessControlService.requireUserSelfOrPrivileged(id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        accessControlService.requirePrivilegedRole();
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        accessControlService.requirePrivilegedRole();
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findAllByRole(UserRole role) {
        accessControlService.requireOwnerRole();
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        accessControlService.requirePrivilegedRole();
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        accessControlService.requirePrivilegedRole();
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        accessControlService.requirePrivilegedRole();
        return userRepository.existsByEmail(email);
    }

    private User updateStaffActiveStatus(String userId, boolean isActive) {
        accessControlService.requireOwnerRole();

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (existing.getRole() != UserRole.STAFF) {
            throw new IllegalStateException("Only STAFF accounts can be locked/unlocked from staff management");
        }

        existing.setIsActive(isActive);
        return userRepository.save(existing);
    }
}





