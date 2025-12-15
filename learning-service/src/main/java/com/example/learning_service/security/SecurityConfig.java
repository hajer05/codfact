package com.example.learning_service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.learning_service.config.SecurityExceptionHandler;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Completely bypass Spring Security for actuator endpoints
        return web -> web.ignoring().requestMatchers("/actuator/**");
    }

    @Bean
    @org.springframework.core.annotation.Order(2) // Lower priority than MetricsSecurityConfig
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CRITICAL: Exclude metrics and actuator from this security chain
        // They are handled by MetricsSecurityConfig with higher priority
        http
            .securityMatcher(request -> {
                String path = request.getRequestURI();
                // This SecurityFilterChain should NOT handle /metrics or /actuator
                boolean exclude = path.equals("/metrics") || path.startsWith("/actuator/");
                if (exclude) {
                    log.debug("Excluding {} from main SecurityConfig (handled by MetricsSecurityConfig)", path);
                }
                return !exclude;
            })
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public API endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/lesson-progress/**").permitAll()
                .requestMatchers("/api/certificates/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/courses/**").permitAll()
                .requestMatchers("/api/modules/**").permitAll()
                .requestMatchers("/api/lessons/**").permitAll()
                .requestMatchers("/api/enrollments/**").permitAll()
                .requestMatchers("/api/email/**").permitAll()
                .requestMatchers("/api/ws/**").permitAll()
                .requestMatchers("/api/quizzes/**").permitAll()  // Temporary: Allow all quiz endpoints
                .requestMatchers("/error").permitAll()  // Allow error endpoint
                // Authenticated endpoints
                .requestMatchers("/api/notifications/**").authenticated()
                // Role-based endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/api/consultant/**").hasAnyRole("CONSULTANT", "ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler)
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Log security configuration
        log.info("Main SecurityConfig: Excluding /metrics and /actuator/** (handled by MetricsSecurityConfig)");

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",  // Backoffice Angular app
            "http://localhost:4201"   // Frontoffice Angular app
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
