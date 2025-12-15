package com.example.learning_service.dto;

import lombok.Data;

@Data
public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private Long orderId;
}

