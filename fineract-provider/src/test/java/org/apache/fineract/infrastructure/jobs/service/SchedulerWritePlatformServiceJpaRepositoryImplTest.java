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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.data.JobDetailDataValidator;
import org.apache.fineract.infrastructure.jobs.data.JobUpdateRequest;
import org.apache.fineract.infrastructure.jobs.data.JobUpdateResponse;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Covers the job update through {@code PUT /jobs/short-name/{shortName}}, which still uses the legacy command pipeline,
 * and through {@code PUT /jobs/{jobId}}, which uses the typed {@link JobUpdateRequest}.
 */
@ExtendWith(MockitoExtension.class)
class SchedulerWritePlatformServiceJpaRepositoryImplTest {

    private static final Long JOB_ID = 4L;
    private static final String CRON = "0 0 1 * * ?";
    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();

    @Mock
    private ScheduledJobDetailRepository scheduledJobDetailsRepository;
    @Mock
    private ScheduledJobReadService scheduledJobReadService;
    @Mock
    private JobDetailDataValidator dataValidator;

    @InjectMocks
    private SchedulerWritePlatformServiceJpaRepositoryImpl service;

    @Test
    void updateReportsTheChangedValuesInOrderAndStoresThemTrimmed() {
        final ScheduledJobDetail job = existing();
        final String json = "{\"active\":false,\"cronExpression\":\" 0 30 2 * * ? \",\"displayName\":\" Renamed job \"}";

        final CommandProcessingResult result = service.updateJobDetail(JOB_ID, command(json));

        assertEquals(List.of("displayName", "cronExpression", "active"), new ArrayList<>(result.getChanges().keySet()));
        assertEquals(Map.of("displayName", "Renamed job", "cronExpression", "0 30 2 * * ?", "active", false), result.getChanges());
        assertEquals("Renamed job", job.getJobDisplayName());
        assertEquals("0 30 2 * * ?", job.getCronExpression());
        assertFalse(job.isActiveSchedular());
        assertEquals(JOB_ID, result.getResourceId());
        verify(dataValidator).validateForUpdate(json);
        verify(scheduledJobDetailsRepository).saveAndFlush(job);
    }

    @Test
    void updateOfOnlyTheActiveFlagReportsOnlyThatFlag() {
        final ScheduledJobDetail job = existing();

        final CommandProcessingResult result = service.updateJobDetail(JOB_ID, command("{\"active\":false}"));

        assertEquals(Map.of("active", false), result.getChanges());
        assertEquals("Job", job.getJobDisplayName());
        assertEquals(CRON, job.getCronExpression());
        assertFalse(job.isActiveSchedular());
    }

    @Test
    void updateWithoutAChangeSavesNothing() {
        existing();

        final CommandProcessingResult result = service.updateJobDetail(JOB_ID,
                command("{\"displayName\":\"Job\",\"cronExpression\":\"" + CRON + "\",\"active\":true}"));

        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        verify(scheduledJobDetailsRepository, never()).saveAndFlush(any());
    }

    /**
     * Documents a known inconsistency, not intended behaviour: the value sent is compared before it is trimmed, so a
     * cron expression that only differs by surrounding spaces is reported as a change (and the job is rescheduled),
     * although the stored value stays the same. The typed {@code PUT /jobs/{jobId}} compares the trimmed value and
     * reports no change. This test must change when both behave the same.
     */
    @Test
    void updateReportsAValueThatOnlyDiffersBySpacesAsAChange() {
        final ScheduledJobDetail job = existing();

        final CommandProcessingResult result = service.updateJobDetail(JOB_ID, command("{\"cronExpression\":\"" + CRON + "  \"}"));

        assertEquals(Map.of("cronExpression", CRON), result.getChanges());
        assertEquals(CRON, job.getCronExpression());
        verify(scheduledJobDetailsRepository).saveAndFlush(job);
    }

    @Test
    void updateOfAnUnknownJobFails() {
        when(scheduledJobReadService.findByJobId(JOB_ID)).thenReturn(null);

        assertThrows(JobNotFoundException.class, () -> service.updateJobDetail(JOB_ID, command("{\"active\":false}")));
        verify(scheduledJobDetailsRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateKeepsAnUnchangedDisplayNameWhenOnlyTheCronExpressionChanges() {
        final ScheduledJobDetail job = existing();

        final CommandProcessingResult result = service.updateJobDetail(JOB_ID, command("{\"cronExpression\":\"0 0 3 * * ?\"}"));

        assertEquals(Map.of("cronExpression", "0 0 3 * * ?"), result.getChanges());
        assertEquals("Job", job.getJobDisplayName());
        assertNull(result.getChanges().get("displayName"));
    }

    @Test
    void typedUpdateReportsAndStoresTheTrimmedValues() {
        final ScheduledJobDetail job = new ScheduledJobDetail().setJobDisplayName("Job").setCronExpression(CRON).setActiveSchedular(true);
        when(scheduledJobDetailsRepository.findByJobId(JOB_ID)).thenReturn(job);

        final JobUpdateResponse response = service.updateJobDetail(JobUpdateRequest.builder().jobId(JOB_ID).displayName(" Renamed job ")
                .cronExpression(" 0 30 2 * * ? ").active(false).build());

        assertEquals(List.of("displayName", "cronExpression", "active"), new ArrayList<>(response.getChanges().keySet()));
        assertEquals(Map.of("displayName", "Renamed job", "cronExpression", "0 30 2 * * ?", "active", false), response.getChanges());
        assertEquals("Renamed job", job.getJobDisplayName());
        assertEquals("0 30 2 * * ?", job.getCronExpression());
        assertFalse(job.isActiveSchedular());
        verify(scheduledJobDetailsRepository).saveAndFlush(job);
    }

    @Test
    void typedUpdateIgnoresAValueThatOnlyDiffersBySpaces() {
        final ScheduledJobDetail job = new ScheduledJobDetail().setJobDisplayName("Job").setCronExpression(CRON).setActiveSchedular(true);
        when(scheduledJobDetailsRepository.findByJobId(JOB_ID)).thenReturn(job);

        final JobUpdateResponse response = service
                .updateJobDetail(JobUpdateRequest.builder().jobId(JOB_ID).cronExpression(CRON + "  ").build());

        assertTrue(response.getChanges().isEmpty());
        verify(scheduledJobDetailsRepository, never()).saveAndFlush(any());
    }

    private ScheduledJobDetail existing() {
        final ScheduledJobDetail job = new ScheduledJobDetail().setJobDisplayName("Job").setCronExpression(CRON).setActiveSchedular(true);
        when(scheduledJobReadService.findByJobId(JOB_ID)).thenReturn(job);
        return job;
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }
}
