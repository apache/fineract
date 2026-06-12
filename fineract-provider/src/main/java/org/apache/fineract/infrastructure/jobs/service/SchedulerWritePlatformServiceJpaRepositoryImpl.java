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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SchedulerWritePlatformServiceJpaRepositoryImpl implements SchedulerWritePlatformService {

    private final ScheduledJobDetailRepository scheduledJobDetailsRepository;

    private final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    private final SchedulerDetailRepository schedulerDetailRepository;

    private final ScheduledJobReadService scheduledJobReadService;

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
            job.setJobDisplayName(StringUtils.defaultIfEmpty(request.getDisplayName().trim(), null));
            changes.put("displayName", request.getDisplayName().trim());
        }
        if (request.getCronExpression() != null && !request.getCronExpression().trim().equals(job.getCronExpression())) {
            job.setCronExpression(StringUtils.defaultIfEmpty(request.getCronExpression().trim(), null));
            changes.put("cronExpression", request.getCronExpression().trim());
        }
        if (request.getActive() != null && request.getActive() != job.isActiveSchedular()) {
            job.setActiveSchedular(request.getActive());
            changes.put("active", request.getActive());
        }

        if (!changes.isEmpty()) {
            scheduledJobDetailsRepository.saveAndFlush(job);
        }

        return JobUpdateResponse.builder().resourceId(job.getId()).changes(changes).build();
    }

    @Transactional
    @Override
    @Retry(name = "processJobDetailForExecution", fallbackMethod = "fallbackProcessJobDetailForExecution")
    public boolean processJobDetailForExecution(final String jobKey, final String triggerType) {
        boolean isStopExecution = false;
        final ScheduledJobDetail scheduledJobDetail = this.scheduledJobDetailsRepository.findByJobKeyWithLock(jobKey);
        if (scheduledJobDetail.isCurrentlyRunning() || (triggerType.equals(SchedulerServiceConstants.TRIGGER_TYPE_CRON)
                && scheduledJobDetail.getNextRunTime().after(new Date()))) {
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
