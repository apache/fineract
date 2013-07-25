package org.mifosplatform.infrastructure.jobs.service;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Global job Listener class to Stop the temporary scheduler once job execution
 * completes
 */
@Component
public class SchedulerStopListener implements JobListener {

    private static final String name = "Singlr Trigger Global Listner";

    private final JobRegisterService jobRegisterService;

    @Autowired
    public SchedulerStopListener(final JobRegisterService jobRegisterService) {
        this.jobRegisterService = jobRegisterService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobToBeExecuted(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, @SuppressWarnings("unused") final JobExecutionException jobException) {
        final String schedulerName = context.getTrigger().getJobDataMap().getString(SchedulerServiceConstants.SCHEDULER_NAME);
        if (schedulerName != null) {
            Thread newThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    jobRegisterService.stopScheduler(schedulerName);
                }
            });
            newThread.run();
        }
    }

}
