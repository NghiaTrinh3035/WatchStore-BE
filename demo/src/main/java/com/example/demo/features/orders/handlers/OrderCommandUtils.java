package com.example.demo.features.orders.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

public final class OrderCommandUtils {

    private OrderCommandUtils() {}

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    public static String normalizeCancelReason(String rawReason) {
        if (!StringUtils.hasText(rawReason)) {
            return "Khác";
        }
        String normalized = rawReason.trim().toUpperCase();
        return switch (normalized) {
            case "WRONG_PRODUCT" -> "Đặt nhầm sản phẩm";
            case "BETTER_PRICE" -> "Tìm thấy giá tốt hơn";
            case "DONT_NEED_ANYMORE" -> "Không cần nữa";
            case "CHANGED_MIND" -> "Thay đổi ý định";
            case "DELIVERY_TOO_LONG" -> "Thời gian giao hàng quá lâu";
            case "OTHER" -> "Khác";
            default -> rawReason.trim();
        };
    }

    public static String normalizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return null;
        }
        return input.trim();
    }
}
