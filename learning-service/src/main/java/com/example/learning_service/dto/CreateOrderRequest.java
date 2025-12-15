package com.example.learning_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private Long cartId;
    private List<Long> courseIds;
}

