package com.example.demo.features.reports.services;


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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AccessControlService accessControlService;

    public DashboardReportResponse getDashboardReport(ReportFilterRequest filterRequest) {
        accessControlService.requireOwnerRole();
        Date fromDate = filterRequest != null ? filterRequest.getFromDate() : null;
        Date toDate = filterRequest != null ? filterRequest.getToDate() : null;

        return DashboardReportResponse.builder()
                .summary(getDashboardSummary(fromDate, toDate))
                .statistics(getDashboardStatistic(fromDate, toDate, 5))
                .build();
    }

    public DashboardSummaryResponse getDashboardSummary(Date fromDate, Date toDate) {
        accessControlService.requireOwnerRole();
        try {
            Long totalRevenue = nonNullLong(orderRepository.sumRevenueBetween(fromDate, toDate));
            long totalOrders = orderRepository.countOrdersBetween(fromDate, toDate);
            long newCustomers = customerRepository.countNewCustomersBetween(fromDate, toDate);
            Long totalProductsSold = nonNullLong(orderRepository.sumSoldQuantityBetween(fromDate, toDate));

            return DashboardSummaryResponse.builder()
                    .totalRevenue(totalRevenue)
                    .totalOrders(totalOrders)
                    .newCustomers(newCustomers)
                    .totalProductsSold(totalProductsSold)
                    .build();
        } catch (RuntimeException ex) {
            log.error("Failed to query dashboard summary from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot generate dashboard summary at the moment");
        }
    }

    public DashboardStatisticResponse getDashboardStatistic(Date fromDate, Date toDate, int topLimit) {
        accessControlService.requireOwnerRole();
        try {
            return DashboardStatisticResponse.builder()
                    .revenueByTime(getRevenueStatistics(fromDate, toDate))
                    .ordersByDay(getOrderStatisticsByDay(fromDate, toDate))
                    .ordersByMonth(getOrderStatisticsByMonth(fromDate, toDate))
                    .topSellingProducts(getTopSellingProducts(fromDate, toDate, topLimit))
                    .build();
        } catch (RuntimeException ex) {
            log.error("Failed to query dashboard statistics from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot generate dashboard statistics at the moment");
        }
    }

    public List<RevenueByTimeResponse> getRevenueStatistics(Date fromDate, Date toDate) {
        accessControlService.requireOwnerRole();
        try {
            List<Object[]> rows = orderRepository.sumRevenueByDay(fromDate, toDate);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            return rows.stream()
                    .map(row -> RevenueByTimeResponse.builder()
                            .time(valueAsString(row[0]))
                            .revenue(valueAsLong(row[1]))
                            .build())
                    .toList();
        } catch (RuntimeException ex) {
            log.error("Failed to query revenue statistics from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot query revenue statistics");
        }
    }

    public List<OrdersByTimeResponse> getOrderStatisticsByDay(Date fromDate, Date toDate) {
        accessControlService.requireOwnerRole();
        try {
            List<Object[]> rows = orderRepository.countOrdersByDay(fromDate, toDate);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            return rows.stream()
                    .map(row -> OrdersByTimeResponse.builder()
                            .time(valueAsString(row[0]))
                            .totalOrders(valueAsLong(row[1]))
                            .build())
                    .toList();
        } catch (RuntimeException ex) {
            log.error("Failed to query order statistics by day from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot query daily order statistics");
        }
    }

    public List<OrdersByTimeResponse> getOrderStatisticsByMonth(Date fromDate, Date toDate) {
        accessControlService.requireOwnerRole();
        try {
            List<Object[]> rows = orderRepository.countOrdersByMonth(fromDate, toDate);
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            return rows.stream()
                    .map(row -> OrdersByTimeResponse.builder()
                            .time(valueAsString(row[0]))
                            .totalOrders(valueAsLong(row[1]))
                            .build())
                    .toList();
        } catch (RuntimeException ex) {
            log.error("Failed to query order statistics by month from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot query monthly order statistics");
        }
    }

    public List<TopSellingProductResponse> getTopSellingProducts(Date fromDate, Date toDate, int topLimit) {
        accessControlService.requireOwnerRole();
        try {
            int safeLimit = topLimit <= 0 ? 5 : topLimit;
            Pageable pageable = PageRequest.of(0, safeLimit);
            return orderRepository.findTopSellingProducts(fromDate, toDate, pageable)
                    .stream()
                    .map(row -> TopSellingProductResponse.builder()
                            .productId(valueAsString(row[0]))
                            .productName(valueAsString(row[1]))
                            .soldQuantity(valueAsLong(row[2]))
                            .build())
                    .toList();
        } catch (RuntimeException ex) {
            log.error("Failed to query top-selling products from {} to {}", fromDate, toDate, ex);
            throw new IllegalStateException("Cannot query top-selling products");
        }
    }

    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long valueAsLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Long nonNullLong(Long value) {
        return value == null ? 0L : value;
    }
}

