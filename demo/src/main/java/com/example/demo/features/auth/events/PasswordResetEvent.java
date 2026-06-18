package com.example.demo.features.auth.events;

import com.example.demo.features.users.entities.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetEvent extends ApplicationEvent {
    private final User user;

    public PasswordResetEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
