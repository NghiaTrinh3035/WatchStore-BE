package com.example.demo.features.auth.listeners;

import com.example.demo.features.auth.events.PasswordResetEvent;
import com.example.demo.features.auth.events.UserRegisteredEvent;
import com.example.demo.features.communications.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthNotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            notificationService.sendRegistrationSuccessNotification(event.getUser());
            log.info("Sent registration success notification for user: {}", event.getUser().getEmail());
        } catch (Exception ex) {
            log.warn("Registration success notification failed for user {}", event.getUser().getId(), ex);
        }
    }

    @Async
    @EventListener
    public void onPasswordReset(PasswordResetEvent event) {
        try {
            notificationService.sendPasswordResetSuccessNotification(event.getUser());
            log.info("Sent password reset success notification for user: {}", event.getUser().getEmail());
        } catch (Exception ex) {
            log.warn("Password reset notification failed for user {}", event.getUser().getId(), ex);
        }
    }
}
