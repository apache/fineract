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
package org.apache.fineract.organisation.office.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.fineract.organisation.office.command.OfficeCreateCommand;
import org.apache.fineract.organisation.office.command.OfficeUpdateCommand;
import org.apache.fineract.organisation.office.data.OfficeCreateRequest;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.organisation.office.data.OfficeUpdateRequest;
import org.apache.fineract.organisation.office.data.OfficeUpdateResponse;
import org.apache.fineract.organisation.office.service.OfficeWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OfficeCommandHandlerTest {

    @Mock
    private OfficeWriteService officeWriteService;

    private CreateOfficeCommandHandler createHandler;
    private UpdateOfficeCommandHandler updateHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateOfficeCommandHandler(officeWriteService);
        updateHandler = new UpdateOfficeCommandHandler(officeWriteService);
    }

    @Test
    void createOfficeCommandHandlerDelegatesToService() {
        OfficeCreateRequest request = OfficeCreateRequest.builder().name("HQ").parentId(1L).openingDate("2023-01-01").build();

        OfficeCreateCommand command = new OfficeCreateCommand();
        command.setPayload(request);

        OfficeCreateResponse expectedResponse = OfficeCreateResponse.builder().officeId(3L).resourceId(3L).build();
        when(officeWriteService.createOffice(request)).thenReturn(expectedResponse);

        OfficeCreateResponse actualResponse = createHandler.handle(command);

        assertNotNull(actualResponse);
        assertEquals(3L, actualResponse.getOfficeId());
        verify(officeWriteService).createOffice(request);
    }

    @Test
    void updateOfficeCommandHandlerDelegatesToService() {
        OfficeUpdateRequest request = OfficeUpdateRequest.builder().id(3L).name("Updated HQ").build();

        OfficeUpdateCommand command = new OfficeUpdateCommand();
        command.setPayload(request);

        OfficeUpdateResponse expectedResponse = OfficeUpdateResponse.builder().officeId(3L).resourceId(3L)
                .changes(Map.of("name", "Updated HQ")).build();
        when(officeWriteService.updateOffice(request)).thenReturn(expectedResponse);

        OfficeUpdateResponse actualResponse = updateHandler.handle(command);

        assertNotNull(actualResponse);
        assertEquals(3L, actualResponse.getOfficeId());
        assertEquals("Updated HQ", actualResponse.getChanges().get("name"));
        verify(officeWriteService).updateOffice(request);
    }
}
