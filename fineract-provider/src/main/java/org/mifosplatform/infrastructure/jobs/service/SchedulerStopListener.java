/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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

    // MIFOSX-1184: This class cannot use constructor injection, because one of
    // its dependencies (SchedulerStopListener) has a circular dependency to
    // itself. So, slightly differently from how it's done elsewhere in this
    // code base, the following fields are not final, and there is no
    // constructor, but setters.

    private JobRegisterService jobRegisterService;

    @Autowired
    public void setJobRegisterService(JobRegisterService jobRegisterService) {
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
            final Thread newThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    SchedulerStopListener.this.jobRegisterService.stopScheduler(schedulerName);
                }
            });
            newThread.run();
        }
    }

}
