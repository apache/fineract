package org.mifosplatform.scheduledjobs.service;

import java.util.List;

import org.mifosplatform.scheduledjobs.domain.ScheduledJobDetails;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobDetailsRepository;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobRunHistory;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobRunHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedularServiceJpaRepositoryImpl implements SchedularService {

    private final ScheduledJobDetailsRepository scheduledJobDetailsRepository;

    private final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    @Autowired
    public SchedularServiceJpaRepositoryImpl(final ScheduledJobDetailsRepository scheduledJobDetailsRepository,
            final ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository) {
        this.scheduledJobDetailsRepository = scheduledJobDetailsRepository;
        this.scheduledJobRunHistoryRepository = scheduledJobRunHistoryRepository;
    }

    @Override
    public List<ScheduledJobDetails> getScheduledJobDetails() {
        return scheduledJobDetailsRepository.findAll();
    }

    @Override
    public ScheduledJobDetails getByTriggerKey(final String triggerKey) {
        return scheduledJobDetailsRepository.findByTriggerKey(triggerKey);
    }

    @Transactional
    @Override
    public void saveOrUpdate(final ScheduledJobDetails scheduledJobDetails) {
        this.scheduledJobDetailsRepository.save(scheduledJobDetails);
    }

    @Transactional
    @Override
    public void saveOrUpdate(final ScheduledJobDetails scheduledJobDetails, final ScheduledJobRunHistory scheduledJobRunHistory) {
        this.scheduledJobDetailsRepository.save(scheduledJobDetails);
        this.scheduledJobRunHistoryRepository.save(scheduledJobRunHistory);
    }

    @Override
    public Long getMaxVersionBy(final String triggerKey) {
        Long version = 0L;
        Long versionFromDB = scheduledJobRunHistoryRepository.findMaxVersionByTriggerKey(triggerKey);
        if (versionFromDB != null) {
            version = versionFromDB;
        }
        return version;
    }
}
