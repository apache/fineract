package org.mifosplatform.infrastructure.jobs.service;


public interface JobRegisterService {

    public void executeJob(Long jobId);

    public void rescheduleJob(Long jobId);

    public void pauseScheduler();

    public void startScheduler();

    public boolean isSchedulerRunning();

    public void stopScheduler(String name);

}
