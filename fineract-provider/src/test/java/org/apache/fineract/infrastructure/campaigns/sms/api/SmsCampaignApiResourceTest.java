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
package org.apache.fineract.infrastructure.campaigns.sms.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.campaigns.sms.data.dto.SmsCampaignPreviewDto;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignReadPlatformService;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmsCampaignApiResourceTest {

    @Mock
    private PlatformSecurityContext platformSecurityContext;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Mock
    private SmsCampaignReadPlatformService smsCampaignReadPlatformService;

    @Mock
    private FromJsonHelper fromJsonHelper;

    @Mock
    private SmsCampaignWritePlatformService smsCampaignWritePlatformService;

    @Mock
    private DefaultToApiJsonSerializer<String> toApiJsonSerializer;

    @Mock
    private AppUser appUser;

    private SmsCampaignApiResource resource;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(appUser);
        resource = new SmsCampaignApiResource(platformSecurityContext, commandsSourceWritePlatformService, smsCampaignReadPlatformService,
                fromJsonHelper, smsCampaignWritePlatformService, context, toApiJsonSerializer);
    }

    @Test
    void previewChecksSmsCampaignReadPermission() {
        when(toApiJsonSerializer.serialize(any())).thenReturn("{}");
        when(fromJsonHelper.parse("{}")).thenReturn(JsonParser.parseString("{}"));

        resource.preview(new SmsCampaignPreviewDto(null, null));

        verify(appUser).validateHasReadPermission("SMSCAMPAIGN");
    }
}
