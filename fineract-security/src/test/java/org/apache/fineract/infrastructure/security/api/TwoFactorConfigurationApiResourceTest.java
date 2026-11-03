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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.TwoFactorConfigurationService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TwoFactorConfigurationApiResourceTest {

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private TwoFactorConfigurationService configurationService;

    @Mock
    private DefaultToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;

    @Mock
    private AppUser appUser;

    @InjectMocks
    private TwoFactorConfigurationApiResource resource;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(appUser);
    }

    @Test
    void retrieveAllChecksTwoFactorConfigurationReadPermission() {
        Map<String, Object> configuration = Map.of();
        when(configurationService.retrieveAll()).thenReturn(configuration);
        when(toApiJsonSerializer.serialize(configuration)).thenReturn("{}");

        resource.retrieveAll();

        verify(appUser).validateHasReadPermission("TWOFACTOR_CONFIGURATION");
    }
}
