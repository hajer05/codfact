package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseDefaultsTest {

    @Test
    void newCourse_shouldHaveDefaults() {
        Course c = new Course();

        // Defaults from entity
        assertThat(c.getStatus()).isEqualTo(Course.CourseStatus.DRAFT);
        assertThat(c.getLanguage()).isEqualTo("French");
        // createdAt is set at construction time
        assertThat(c.getCreatedAt()).isNotNull();
    }
}


