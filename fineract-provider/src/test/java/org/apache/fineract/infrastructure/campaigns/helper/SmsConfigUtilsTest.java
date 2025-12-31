/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.campaigns.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import org.apache.fineract.infrastructure.campaigns.sms.data.MessageGatewayConfigurationData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SmsConfigUtilsTest {

    @Mock
    private ExternalServicesPropertiesReadPlatformService propertiesReadPlatformService;

    @InjectMocks
    private SmsConfigUtils smsConfigUtils;

    private MockedStatic<ThreadLocalContextUtil> threadLocalContextUtilMock;

    @BeforeEach
    public void setUp() {
        threadLocalContextUtilMock = Mockito.mockStatic(ThreadLocalContextUtil.class);
        FineractPlatformTenant tenant = new FineractPlatformTenant(1L, "default", "Default", "UTC", null);
        threadLocalContextUtilMock.when(ThreadLocalContextUtil::getTenant).thenReturn(tenant);
    }

    @AfterEach
    public void tearDown() {
        threadLocalContextUtilMock.close();
    }

    @Test
    public void testGetMessageGateWayRequestURI_WithSslEnabled_ShouldUseHttps() {
        // Given
        MessageGatewayConfigurationData configData = new MessageGatewayConfigurationData(1L, "TestConnection", "sms.example.com", 443,
                "/api", "user", "pass", true, "appKey123");
        when(propertiesReadPlatformService.getSMSGateway()).thenReturn(configData);

        // When
        Map<String, Object> result = smsConfigUtils.getMessageGateWayRequestURI("sendSms", null);

        // Then
        assertNotNull(result);
        URI uri = (URI) result.get("uri");
        assertNotNull(uri);
        assertEquals("https", uri.getScheme(), "Expected HTTPS scheme when SSL is enabled");
        assertEquals("sms.example.com", uri.getHost());
        assertEquals(443, uri.getPort());
    }

    @Test
    public void testGetMessageGateWayRequestURI_WithSslDisabled_ShouldUseHttp() {
        // Given
        MessageGatewayConfigurationData configData = new MessageGatewayConfigurationData(1L, "TestConnection", "sms.example.com", 80,
                "/api", "user", "pass", false, "appKey123");
        when(propertiesReadPlatformService.getSMSGateway()).thenReturn(configData);

        // When
        Map<String, Object> result = smsConfigUtils.getMessageGateWayRequestURI("sendSms", null);

        // Then
        assertNotNull(result);
        URI uri = (URI) result.get("uri");
        assertNotNull(uri);
        assertEquals("http", uri.getScheme(), "Expected HTTP scheme when SSL is disabled");
        assertEquals("sms.example.com", uri.getHost());
        assertEquals(80, uri.getPort());
    }

    @Test
    public void testGetMessageGateWayRequestURI_WithNullEndpoint_ShouldUseHttps() {
        // Given
        MessageGatewayConfigurationData configData = new MessageGatewayConfigurationData(1L, "TestConnection", "sms.example.com", 443,
                null, "user", "pass", true, "appKey123");
        when(propertiesReadPlatformService.getSMSGateway()).thenReturn(configData);

        // When
        Map<String, Object> result = smsConfigUtils.getMessageGateWayRequestURI("sendSms", null);

        // Then
        assertNotNull(result);
        URI uri = (URI) result.get("uri");
        assertNotNull(uri);
        assertEquals("https", uri.getScheme(), "Expected HTTPS scheme when SSL is enabled");
    }

    @Test
    public void testGetMessageGateWayRequestURI_WithRootEndpoint_ShouldUseHttp() {
        // Given
        MessageGatewayConfigurationData configData = new MessageGatewayConfigurationData(1L, "TestConnection", "sms.example.com", 80, "/",
                "user", "pass", false, "appKey123");
        when(propertiesReadPlatformService.getSMSGateway()).thenReturn(configData);

        // When
        Map<String, Object> result = smsConfigUtils.getMessageGateWayRequestURI("sendSms", null);

        // Then
        assertNotNull(result);
        URI uri = (URI) result.get("uri");
        assertNotNull(uri);
        assertEquals("http", uri.getScheme(), "Expected HTTP scheme when SSL is disabled");
    }
}
