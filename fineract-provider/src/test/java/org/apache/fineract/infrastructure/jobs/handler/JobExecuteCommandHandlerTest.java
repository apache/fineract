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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.apache.fineract.infrastructure.jobs.command.JobExecuteCommand;
import org.apache.fineract.infrastructure.jobs.data.JobExecuteRequest;
import org.apache.fineract.infrastructure.jobs.data.JobExecuteResponse;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobExecuteCommandHandlerTest {

    @Mock
    private JobRegisterService jobRegisterService;

    @InjectMocks
    private JobExecuteCommandHandler underTest;

    @Test
    void handle_withNullParameters_callsServiceWithEmptySetAndReturnsJobId() {
        Long jobId = 42L;
        JobExecuteRequest request = JobExecuteRequest.builder().jobId(jobId).build();
        JobExecuteCommand command = new JobExecuteCommand();
        command.setPayload(request);

        JobExecuteResponse response = underTest.handle(command);

        verify(jobRegisterService).executeJobWithParameters(eq(jobId), eq(Set.of()));
        assertThat(response.getResourceId()).isEqualTo(jobId);
    }

    @Test
    void handle_withParameters_callsServiceWithParameterSet() {
        Long jobId = 42L;
        JobExecuteRequest request = JobExecuteRequest.builder().jobId(jobId).jobParameters(java.util.List.of()).build();
        JobExecuteCommand command = new JobExecuteCommand();
        command.setPayload(request);

        JobExecuteResponse response = underTest.handle(command);

        verify(jobRegisterService).executeJobWithParameters(eq(jobId), anySet());
        assertThat(response.getResourceId()).isEqualTo(jobId);
    }
}
