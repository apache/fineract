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
package org.apache.fineract.accounting.closure.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.accounting.closure.command.GLClosureCreateCommand;
import org.apache.fineract.accounting.closure.data.GLClosureCreateRequest;
import org.apache.fineract.accounting.closure.data.GLClosureCreateResponse;
import org.apache.fineract.accounting.closure.service.GLClosureWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GLClosureCreateCommandHandlerTest {

    @Mock
    private GLClosureWritePlatformService writePlatformService;

    @InjectMocks
    private GLClosureCreateCommandHandler underTest;

    @Test
    void testHandleSuccess() {
        final GLClosureCreateRequest request = GLClosureCreateRequest.builder().officeId(1L).closingDate("2022-12-01")
                .comments("Test closure").build();

        final GLClosureCreateCommand command = new GLClosureCreateCommand();
        command.setPayload(request);

        final GLClosureCreateResponse expectedResponse = GLClosureCreateResponse.builder().officeId(1L).resourceId(10L).build();

        when(writePlatformService.createGLClosure(request)).thenReturn(expectedResponse);

        final GLClosureCreateResponse actualResponse = underTest.handle(command);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(writePlatformService).createGLClosure(request);
    }
}
