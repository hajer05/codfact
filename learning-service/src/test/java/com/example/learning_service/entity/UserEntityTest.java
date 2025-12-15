package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void getAuthorities_shouldMapRolesToGrantedAuthorities() {
        User user = new User();
        user.setRoles(Set.of(
            new Role(1L, Role.RoleName.ADMIN),
            new Role(2L, Role.RoleName.TEACHER)
        ));

        var authorities = user.getAuthorities();

        assertThat(authorities)
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_TEACHER");
    }
}


