package com.example.demo.core.config;


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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SystemBotInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${notification.system-bot.username:system.bot}")
    private String botUsername;

    @Value("${notification.system-bot.email:system.bot@local}")
    private String botEmail;

    @Value("${notification.system-bot.password:system-bot-secret}")
    private String botPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalizedUsername = normalize(botUsername);
        String normalizedEmail = normalize(botEmail).toLowerCase();

        if (!StringUtils.hasText(normalizedUsername) || !StringUtils.hasText(normalizedEmail)) {
            throw new IllegalStateException("notification.system-bot.username and notification.system-bot.email are required");
        }

        User byUsername = userRepository.findByUsername(normalizedUsername).orElse(null);
        User byEmail = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (byUsername != null && byEmail != null && !byUsername.getId().equals(byEmail.getId())) {
            throw new IllegalStateException("System BOT config is ambiguous: username and email point to different users");
        }

        User bot = byUsername != null ? byUsername : byEmail;
        if (bot == null) {
            User newBot = User.builder()
                    .username(normalizedUsername)
                    .password(passwordEncoder.encode(normalize(botPassword)))
                    .fullName("System Bot")
                    .email(normalizedEmail)
                    .address("SYSTEM")
                    .role(UserRole.STAFF)
                    .isActive(true)
                    .build();
            userRepository.save(newBot);
            return;
        }

        boolean changed = false;
        if (!StringUtils.hasText(bot.getAddress())) {
            bot.setAddress("SYSTEM");
            changed = true;
        }
        if (!StringUtils.hasText(bot.getFullName())) {
            bot.setFullName("System Bot");
            changed = true;
        }
        if (bot.getRole() != UserRole.STAFF && bot.getRole() != UserRole.OWNER) {
            bot.setRole(UserRole.STAFF);
            changed = true;
        }

        if (changed) {
            userRepository.save(bot);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
