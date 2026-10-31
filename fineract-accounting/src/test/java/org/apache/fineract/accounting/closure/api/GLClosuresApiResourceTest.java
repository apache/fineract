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
package org.apache.fineract.accounting.closure.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import org.apache.fineract.accounting.closure.command.GLClosureCreateCommand;
import org.apache.fineract.accounting.closure.command.GLClosureDeleteCommand;
import org.apache.fineract.accounting.closure.command.GLClosureUpdateCommand;
import org.apache.fineract.accounting.closure.data.GLClosureCreateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureCreateResponse;
import org.apache.fineract.accounting.closure.data.GLClosureData;
import org.apache.fineract.accounting.closure.data.GLClosureDeleteResponse;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureUpdateResponse;
import org.apache.fineract.accounting.closure.service.GLClosureReadPlatformService;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GLClosuresApiResourceTest {

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private GLClosureReadPlatformService glClosureReadPlatformService;

    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;

    @Mock
    private OfficeReadPlatformService officeReadPlatformService;

    @Mock
    private CommandDispatcher dispatcher;

    @Mock
    private AppUser appUser;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private GLClosuresApiResource underTest;

    private static final Long OFFICE_ID = 1L;
    private static final Long CLOSURE_ID = 10L;

    @Test
    void testRetrieveAllClosures() {
        when(context.authenticatedUser()).thenReturn(appUser);
        final GLClosureData closureData = org.mockito.Mockito.mock(GLClosureData.class);
        final List<GLClosureData> expectedList = List.of(closureData);
        when(glClosureReadPlatformService.retrieveAllGLClosures(OFFICE_ID)).thenReturn(expectedList);

        final List<GLClosureData> result = underTest.retrieveAllClosures(OFFICE_ID);

        assertNotNull(result);
        assertEquals(expectedList, result);
        verify(appUser).validateHasReadPermission("GLCLOSURE");
        verify(glClosureReadPlatformService).retrieveAllGLClosures(OFFICE_ID);
    }

    @Test
    void testRetrieveClosure() {
        when(context.authenticatedUser()).thenReturn(appUser);
        final GLClosureData expectedData = org.mockito.Mockito.mock(GLClosureData.class);
        final ApiRequestJsonSerializationSettings settings = org.mockito.Mockito.mock(ApiRequestJsonSerializationSettings.class);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(apiRequestParameterHelper.process(any())).thenReturn(settings);
        when(settings.isTemplate()).thenReturn(false);
        when(glClosureReadPlatformService.retrieveGLClosureById(CLOSURE_ID)).thenReturn(expectedData);

        final GLClosureData result = underTest.retreiveClosure(CLOSURE_ID, uriInfo);

        assertNotNull(result);
        assertEquals(expectedData, result);
        verify(appUser).validateHasReadPermission("GLCLOSURE");
        verify(glClosureReadPlatformService).retrieveGLClosureById(CLOSURE_ID);
    }

    @Test
    void testCreateGLClosure() {
        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(OFFICE_ID).closingDate("2022-12-01")
                .comments("Test create").build();

        final GLClosureCreateResponse expectedResponse = GLClosureCreateResponse.builder().officeId(OFFICE_ID).resourceId(CLOSURE_ID)
                .build();

        when(dispatcher.dispatch(any(GLClosureCreateCommand.class))).thenReturn(() -> expectedResponse);

        final GLClosureCreateResponse actualResponse = underTest.createGLClosure(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        final ArgumentCaptor<GLClosureCreateCommand> captor = ArgumentCaptor.forClass(GLClosureCreateCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertEquals(request, captor.getValue().getPayload());
    }

    @Test
    void testUpdateGLClosure() {
        final GLClosureUpdateRequest request = GLClosureUpdateRequest.builder().comments("Updated comments").build();

        final GLClosureUpdateResponse expectedResponse = GLClosureUpdateResponse.builder().officeId(OFFICE_ID).resourceId(CLOSURE_ID)
                .comments("Updated comments").build();

        when(dispatcher.dispatch(any(GLClosureUpdateCommand.class))).thenReturn(() -> expectedResponse);

        final GLClosureUpdateResponse actualResponse = underTest.updateGLClosure(CLOSURE_ID, request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(CLOSURE_ID, request.getId());

        final ArgumentCaptor<GLClosureUpdateCommand> captor = ArgumentCaptor.forClass(GLClosureUpdateCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertEquals(request, captor.getValue().getPayload());
    }

    @Test
    void testDeleteGLClosure() {
        final GLClosureDeleteResponse expectedResponse = GLClosureDeleteResponse.builder().officeId(OFFICE_ID).resourceId(CLOSURE_ID)
                .build();

        when(dispatcher.dispatch(any(GLClosureDeleteCommand.class))).thenReturn(() -> expectedResponse);

        final GLClosureDeleteResponse actualResponse = underTest.deleteGLClosure(CLOSURE_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        final ArgumentCaptor<GLClosureDeleteCommand> captor = ArgumentCaptor.forClass(GLClosureDeleteCommand.class);
        verify(dispatcher).dispatch(captor.capture());
        assertEquals(CLOSURE_ID, captor.getValue().getPayload().getId());
    }
}
