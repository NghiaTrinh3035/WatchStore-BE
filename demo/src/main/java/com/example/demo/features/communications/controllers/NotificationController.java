package com.example.demo.features.communications.controllers;


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
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId, isRead, resolveType(type))
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId, isRead, resolveType(type))
        );
    }

    @GetMapping("/receiver/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByReceiver(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId, isRead, resolveType(type))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @RequestParam String userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(userId, isRead, resolveType(type)));
    }

    @PatchMapping({"/{notificationId}/read", "/read/{notificationId}"})
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId,
            @RequestParam(required = false) String userId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.markAsRead(resolveUserId(null, userId, payload), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping({"/{notificationId}/read", "/read/{notificationId}"})
    public ResponseEntity<Void> markAsReadByPut(
            @PathVariable String notificationId,
            @RequestParam(required = false) String userId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.markAsRead(resolveUserId(null, userId, payload), notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping({"/user/{userId}/read-all", "/read-all"})
    public ResponseEntity<Void> markAllAsRead(
            @PathVariable(required = false) String userId,
            @RequestParam(required = false) String queryUserId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.markAllAsRead(resolveUserId(userId, queryUserId, payload));
        return ResponseEntity.noContent().build();
    }

    @PutMapping({"/user/{userId}/read-all", "/read-all"})
    public ResponseEntity<Void> markAllAsReadByPut(
            @PathVariable(required = false) String userId,
            @RequestParam(required = false) String queryUserId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.markAllAsRead(resolveUserId(userId, queryUserId, payload));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/{notificationId}", "/delete/{notificationId}"})
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String notificationId,
            @RequestParam(required = false) String userId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.deleteNotification(resolveUserId(null, userId, payload), notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/user/{userId}", "/clear-all"})
    public ResponseEntity<Void> clearAllNotifications(
            @PathVariable(required = false) String userId,
            @RequestParam(required = false) String queryUserId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        notificationService.clearAllNotifications(resolveUserId(userId, queryUserId, payload));
        return ResponseEntity.noContent().build();
    }

    private String resolveUserId(String pathUserId, String queryUserId, Map<String, String> payload) {
        if (StringUtils.hasText(pathUserId)) {
            return pathUserId;
        }
        if (StringUtils.hasText(queryUserId)) {
            return queryUserId;
        }
        if (payload != null) {
            String bodyUserId = payload.get("userId");
            if (StringUtils.hasText(bodyUserId)) {
                return bodyUserId;
            }
        }
        throw new IllegalArgumentException("userId is required");
    }

    private NotificationType resolveType(String type) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        try {
            return NotificationType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid notification type: " + type);
        }
    }
}
