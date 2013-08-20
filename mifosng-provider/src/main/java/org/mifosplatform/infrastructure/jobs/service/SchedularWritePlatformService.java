package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.mifosplatform.infrastructure.jobs.domain.SchedulerDetail;

public interface SchedularWritePlatformService {

    public List<ScheduledJobDetail> retrieveAllJobs();

    public ScheduledJobDetail findByJobKey(String triggerKey);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    public Long fetchMaxVersionBy(String triggerKey);

    public ScheduledJobDetail findByJobId(Long jobId);

    public CommandProcessingResult updateJobDetail(Long jobId, JsonCommand command);

    public SchedulerDetail retriveSchedulerDetail();

    public void updateSchedulerDetail(final SchedulerDetail schedulerDetail);

    public boolean processJobDetailForExecution(String jobKey, String triggerType);

}
