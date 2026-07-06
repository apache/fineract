package com.example.smsgateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API key filter for securing /otp/ and /sms/ endpoints.
 *
 * <p>Registration is handled exclusively by {@link FilterConfig} via
 * {@code FilterRegistrationBean} with explicit URL patterns. This class
 * is intentionally <b>not</b> annotated with {@code @Component} to avoid
 * double-registration by Spring Boot's auto-scan.</p>
 *
 * <p><b>Fail-closed:</b> If {@code SMS_GATEWAY_API_KEY} is not configured,
 * all requests to protected endpoints are rejected with 503.</p>
 */
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-SMS-Gateway-Api-Key";

    @Value("${sms.gateway.api-key:}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Fail-closed: reject all requests if the API key is not configured
        if (expectedApiKey == null || expectedApiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("SMS gateway API key not configured");
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        if (!expectedApiKey.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
