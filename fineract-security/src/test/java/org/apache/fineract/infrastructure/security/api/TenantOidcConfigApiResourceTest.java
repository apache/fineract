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
package org.apache.fineract.infrastructure.security.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.TenantOidcConfigData;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.apache.fineract.infrastructure.security.event.TenantOidcConfigChangedEvent;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TenantOidcConfigApiResourceTest {

    private static final String OWN_TENANT = "tenant_a";
    private static final String OTHER_TENANT = "tenant_b";
    private static final String OLD_ISSUER = "https://idp.example.com/realms/old";
    private static final String NEW_ISSUER = "https://idp.example.com/realms/new";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private TenantOidcConfigService service;
    @Mock
    private ToApiJsonSerializer<TenantOidcConfigData> serializer;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AppUser user;

    private TenantOidcConfigApiResource resource;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, OWN_TENANT, "Tenant A", "UTC", null));
        when(context.authenticatedUser()).thenReturn(user);
        resource = new TenantOidcConfigApiResource(context, service, serializer, eventPublisher);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private TenantOidcConfig config(String tenantId, String issuer) {
        return TenantOidcConfig.builder().id(1L).tenantId(tenantId).providerType(OidcFederationType.GENERIC).issuerUri(issuer)
                .clientId("client").usernameClaim("preferred_username").scopes("openid").enabled(true).build();
    }

    @Test
    void retrieveForOtherTenantIsNotFound() {
        assertThatThrownBy(() -> resource.retrieve(OTHER_TENANT)).isInstanceOf(ResourceNotFoundException.class);
        verify(service, never()).findByTenantId(any());
    }

    @Test
    void createForOtherTenantIsNotFound() {
        assertThatThrownBy(() -> resource.create(OTHER_TENANT, "{\"issuerUri\":\"" + NEW_ISSUER + "\"}"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(service, never()).save(any());
    }

    @Test
    void updateForOtherTenantIsNotFound() {
        assertThatThrownBy(() -> resource.update(OTHER_TENANT, "{}")).isInstanceOf(ResourceNotFoundException.class);
        verify(service, never()).save(any());
    }

    @Test
    void deleteForOtherTenantIsNotFound() {
        assertThatThrownBy(() -> resource.delete(OTHER_TENANT)).isInstanceOf(ResourceNotFoundException.class);
        verify(service, never()).deleteByTenantId(any());
    }

    @Test
    void retrieveForOwnTenantSucceeds() {
        when(service.findByTenantId(OWN_TENANT)).thenReturn(Optional.of(config(OWN_TENANT, OLD_ISSUER)));
        when(serializer.serialize(any(TenantOidcConfigData.class))).thenReturn("json");

        assertThat(resource.retrieve(OWN_TENANT)).isEqualTo("json");
    }

    @Test
    void updateEvictsOldAndNewIssuer() {
        when(service.findByTenantId(OWN_TENANT)).thenReturn(Optional.of(config(OWN_TENANT, OLD_ISSUER)));
        when(service.save(any(TenantOidcConfig.class))).thenAnswer(inv -> inv.getArgument(0));
        when(serializer.serialize(any(TenantOidcConfigData.class))).thenReturn("json");

        resource.update(OWN_TENANT, "{\"issuerUri\":\"" + NEW_ISSUER + "\"}");

        verify(service).evictIssuer(OLD_ISSUER);
        verify(service).evictIssuer(NEW_ISSUER);
        verify(eventPublisher).publishEvent(new TenantOidcConfigChangedEvent(OLD_ISSUER, NEW_ISSUER));
    }

    @Test
    void deleteEvictsOldIssuer() {
        when(service.findByTenantId(OWN_TENANT)).thenReturn(Optional.of(config(OWN_TENANT, OLD_ISSUER)));

        resource.delete(OWN_TENANT);

        verify(service).deleteByTenantId(OWN_TENANT);
        verify(service).evictIssuer(OLD_ISSUER);
        verify(eventPublisher).publishEvent(new TenantOidcConfigChangedEvent(OLD_ISSUER, null));
    }
}
