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
package org.apache.fineract.cob.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessStepConfigUpdateHandlerTest {

    @InjectMocks
    private BusinessStepConfigUpdateHandler testObj;

    @Mock
    private ConfigJobParameterService configJobParameterService;

    @Captor
    private ArgumentCaptor<String> argumentCaptor;

    @Test
    void shouldProcessCommandGetsJobName() {
        JsonCommand command = mock(JsonCommand.class);
        given(command.getUrl()).willReturn("/jobs/jobName/steps");
        testObj.processCommand(command);

        verify(configJobParameterService).updateStepConfigByJobName(eq(command), argumentCaptor.capture());
        assertEquals("jobName", argumentCaptor.getValue());
    }

    @Test
    void shouldProcessCommandGetsJobNameEmptyString() {
        JsonCommand command = mock(JsonCommand.class);
        given(command.getUrl()).willReturn("/jobs//steps");
        testObj.processCommand(command);

        verify(configJobParameterService).updateStepConfigByJobName(eq(command), argumentCaptor.capture());
        assertEquals("", argumentCaptor.getValue());
    }

    @Test
    void shouldProcessCommandGetsJobNameNull() {
        JsonCommand command = mock(JsonCommand.class);
        given(command.getUrl()).willReturn("/jobs/" + null + "/steps");
        testObj.processCommand(command);

        verify(configJobParameterService).updateStepConfigByJobName(eq(command), argumentCaptor.capture());
        assertEquals(null, argumentCaptor.getValue());
    }
}
