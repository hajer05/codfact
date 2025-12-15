package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDefaultsTest {

    @Test
    void newUser_shouldHaveSecurityFlagsTrueAndEnabled() {
        User u = new User();

        assertThat(u.isAccountNonExpired()).isTrue();
        assertThat(u.isAccountNonLocked()).isTrue();
        assertThat(u.isCredentialsNonExpired()).isTrue();
        assertThat(u.isEnabled()).isTrue();
        assertThat(u.getCreatedAt()).isNotNull();
    }

    @Test
    void getUsername_shouldReturnEmail() {
        User u = new User();
        u.setEmail("user@example.com");

        assertThat(u.getUsername()).isEqualTo("user@example.com");
    }
}


