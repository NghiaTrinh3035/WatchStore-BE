package com.example.demo.features.communications.repositories;


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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

    Optional<Chat> findFirstByCustomerIdAndIsAiHandledFalseAndEndDateIsNullOrderByStartDateDesc(String customerId);

    @Query(
            value = "SELECT c FROM Chat c JOIN FETCH c.customer cu WHERE c.isAiHandled = false ORDER BY c.startDate DESC",
            countQuery = "SELECT COUNT(c) FROM Chat c JOIN c.customer cu WHERE c.isAiHandled = false"
    )
    Page<Chat> findAllSupportChatsPageWithCustomer(Pageable pageable);

    @Query(
            value = "SELECT c FROM Chat c JOIN FETCH c.customer cu WHERE c.isAiHandled = false AND c.endDate IS NULL ORDER BY c.startDate DESC",
            countQuery = "SELECT COUNT(c) FROM Chat c JOIN c.customer cu WHERE c.isAiHandled = false AND c.endDate IS NULL"
    )
    Page<Chat> findOpenSupportChatsPageWithCustomer(Pageable pageable);

    @Query(
            value = "SELECT c FROM Chat c JOIN FETCH c.customer cu WHERE c.isAiHandled = false AND c.endDate IS NOT NULL ORDER BY c.startDate DESC",
            countQuery = "SELECT COUNT(c) FROM Chat c JOIN c.customer cu WHERE c.isAiHandled = false AND c.endDate IS NOT NULL"
    )
    Page<Chat> findClosedSupportChatsPageWithCustomer(Pageable pageable);

    @Query("SELECT c FROM Chat c JOIN FETCH c.customer WHERE c.isAiHandled = false AND c.endDate IS NULL ORDER BY c.startDate DESC")
    List<Chat> findOpenSupportChatsWithCustomer();

    @Query("SELECT c FROM Chat c JOIN FETCH c.customer WHERE c.isAiHandled = false ORDER BY c.startDate DESC")
    List<Chat> findAllSupportChatsWithCustomer();
}
