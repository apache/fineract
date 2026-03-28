package org.apache.fineract.infrastructure.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class IpAddressUtilsTest {

    @AfterEach
    void clearContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    // Helper method for setting request context
    private void withRequest(HttpServletRequest request, Runnable testLogic) {
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        try {
            RequestContextHolder.setRequestAttributes(attributes);
            testLogic.run();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void getClientIpReturnsEmptyWhenNoRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        String result = IpAddressUtils.getClientIp();

        assertEquals("", result);
    }

    @Test
    void getClientIpReturnsEmptyWhenIpAttributeMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn(null);

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("", result);
        });
    }

    @Test
    void getClientIpReturnsIpWhenAttributePresent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn("192.168.1.1");

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("192.168.1.1", result);
        });
    }

    @Test
    void getClientIpConvertsNonStringAttributeUsingToString() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn(12345);

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("12345", result);
        });
    }
}