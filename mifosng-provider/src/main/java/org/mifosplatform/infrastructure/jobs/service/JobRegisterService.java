package org.mifosplatform.infrastructure.jobs.service;

import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;

public interface JobRegisterService {

    public void executeJob(Long jobId);

    public void rescheduleJob(ScheduledJobDetail scheduledJobDetails);

}
