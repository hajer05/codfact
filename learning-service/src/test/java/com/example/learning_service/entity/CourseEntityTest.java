package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseEntityTest {

    @Test
    void equalsAndHashCode_shouldUseIdOnly() {
        Course c1 = new Course();
        c1.setId(100L);
        c1.setTitle("A");

        Course c2 = new Course();
        c2.setId(100L);
        c2.setTitle("B");

        Course c3 = new Course();
        c3.setId(101L);
        c3.setTitle("A");

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());

        assertThat(c1).isNotEqualTo(c3);
    }
}


