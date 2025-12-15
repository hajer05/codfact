package com.example.learning_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Still process metrics but log errors
        boolean isMetricsEndpoint = path.equals("/metrics") || path.startsWith("/actuator");
        
        if (isMetricsEndpoint) {
            log.info("LoggingFilter: Processing metrics request - {} {}", method, path);
        }
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            log.error("LoggingFilter: Exception during filter chain for {} {}", method, path, e);
            throw e;
        } finally {
            int status = wrappedResponse.getStatus();
            
            // Log errors and metrics endpoint requests
            if (status >= 400) {
                log.error("=== HTTP ERROR ===");
                log.error("Method: {}, Path: {}, Status: {}", method, path, status);
                if (isMetricsEndpoint) {
                    log.error("!!! METRICS ENDPOINT ERROR !!!");
                    log.error("{} returned status {} - This should NOT happen!", path, status);
                    log.error("Check WebSecurityCustomizer (ignoring /actuator/**) and MetricsSecurityConfig!");
                }
            } else if (status >= 300) {
                log.warn("HTTP {} {} - Status: {} - Redirect", method, path, status);
            } else if (isMetricsEndpoint && status >= 200 && status < 300) {
                log.info("✓ Metrics endpoint {} accessed successfully - Status: {}", path, status);
            } else if (status >= 200 && status < 300) {
                log.debug("HTTP {} {} - Status: {} - Success", method, path, status);
            }
            
            wrappedResponse.copyBodyToResponse();
        }
    }
}

