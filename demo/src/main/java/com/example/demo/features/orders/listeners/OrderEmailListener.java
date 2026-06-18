package com.example.demo.features.orders.listeners;

import com.example.demo.features.orders.entities.Order;
import com.example.demo.features.orders.events.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEmailListener {

    private static final String REFUND_PROCESSING_MESSAGE = "Hoàn tiền sẽ được xử lý trong 3-7 ngày làm việc.";

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String mailFromAddress;

    @Async
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        Order order = event.getOrder();
        String reason = event.getReason();
        String note = event.getNote();
        boolean paidOrder = event.isPaidOrder();
        boolean restockIssue = event.isRestockIssue();

        String email = order.getCustomer() != null ? order.getCustomer().getEmail() : null;
        if (!StringUtils.hasText(email)) {
            return;
        }

        String customerName = StringUtils.hasText(order.getCustomer().getFullName())
                ? order.getCustomer().getFullName()
                : order.getCustomer().getUsername();

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(mailFromAddress)) {
            message.setFrom(mailFromAddress.trim());
        }
        message.setTo(email.trim());
        message.setSubject("Xác nhận hủy đơn hàng #" + order.getId());

        StringBuilder body = new StringBuilder();
        body.append("Xin chào ").append(customerName).append(",\n\n")
                .append("Đơn hàng #").append(order.getId()).append(" đã được hủy thành công.\n")
                .append("Lý do: ").append(reason).append(".\n");

        if (StringUtils.hasText(note)) {
            body.append("Ghi chú: ").append(note).append("\n");
        }
        if (paidOrder) {
            body.append(REFUND_PROCESSING_MESSAGE).append("\n");
        }
        if (restockIssue) {
            body.append("Lưu ý: hệ thống đang xử lý sự cố hoàn kho, nhân viên sẽ kiểm tra thủ công.\n");
        }

        body.append("\nTrân trọng,\nChronolux Team");
        message.setText(body.toString());

        try {
            mailSender.send(message);
            log.info("Cancellation email sent to {} for order {}", email, order.getId());
        } catch (MailException ex) {
            log.warn("Failed to send cancellation email to {} for order {}", email, order.getId(), ex);
        } catch (Exception ex) {
            log.warn("Unexpected error while sending cancellation email to {} for order {}", email, order.getId(), ex);
        }
    }
}
