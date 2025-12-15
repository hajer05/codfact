package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollmentEntityTest {

    @Test
    void defaults_shouldBeActiveWithZeroProgress() {
        Enrollment e = new Enrollment();
        assertThat(e.getStatus()).isEqualTo(Enrollment.EnrollmentStatus.ACTIVE);
        assertThat(e.getProgress()).isEqualTo(0.0);
        assertThat(e.getEnrolledAt()).isNotNull();
    }

    @Test
    void equalsAndHashCode_shouldUseIdOnly() {
        Enrollment e1 = new Enrollment();
        e1.setId(1L);
        Enrollment e2 = new Enrollment();
        e2.setId(1L);
        Enrollment e3 = new Enrollment();
        e3.setId(2L);

        assertThat(e1).isEqualTo(e2);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        assertThat(e1).isNotEqualTo(e3);
    }
}


