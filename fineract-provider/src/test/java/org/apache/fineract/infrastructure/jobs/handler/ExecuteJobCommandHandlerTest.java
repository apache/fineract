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
package org.apache.fineract.infrastructure.jobs.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecuteJobCommandHandlerTest {

    @Mock
    private JobRegisterService jobRegisterService;

    @Mock
    private JsonCommand command;

    @InjectMocks
    private ExecuteJobCommandHandler underTest;

    @Test
    void shouldExecuteJobAndReturnCommandResult() {
        // given
        Long jobId = 123L;
        Long commandId = 456L;
        String json = "{\"includeTasks\":true}";
        when(command.entityId()).thenReturn(jobId);
        when(command.commandId()).thenReturn(commandId);
        when(command.json()).thenReturn(json);

        // when
        CommandProcessingResult result = underTest.processCommand(command);

        // then
        verify(jobRegisterService).executeJobWithParameters(jobId, json);
        assertEquals(commandId, result.getCommandId());
        assertEquals(jobId, result.getResourceId());
    }
}
