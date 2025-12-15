package com.example.learning_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityTest {

    @Test
    void roleEnum_shouldContainExpectedValues() {
        assertThat(Role.RoleName.values())
            .containsExactlyInAnyOrder(Role.RoleName.ADMIN, Role.RoleName.TEACHER, Role.RoleName.ETUDIANT, Role.RoleName.CONSULTANT);
    }

    @Test
    void roleEntity_setAndGet() {
        Role r = new Role();
        r.setId(10L);
        r.setName(Role.RoleName.ADMIN);

        assertThat(r.getId()).isEqualTo(10L);
        assertThat(r.getName()).isEqualTo(Role.RoleName.ADMIN);
    }
}


