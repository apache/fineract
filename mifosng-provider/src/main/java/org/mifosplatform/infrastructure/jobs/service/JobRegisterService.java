package org.mifosplatform.infrastructure.jobs.service;

public interface JobRegisterService {

    public void executeJob(Long jobId);

    public void rescheduleJob(Long jobId);

    public void stopScheduler();

    public void startScheduler();

    public boolean isSchedulerRunning();

}
