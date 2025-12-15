package com.example.learning_service.config;

import com.example.learning_service.entity.Role;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.RoleRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeUsers();
        log.info("Data initialization completed successfully!");
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");
            
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);
            
            Role teacherRole = new Role();
            teacherRole.setName(Role.RoleName.TEACHER);
            roleRepository.save(teacherRole);
            
            Role studentRole = new Role();
            studentRole.setName(Role.RoleName.ETUDIANT);
            roleRepository.save(studentRole);
            
            Role consultantRole = new Role();
            consultantRole.setName(Role.RoleName.CONSULTANT);
            roleRepository.save(consultantRole);
            
            log.info("Roles initialized successfully!");
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing users...");
            
            // Create Admin User
            createUser("admin@codingfactory.com", "password123", "Admin", "User", Role.RoleName.ADMIN);
            
            // Create Teacher User
            createUser("teacher@codingfactory.com", "password123", "John", "Teacher", Role.RoleName.TEACHER);
            
            // Create Consultant User
            createUser("consultant@codingfactory.com", "password123", "Jane", "Consultant", Role.RoleName.CONSULTANT);
            
            // Create Student User
            createUser("student@codingfactory.com", "password123", "Alice", "Student", Role.RoleName.ETUDIANT);
            
            log.info("Users initialized successfully!");
        }
    }

    private void createUser(String email, String password, String firstName, String lastName, Role.RoleName roleName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
        log.info("Created user: {} with role: {}", email, roleName);
    }
}
