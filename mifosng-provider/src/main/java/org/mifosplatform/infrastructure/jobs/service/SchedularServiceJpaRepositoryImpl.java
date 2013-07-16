package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;

import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedularServiceJpaRepositoryImpl implements SchedularService {

    private final ScheduledJobDetailRepository scheduledJobDetailsRepository;

    private final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    @Autowired
    public SchedularServiceJpaRepositoryImpl(final ScheduledJobDetailRepository scheduledJobDetailsRepository,
            final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository) {
        this.scheduledJobDetailsRepository = scheduledJobDetailsRepository;
        this.scheduledJobRunHistoryRepository = scheduledJobRunHistoryRepository;
    }

    @Override
    public List<ScheduledJobDetail> retrieveAllJobs() {
        return scheduledJobDetailsRepository.findAll();
    }

    @Override
    public ScheduledJobDetail findByJobKey(final String jobKey) {
        return scheduledJobDetailsRepository.findByJobKey(jobKey);
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
        Long versionFromDB = this.scheduledJobRunHistoryRepository.findMaxVersionByJobKey(jobKey);
        if (versionFromDB != null) {
            version = versionFromDB;
        }
        return version;
    }

    @Override
    public ScheduledJobDetail findByJobId(Long jobId) {
        return this.scheduledJobDetailsRepository.findByJobId(jobId);
    }

}
