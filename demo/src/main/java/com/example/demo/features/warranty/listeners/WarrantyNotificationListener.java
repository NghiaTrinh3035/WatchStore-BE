package com.example.demo.features.warranty.listeners;

import com.example.demo.core.enums.NotificationType;
import com.example.demo.core.enums.WarrantyStatus;
import com.example.demo.features.communications.entities.Notification;
import com.example.demo.features.communications.repositories.NotificationRepository;
import com.example.demo.features.users.entities.Customer;
import com.example.demo.features.users.entities.User;
import com.example.demo.features.users.repositories.CustomerRepository;
import com.example.demo.features.warranty.entities.Warranty;
import com.example.demo.features.warranty.events.WarrantyStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarrantyNotificationListener {

    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;

    @Async
    @EventListener
    public void onWarrantyStatusUpdated(WarrantyStatusUpdatedEvent event) {
        try {
            Warranty warranty = event.getWarranty();
            User sender = event.getSender();
            WarrantyStatus status = event.getStatus();

            if (warranty.getCustomerId() == null || warranty.getCustomerId().isBlank()) {
                return;
            }

            Optional<Customer> customerOpt = customerRepository.findById(warranty.getCustomerId());
            if (customerOpt.isEmpty()) {
                return;
            }

            String content = status == WarrantyStatus.REJECTED
                    ? "Your warranty request has been rejected. Please check details from support."
                    : "Your warranty request has been updated to status: " + status;

            Notification notification = Notification.builder()
                    .title("Warranty request update")
                    .content(content)
                    .type(NotificationType.WARRANTY)
                    .directUrl("/warranty")
                    .sender(sender)
                    .receiver(customerOpt.get())
                    .build();

            notificationRepository.save(notification);
            log.info("Saved warranty notification for customer: {}", warranty.getCustomerId());
        } catch (Exception ex) {
            log.warn("Failed to process warranty notification for warranty {}", event.getWarranty().getId(), ex);
        }
    }
}
