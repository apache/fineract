package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;

import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;

public interface SchedularService {

    public List<ScheduledJobDetail> getScheduledJobDetails();

    public ScheduledJobDetail getByTriggerKey(String triggerKey);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    public Long getMaxVersionBy(String triggerKey);

}
