package com.example.demo.features.warranty.events;

import com.example.demo.core.enums.WarrantyStatus;
import com.example.demo.features.users.entities.User;
import com.example.demo.features.warranty.entities.Warranty;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WarrantyStatusUpdatedEvent extends ApplicationEvent {
    private final Warranty warranty;
    private final User sender;
    private final WarrantyStatus status;

    public WarrantyStatusUpdatedEvent(Object source, Warranty warranty, User sender, WarrantyStatus status) {
        super(source);
        this.warranty = warranty;
        this.sender = sender;
        this.status = status;
    }
}
