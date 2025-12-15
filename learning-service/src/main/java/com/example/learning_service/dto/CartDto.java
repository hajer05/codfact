package com.example.learning_service.dto;

import com.example.learning_service.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CartDto {
    private Long id;
    private Long userId;
    private Set<CartItemDto> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class CartItemDto {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private BigDecimal coursePrice;
        private BigDecimal price;
        private String thumbnailImage;
        private LocalDateTime addedAt;
    }
}

