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
package org.apache.fineract.infrastructure.entityaccess.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriInfo;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityRelationData;
import org.apache.fineract.infrastructure.entityaccess.data.FineractEntityToEntityMappingData;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FineractEntityApiResourceTest {

    private static final String READ_PERMISSION_RESOURCE = "ENTITYMAPPING";
    private static final Long MAP_ID = 1L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private FineractEntityAccessReadService readPlatformService;
    @Mock
    private DefaultToApiJsonSerializer<FineractEntityRelationData> toApiJsonSerializer;
    @Mock
    private DefaultToApiJsonSerializer<FineractEntityToEntityMappingData> toApiJsonSerializerOfficeToLoanProducts;
    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private AppUser appUser;
    @Mock
    private UriInfo uriInfo;

    private FineractEntityApiResource underTest;

    @BeforeEach
    void setUp() {
        when(context.authenticatedUser()).thenReturn(appUser);
        underTest = new FineractEntityApiResource(context, readPlatformService, toApiJsonSerializer, toApiJsonSerializerOfficeToLoanProducts,
                apiRequestParameterHelper, commandsSourceWritePlatformService);
    }

    @Test
    void retrieveAllChecksEntityMappingReadPermission() {
        underTest.retrieveAll(uriInfo);

        verify(appUser).validateHasReadPermission(READ_PERMISSION_RESOURCE);
    }

    @Test
    void retrieveOneChecksEntityMappingReadPermission() {
        underTest.retrieveOne(MAP_ID, uriInfo);

        verify(appUser).validateHasReadPermission(READ_PERMISSION_RESOURCE);
    }

    @Test
    void getEntityToEntityMappingsChecksEntityMappingReadPermission() {
        underTest.getEntityToEntityMappings(MAP_ID, 2L, 3L, uriInfo);

        verify(appUser).validateHasReadPermission(READ_PERMISSION_RESOURCE);
    }
}
