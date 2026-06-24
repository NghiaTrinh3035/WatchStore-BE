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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").hasRole("OWNER")

                        .requestMatchers("/api/staff/**").hasRole("OWNER")

                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/customers/**").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasAnyRole("STAFF", "OWNER", "CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasAnyRole("STAFF", "OWNER")

                        .requestMatchers(HttpMethod.GET, "/api/warranties/customer").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/warranties/customer/**").hasRole("CUSTOMER")

                        .requestMatchers("/api/suppliers/**").hasRole("OWNER")
                        .requestMatchers("/api/import-receipts/**").hasRole("OWNER")
                        .requestMatchers("/api/warranties/**").hasAnyRole("STAFF", "OWNER")

                        .requestMatchers(HttpMethod.GET, "/api/vouchers", "/api/vouchers/search").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/vouchers").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/vouchers/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/vouchers/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/vouchers/**").hasRole("OWNER")

                        .requestMatchers("/api/payment/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/status").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/products/*/discussions").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**").hasAnyRole("STAFF", "OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**").hasAnyRole("STAFF", "OWNER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isEmpty()) {
                user = userRepository.findByEmail(username);
            }

            User foundUser = user.orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return org.springframework.security.core.userdetails.User
                    .withUsername(foundUser.getUsername())
                    .password(foundUser.getPassword())
                    .disabled(Boolean.FALSE.equals(foundUser.getIsActive()))
                    .roles(foundUser.getRole().name())
                    .build();
        };
    }
}
