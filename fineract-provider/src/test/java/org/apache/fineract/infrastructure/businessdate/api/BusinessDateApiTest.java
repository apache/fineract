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
package org.apache.fineract.infrastructure.businessdate.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.ServletException;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessDateApiTest {

    @Mock
    private ApiRequestParameterHelper parameterHelper;

    @Mock
    private BusinessDateReadPlatformService readPlatformService;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private PlatformSecurityContext securityContext;

    @Mock
    private DefaultToApiJsonSerializer<BusinessDateData> jsonSerializer;

    @Mock
    private PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @InjectMocks
    private BusinessDateApiResource underTest;

    private ApiRequestJsonSerializationSettings apiRequestJsonSerializationSettings;

    @BeforeEach
    void setUp() throws IOException {
        apiRequestJsonSerializationSettings = new ApiRequestJsonSerializationSettings(false, null, false, false, false);
        given(parameterHelper.process(Mockito.any())).willReturn(apiRequestJsonSerializationSettings);
    }

    @Test
    void getBusinessDatesAPIHasPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        List<BusinessDateData> response = Mockito.mock(List.class);
        given(readPlatformService.findAll()).willReturn(response);
        // given
        Mockito.doNothing().when(appUser).validateHasReadPermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        // when
        underTest.getBusinessDates(uriInfo);
        // then
        verify(readPlatformService, Mockito.times(1)).findAll();
        verify(jsonSerializer, Mockito.times(1)).serialize(apiRequestJsonSerializationSettings, response);
    }

    @Test
    void getBusinessDatesAPIHasNoPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        // given
        Mockito.doThrow(NoAuthorizationException.class).when(appUser).validateHasReadPermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        // when
        assertThatThrownBy(() -> underTest.getBusinessDates(uriInfo)).isInstanceOf(NoAuthorizationException.class);
        // then
        verifyNoInteractions(readPlatformService);
    }

    @Test
    void getBusinessDateByTypeAPIHasPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        BusinessDateData response = Mockito.mock(BusinessDateData.class);
        given(readPlatformService.findByType("type")).willReturn(response);
        // given
        Mockito.doNothing().when(appUser).validateHasReadPermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        // when
        underTest.getBusinessDate("type", uriInfo);
        // then
        verify(readPlatformService, Mockito.times(1)).findByType("type");
        verify(jsonSerializer, Mockito.times(1)).serialize(apiRequestJsonSerializationSettings, response);
    }

    @Test
    void getBusinessDateByTypeAPIHasNoPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        // given
        Mockito.doThrow(NoAuthorizationException.class).when(appUser).validateHasReadPermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        // when
        assertThatThrownBy(() -> underTest.getBusinessDate("type", uriInfo)).isInstanceOf(NoAuthorizationException.class);
        // then
        verifyNoInteractions(readPlatformService);
    }

    @Test
    void postBusinessDateAPIHasPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        CommandProcessingResult response = Mockito.mock(CommandProcessingResult.class);
        // given
        Mockito.doNothing().when(appUser).validateHasUpdatePermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        given(commandWritePlatformService.logCommandSource(Mockito.any())).willReturn(response);
        // when
        underTest.updateBusinessDate("{}", uriInfo);
        // then
        verify(commandWritePlatformService, Mockito.times(1)).logCommandSource(Mockito.any());
        verify(jsonSerializer, Mockito.times(1)).serialize(response);
    }

    @Test
    void postBusinessDateAPIHasNoPermission() throws ServletException, IOException {
        AppUser appUser = Mockito.mock(AppUser.class);
        // given
        Mockito.doThrow(NoAuthorizationException.class).when(appUser).validateHasUpdatePermission("BUSINESS_DATE");
        given(securityContext.authenticatedUser()).willReturn(appUser);
        // when
        assertThatThrownBy(() -> underTest.updateBusinessDate("{}", uriInfo)).isInstanceOf(NoAuthorizationException.class);
        // then
        verifyNoInteractions(readPlatformService);
    }
}
