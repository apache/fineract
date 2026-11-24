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

import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.apache.fineract.infrastructure.jobs.data.JobDetailDataValidator;
import org.apache.fineract.infrastructure.jobs.data.JobUpdateRequest;
import org.apache.fineract.infrastructure.jobs.data.JobUpdateResponse;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistoryRepository;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetailRepository;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchedulerWritePlatformServiceJpaRepositoryImpl implements SchedulerWritePlatformService {

    private final ScheduledJobDetailRepository scheduledJobDetailsRepository;

    private final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    private final SchedulerDetailRepository schedulerDetailRepository;

    private final ScheduledJobReadService scheduledJobReadService;

    private final JobDetailDataValidator dataValidator;

    @Transactional
    @Override
    public void saveOrUpdate(final ScheduledJobDetail scheduledJobDetails) {
        this.scheduledJobDetailsRepository.save(scheduledJobDetails);
    }

    @Transactional
    @Override
    public void saveOrUpdate(final ScheduledJobDetail scheduledJobDetails, final ScheduledJobRunHistory scheduledJobRunHistory) {
        this.scheduledJobDetailsRepository.save(scheduledJobDetails);
        this.scheduledJobRunHistoryRepository.save(scheduledJobRunHistory);
    }

    @Override
    @Transactional
    public void updateSchedulerDetail(final SchedulerDetail schedulerDetail) {
        this.schedulerDetailRepository.save(schedulerDetail);
    }

    @Transactional
    @Override
    public JobUpdateResponse updateJobDetail(JobUpdateRequest request) {
        ScheduledJobDetail job = scheduledJobDetailsRepository.findByJobId(request.getJobId());
        if (job == null) {
            throw new JobNotFoundException(String.valueOf(request.getJobId()));
        }

        Map<String, Object> changes = new LinkedHashMap<>();

        if (request.getDisplayName() != null && !request.getDisplayName().trim().equals(job.getJobDisplayName())) {
            job.updateDisplayName(request.getDisplayName().trim());
            changes.put("displayName", request.getDisplayName().trim());
        }
        if (request.getCronExpression() != null && !request.getCronExpression().trim().equals(job.getCronExpression())) {
            job.updateCronExpression(request.getCronExpression().trim());
            changes.put("cronExpression", request.getCronExpression().trim());
        }
        if (request.getActive() != null && request.getActive() != job.isActiveSchedular()) {
            job.updateActive(request.getActive());
            changes.put("active", request.getActive());
        }

        if (!changes.isEmpty()) {
            scheduledJobDetailsRepository.saveAndFlush(job);
        }

        return JobUpdateResponse.builder().resourceId(job.getId()).changes(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateJobDetail(final Long jobId, final JsonCommand command) {
        this.dataValidator.validateForUpdate(command.json());
        final ScheduledJobDetail scheduledJobDetail = scheduledJobReadService.findByJobId(jobId);
        if (scheduledJobDetail == null) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
        final Map<String, Object> changes = applyChanges(scheduledJobDetail, toJobUpdateRequest(jobId, command));
        if (!changes.isEmpty()) {
            this.scheduledJobDetailsRepository.saveAndFlush(scheduledJobDetail);
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(jobId) //
                .with(changes) //
                .build();
    }

    private static JobUpdateRequest toJobUpdateRequest(final Long jobId, final JsonCommand command) {
        final String displayName = command.parameterExists(SchedulerJobApiConstants.displayNameParamName)
                ? command.stringValueOfParameterNamed(SchedulerJobApiConstants.displayNameParamName)
                : null;
        final String cronExpression = command.parameterExists(SchedulerJobApiConstants.cronExpressionParamName)
                ? command.stringValueOfParameterNamed(SchedulerJobApiConstants.cronExpressionParamName)
                : null;
        final Boolean active = command.parameterExists(SchedulerJobApiConstants.jobActiveStatusParamName)
                ? command.booleanPrimitiveValueOfParameterNamed(SchedulerJobApiConstants.jobActiveStatusParamName)
                : null;
        return JobUpdateRequest.builder().jobId(jobId).displayName(displayName).cronExpression(cronExpression).active(active).build();
    }

    /**
     * Applies the update of {@code PUT /jobs/short-name/{shortName}}. Unlike
     * {@link #updateJobDetail(JobUpdateRequest)}, it compares the value as sent, before trimming it, as the former
     * {@code ScheduledJobDetail.update(JsonCommand)} did.
     */
    private static Map<String, Object> applyChanges(final ScheduledJobDetail job, final JobUpdateRequest request) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        if (request.getDisplayName() != null && !request.getDisplayName().equals(job.getJobDisplayName())) {
            final String newValue = request.getDisplayName().trim();
            actualChanges.put(SchedulerJobApiConstants.displayNameParamName, newValue);
            job.updateDisplayName(newValue);
        }
        if (request.getCronExpression() != null && !request.getCronExpression().equals(job.getCronExpression())) {
            final String newValue = request.getCronExpression().trim();
            actualChanges.put(SchedulerJobApiConstants.cronExpressionParamName, newValue);
            job.updateCronExpression(newValue);
        }
        if (request.getActive() != null && request.getActive() != job.isActiveSchedular()) {
            final boolean newValue = request.getActive();
            actualChanges.put(SchedulerJobApiConstants.jobActiveStatusParamName, newValue);
            job.updateActive(newValue);
        }

        return actualChanges;
    }

    // Annotation/aspect order matters here: the resilience4j @Retry aspect (default order
    // LOWEST_PRECEDENCE - 3) wraps Spring's transaction interceptor (LOWEST_PRECEDENCE), so each
    // retry attempt re-enters the transaction interceptor. Combined with REQUIRES_NEW, every
    // attempt runs in a brand-new transaction with a fresh DB snapshot. This is what lets a
    // serialization failure (e.g. Postgres 40001 under the pool's REPEATABLE_READ default) recover:
    // on retry the competing node's job-claim is already committed and visible, so we veto correctly
    // instead of failing. The caller (SchedulerVetoer#veto) must stay non-transactional for this.
    @Retry(name = "processJobDetailForExecution", fallbackMethod = "fallbackProcessJobDetailForExecution")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean processJobDetailForExecution(final String jobKey, final String triggerType) {
        boolean isStopExecution = false;
        final ScheduledJobDetail scheduledJobDetail = this.scheduledJobDetailsRepository.findByJobKeyWithLock(jobKey);
        if (scheduledJobDetail.isCurrentlyRunning() || (triggerType.equals(SchedulerServiceConstants.TRIGGER_TYPE_CRON)
                && scheduledJobDetail.getNextRunTime().isAfter(Instant.now()))) {
            isStopExecution = true;
        }
        final SchedulerDetail schedulerDetail = scheduledJobReadService.retrieveSchedulerDetail();
        if (triggerType.equals(SchedulerServiceConstants.TRIGGER_TYPE_CRON) && schedulerDetail.isSuspended()) {
            scheduledJobDetail.setTriggerMisfired(true);
            isStopExecution = true;
        } else if (!isStopExecution) {
            scheduledJobDetail.setCurrentlyRunning(true);
            scheduledJobDetail.setMismatchedJob(false);
        }
        this.scheduledJobDetailsRepository.save(scheduledJobDetail);
        return isStopExecution;
    }

    @SuppressWarnings("unused")
    public boolean fallbackProcessJobDetailForExecution(Exception e) {
        return false;
    }

}
