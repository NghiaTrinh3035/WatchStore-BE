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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiChatService aiChatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askBot(@Valid @RequestBody ChatRequest request) {
        String question = request.getMessage().trim();
        AiChatService.AiChatResult result = aiChatService.chatWithBot(question);

        ChatResponse response = ChatResponse.builder()
                .sender("System Bot")
                .message(result.message())
                .handledBy(result.handledBy())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/escalate")
    public ResponseEntity<ChatResponse> escalateToStaff() {
        AiChatService.AiChatResult result = aiChatService.escalateToStaff();

        ChatResponse response = ChatResponse.builder()
                .sender("System Bot")
                .message(result.message())
                .handledBy(result.handledBy())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryMessageResponse>> getMyHistory() {
        List<ChatHistoryMessageResponse> data = aiChatService.getMyChatHistory().stream()
                .map(message -> ChatHistoryMessageResponse.builder()
                        .id(message.id())
                        .role(message.role())
                        .content(message.content())
                        .createdAt(message.createdAt())
                        .handledBy(message.handledBy())
                        .build())
                .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/support/pending")
    public ResponseEntity<List<SupportChatResponse>> getPendingSupportChats() {
        List<SupportChatResponse> data = aiChatService.getOpenStaffSupportChats().stream()
                .map(this::toSupportResponse)
                .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/support/all")
    public ResponseEntity<PageResponse<SupportChatResponse>> getAllSupportChats(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int pageSize,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize);

        Page<Chat> result = aiChatService.getStaffSupportChatsPage(status, pageable);
        List<SupportChatResponse> items = result.getContent().stream()
                .map(this::toSupportResponse)
                .toList();
        PageResponse<SupportChatResponse> response = PageResponse.<SupportChatResponse>builder()
                .items(items)
                .page(safePage)
                .pageSize(safePageSize)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/support/my-active")
    public ResponseEntity<SupportChatResponse> getMyActiveSupportChat() {
        SupportChatResponse response = aiChatService.getMyOpenSupportChat()
                .map(this::toSupportResponse)
                .orElse(null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/support/{chatId}/reply")
    public ResponseEntity<Void> replySupportChat(
            @PathVariable String chatId,
            @Valid @RequestBody SupportReplyRequest request
    ) {
        aiChatService.replySupportChat(chatId, request.getMessage().trim());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/support/{chatId}/close")
    public ResponseEntity<Void> closeSupportChat(@PathVariable String chatId) {
        aiChatService.closeSupportChat(chatId);
        return ResponseEntity.noContent().build();
    }

    private SupportChatResponse toSupportResponse(Chat chat) {
        String customerName = chat.getCustomer() != null ? chat.getCustomer().getFullName() : null;
        String customerId = chat.getCustomer() != null ? chat.getCustomer().getId() : null;
        return SupportChatResponse.builder()
                .id(chat.getId())
                .customerId(customerId)
                .customerName(customerName)
                .startDate(chat.getStartDate())
                .endDate(chat.getEndDate())
                .contentLog(aiChatService.buildChatContentLog(chat))
                .aiHandled(chat.getIsAiHandled())
                .build();
    }
}
