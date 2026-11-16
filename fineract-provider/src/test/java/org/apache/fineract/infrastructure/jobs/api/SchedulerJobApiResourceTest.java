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
package org.apache.fineract.infrastructure.jobs.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.IdTypeResolver;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.jobs.data.JobExecuteRequest;
import org.apache.fineract.infrastructure.jobs.service.JobRegisterService;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.junit.jupiter.api.Test;

class SchedulerJobApiResourceTest {

    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService = mock(SchedulerJobRunnerReadService.class);
    private final JobRegisterService jobRegisterService = mock(JobRegisterService.class);
    private final ApiRequestParameterHelper apiRequestParameterHelper = mock(ApiRequestParameterHelper.class);
    private final ToApiJsonSerializer<?> toApiJsonSerializer = mock(ToApiJsonSerializer.class);
    private final ToApiJsonSerializer<?> jobHistoryToApiJsonSerializer = mock(ToApiJsonSerializer.class);
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService = mock(
            PortfolioCommandSourceWritePlatformService.class);
    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final SqlValidator sqlValidator = mock(SqlValidator.class);
    private final CommandDispatcher dispatcher = mock(CommandDispatcher.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SchedulerJobApiResource resourceWithBatchManagerEnabled(boolean enabled) {
        FineractProperties fineractProperties = new FineractProperties();
        FineractProperties.FineractModeProperties mode = new FineractProperties.FineractModeProperties();
        mode.setBatchManagerEnabled(enabled);
        fineractProperties.setMode(mode);
        return new SchedulerJobApiResource(schedulerJobRunnerReadService, jobRegisterService, apiRequestParameterHelper,
                (ToApiJsonSerializer) toApiJsonSerializer, (ToApiJsonSerializer) jobHistoryToApiJsonSerializer,
                commandsSourceWritePlatformService, context, fineractProperties, sqlValidator, dispatcher);
    }

    @Test
    void executeJob_returns405_whenBatchManagerDisabled() {
        SchedulerJobApiResource resource = resourceWithBatchManagerEnabled(false);

        Response response = resource.executeJob(1L, "executeJob", JobExecuteRequest.builder().build());

        assertThat(response.getStatus()).isEqualTo(405);
        verifyNoInteractions(dispatcher);
    }

    @Test
    void executeJob_throwsUnrecognizedQueryParamException_whenCommandIsNotExecuteJob() {
        SchedulerJobApiResource resource = resourceWithBatchManagerEnabled(true);

        assertThatThrownBy(() -> resource.executeJob(1L, "notExecuteJob", JobExecuteRequest.builder().build()))
                .isInstanceOf(UnrecognizedQueryParamException.class);
        verifyNoInteractions(dispatcher);
    }

    @Test
    void executeJob_throwsUnrecognizedQueryParamException_whenCommandIsBlank() {
        SchedulerJobApiResource resource = resourceWithBatchManagerEnabled(true);

        assertThatThrownBy(() -> resource.executeJob(1L, "", JobExecuteRequest.builder().build()))
                .isInstanceOf(UnrecognizedQueryParamException.class);
        verifyNoInteractions(dispatcher);
    }

    @Test
    void executeJobByShortName_returns405_whenBatchManagerDisabled() {
        when(schedulerJobRunnerReadService.retrieveId(IdTypeResolver.IdType.SHORT_NAME, "SA_AANF")).thenReturn(1L);
        SchedulerJobApiResource resource = resourceWithBatchManagerEnabled(false);

        Response response = resource.executeJobByShortName("SA_AANF", "executeJob", JobExecuteRequest.builder().build());

        assertThat(response.getStatus()).isEqualTo(405);
        verifyNoInteractions(dispatcher);
    }
}
