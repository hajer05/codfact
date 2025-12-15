package com.example.learning_service.dto;

import lombok.Data;

@Data
public class ConfirmPaymentRequest {
    private String paymentIntentId;
    private Long orderId;
}

