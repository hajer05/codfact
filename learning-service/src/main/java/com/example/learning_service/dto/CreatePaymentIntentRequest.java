package com.example.learning_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentIntentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency = "USD";
}

