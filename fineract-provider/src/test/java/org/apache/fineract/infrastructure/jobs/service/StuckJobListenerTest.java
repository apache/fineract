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
package org.apache.fineract.infrastructure.jobs.service;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.JdbcTemplateFactory;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class StuckJobListenerTest {

    @Test
    void failureOfOneTenantDoesNotStopRecoveryOfTheNextTenant() {
        final FineractPlatformTenant failing = new FineractPlatformTenant(1L, "failing", "Failing", "UTC", null);
        final FineractPlatformTenant healthy = new FineractPlatformTenant(2L, "healthy", "Healthy", "UTC", null);
        final JobExecutionRepository jobExecutionRepository = mock(JobExecutionRepository.class);
        final JdbcTemplateFactory jdbcTemplateFactory = mock(JdbcTemplateFactory.class);
        final TenantDetailsService tenantDetailsService = mock(TenantDetailsService.class);
        final JobRegistry jobRegistry = mock(JobRegistry.class);
        final BusinessDateReadPlatformService businessDateReadPlatformService = mock(BusinessDateReadPlatformService.class);
        final StuckJobExecutorService stuckJobExecutorService = mock(StuckJobExecutorService.class);
        final AppUserRepositoryWrapper userRepository = mock(AppUserRepositoryWrapper.class);
        final NamedParameterJdbcTemplate failingTemplate = mock(NamedParameterJdbcTemplate.class);
        final NamedParameterJdbcTemplate healthyTemplate = mock(NamedParameterJdbcTemplate.class);

        when(jobRegistry.getJobNames()).thenReturn(Set.of("Loan COB"));
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of(failing, healthy));
        when(jdbcTemplateFactory.createNamedParameterJdbcTemplate(failing)).thenReturn(failingTemplate);
        when(jdbcTemplateFactory.createNamedParameterJdbcTemplate(healthy)).thenReturn(healthyTemplate);
        when(jobExecutionRepository.getStuckJobNames(failingTemplate)).thenReturn(List.of("failingJob"));
        when(jobExecutionRepository.getStuckJobNames(healthyTemplate)).thenReturn(List.of("healthyJob"));
        when(businessDateReadPlatformService.getBusinessDates()).thenReturn(new HashMap<>());
        when(userRepository.fetchSystemUser()).thenReturn(mock(AppUser.class));
        doThrow(new IllegalStateException("tenant database down")).when(stuckJobExecutorService).resumeStuckJob("failingJob");

        final StuckJobListener listener = new StuckJobListener(jobExecutionRepository, jdbcTemplateFactory, tenantDetailsService,
                jobRegistry, businessDateReadPlatformService, stuckJobExecutorService, userRepository);

        listener.onApplicationEvent(mock(ContextRefreshedEvent.class));

        verify(stuckJobExecutorService).resumeStuckJob("healthyJob");
        verify(stuckJobExecutorService).resumeStuckJob("failingJob");
    }
}
