package org.mifosplatform.scheduledjobs.service;

import java.util.List;

import org.mifosplatform.scheduledjobs.domain.ScheduledJobDetails;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobRunHistory;

public interface SchedularService {

    public List<ScheduledJobDetails> getScheduledJobDetails();

    public ScheduledJobDetails getByTriggerKey(String triggerKey);

    public void saveOrUpdate(ScheduledJobDetails scheduledJobDetails);

    public void saveOrUpdate(ScheduledJobDetails scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    public Long getMaxVersionBy(String triggerKey);

}
