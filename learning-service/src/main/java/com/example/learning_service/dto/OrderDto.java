package com.example.learning_service.dto;

import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private String userEmail;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private PaymentDto payment;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @Data
    public static class OrderItemDto {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private BigDecimal price;
        private String thumbnailImage;
    }

    @Data
    public static class PaymentDto {
        private Long id;
        private BigDecimal amount;
        private Payment.PaymentMethod method;
        private Payment.PaymentStatus status;
        private String stripePaymentIntentId;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }
}

