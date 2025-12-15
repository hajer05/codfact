package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityTest {

    @Test
    void defaults_shouldBePendingWithCreatedAtSet() {
        Order o = new Order();
        assertThat(o.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(o.getCreatedAt()).isNotNull();
    }

    @Test
    void preUpdate_shouldSetCompletedAtWhenStatusCompleted() {
        Order o = new Order();
        o.setId(5L);
        o.setTotalAmount(new BigDecimal("10.00"));
        assertThat(o.getCompletedAt()).isNull();

        o.setStatus(Order.OrderStatus.COMPLETED);
        o.preUpdate();

        assertThat(o.getCompletedAt()).isNotNull();
    }

    @Test
    void equalsAndHashCode_shouldUseIdOnly() {
        Order o1 = new Order();
        o1.setId(1L);
        Order o2 = new Order();
        o2.setId(1L);
        Order o3 = new Order();
        o3.setId(2L);

        assertThat(o1).isEqualTo(o2);
        assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
        assertThat(o1).isNotEqualTo(o3);
    }
}


