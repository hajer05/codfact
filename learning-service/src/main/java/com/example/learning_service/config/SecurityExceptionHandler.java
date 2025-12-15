package com.example.learning_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        String path = request.getRequestURI();
        log.error("Authentication failed for {} {} - Error: {}", 
                 request.getMethod(), path, authException.getMessage());
        
        // Special logging for metrics endpoint
        if (path.equals("/metrics") || path.startsWith("/actuator")) {
            log.error("SECURITY ISSUE: Metrics endpoint {} blocked by authentication!", path);
        }
        
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        log.error("=== ACCESS DENIED ===");
        log.error("Method: {}, Path: {}", method, path);
        log.error("Remote Addr: {}", remoteAddr);
        log.error("User-Agent: {}", userAgent);
        log.error("Error: {}", accessDeniedException.getMessage());
        log.error("Stack trace:", accessDeniedException);
        
        // Special logging for metrics endpoint
        if (path.equals("/metrics") || path.startsWith("/actuator")) {
            log.error("!!! CRITICAL SECURITY ISSUE !!!");
            log.error("Metrics endpoint {} blocked by access control!", path);
            log.error("This should NOT happen - MetricsSecurityConfig should handle this!");
            log.error("Check if MetricsSecurityConfig is properly loaded with @Order(1)");
        }
        
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
}

