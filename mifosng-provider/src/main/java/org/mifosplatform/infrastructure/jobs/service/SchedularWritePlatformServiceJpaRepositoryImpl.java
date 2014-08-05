/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.jobs.data.JobDetailDataValidator;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistoryRepository;
import org.mifosplatform.infrastructure.jobs.domain.SchedulerDetail;
import org.mifosplatform.infrastructure.jobs.domain.SchedulerDetailRepository;
import org.mifosplatform.infrastructure.jobs.exception.JobNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedularWritePlatformServiceJpaRepositoryImpl implements SchedularWritePlatformService {

    private final ScheduledJobDetailRepository scheduledJobDetailsRepository;

    private final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    private final SchedulerDetailRepository schedulerDetailRepository;

    private final JobDetailDataValidator dataValidator;

    @Autowired
    public SchedularWritePlatformServiceJpaRepositoryImpl(final ScheduledJobDetailRepository scheduledJobDetailsRepository,
            final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository, final JobDetailDataValidator dataValidator,
            final SchedulerDetailRepository schedulerDetailRepository) {
        this.scheduledJobDetailsRepository = scheduledJobDetailsRepository;
        this.scheduledJobRunHistoryRepository = scheduledJobRunHistoryRepository;
        this.schedulerDetailRepository = schedulerDetailRepository;
        this.dataValidator = dataValidator;
    }

    @Override
    public List<ScheduledJobDetail> retrieveAllJobs() {
        return this.scheduledJobDetailsRepository.findAll();
    }

    @Override
    public ScheduledJobDetail findByJobKey(final String jobKey) {
        return this.scheduledJobDetailsRepository.findByJobKey(jobKey);
    }

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
    public Long fetchMaxVersionBy(final String jobKey) {
        Long version = 0L;
        final Long versionFromDB = this.scheduledJobRunHistoryRepository.findMaxVersionByJobKey(jobKey);
        if (versionFromDB != null) {
            version = versionFromDB;
        }
        return version;
    }

    @Override
    public ScheduledJobDetail findByJobId(final Long jobId) {
        return this.scheduledJobDetailsRepository.findByJobId(jobId);
    }

    @Override
    @Transactional
    public void updateSchedulerDetail(final SchedulerDetail schedulerDetail) {
        this.schedulerDetailRepository.save(schedulerDetail);
    }

    @Override
    public SchedulerDetail retriveSchedulerDetail() {
        SchedulerDetail schedulerDetail = null;
        final List<SchedulerDetail> schedulerDetailList = this.schedulerDetailRepository.findAll();
        if (schedulerDetailList != null) {
            schedulerDetail = schedulerDetailList.get(0);
        }
        return schedulerDetail;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateJobDetail(final Long jobId, final JsonCommand command) {
        this.dataValidator.validateForUpdate(command.json());
        final ScheduledJobDetail scheduledJobDetail = findByJobId(jobId);
        if (scheduledJobDetail == null) { throw new JobNotFoundException(String.valueOf(jobId)); }
        final Map<String, Object> changes = scheduledJobDetail.update(command);
        if (!changes.isEmpty()) {
            this.scheduledJobDetailsRepository.saveAndFlush(scheduledJobDetail);
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(jobId) //
                .with(changes) //
                .build();

    }

    @Transactional
    @Override
    public boolean processJobDetailForExecution(final String jobKey, final String triggerType) {
        boolean isStopExecution = false;
        final ScheduledJobDetail scheduledJobDetail = this.scheduledJobDetailsRepository.findByJobKeyWithLock(jobKey);
        if (scheduledJobDetail.isCurrentlyRunning()
                || (triggerType == SchedulerServiceConstants.TRIGGER_TYPE_CRON && (scheduledJobDetail.getNextRunTime().after(new Date())))) {
            isStopExecution = true;
        }
        final SchedulerDetail schedulerDetail = retriveSchedulerDetail();
        if (triggerType == SchedulerServiceConstants.TRIGGER_TYPE_CRON && schedulerDetail.isSuspended()) {
            scheduledJobDetail.updateTriggerMisfired(true);
            isStopExecution = true;
        } else if (!isStopExecution) {
            scheduledJobDetail.updateCurrentlyRunningStatus(true);
        }
        this.scheduledJobDetailsRepository.save(scheduledJobDetail);
        return isStopExecution;
    }

}
