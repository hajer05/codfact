package com.example.learning_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
public class MetricsSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain metricsSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );

        log.info("MetricsSecurityConfig: /actuator/** is public (Prometheus enabled at /actuator/prometheus)");
        return http.build();
    }
}


