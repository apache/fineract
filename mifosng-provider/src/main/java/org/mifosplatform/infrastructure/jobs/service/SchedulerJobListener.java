/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.service;

import java.util.Date;

import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Global job Listener class to set Tenant details to
 * {@link ThreadLocalContextUtil} for batch Job and stores the batch job status
 * to database after the execution
 * 
 */
@Component
public class SchedulerJobListener implements JobListener {

    private int stackTraceLevel = 0;

    private final String name = SchedulerServiceConstants.DEFAULT_LISTENER_NAME;

    private final SchedularWritePlatformService schedularService;

    @Autowired
    public SchedulerJobListener(final SchedularWritePlatformService schedularService) {
        this.schedularService = schedularService;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void jobToBeExecuted(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Trigger trigger = context.getTrigger();
        final JobKey key = context.getJobDetail().getKey();
        final String jobKey = key.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        final ScheduledJobDetail scheduledJobDetails = this.schedularService.findByJobKey(jobKey);
        final Long version = this.schedularService.fetchMaxVersionBy(jobKey) + 1;
        String status = SchedulerServiceConstants.STATUS_SUCCESS;
        String errorMessage = null;
        String errorLog = null;
        if (jobException != null) {
            status = SchedulerServiceConstants.STATUS_FAILED;
            this.stackTraceLevel = 0;
            final Throwable throwable = getCauseFromException(jobException);
            this.stackTraceLevel = 0;
            StackTraceElement[] stackTraceElements = null;
            errorMessage = throwable.getMessage();
            stackTraceElements = throwable.getStackTrace();
            final StringBuffer sb = new StringBuffer(throwable.toString());
            for (final StackTraceElement element : stackTraceElements) {
                sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                        .append(element.getLineNumber()).append(")");
            }
            errorLog = sb.toString();

        }
        String triggerType = SchedulerServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        if (triggerType == SchedulerServiceConstants.TRIGGER_TYPE_CRON && trigger.getNextFireTime() != null
                && trigger.getNextFireTime().after(scheduledJobDetails.getNextRunTime())) {
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
        }

        scheduledJobDetails.updatePreviousRunStartTime(context.getFireTime());
        scheduledJobDetails.updateCurrentlyRunningStatus(false);

        final ScheduledJobRunHistory runHistory = new ScheduledJobRunHistory(scheduledJobDetails, version, context.getFireTime(),
                new Date(), status, errorMessage, triggerType, errorLog);
        // scheduledJobDetails.addRunHistory(runHistory);

        this.schedularService.saveOrUpdate(scheduledJobDetails, runHistory);

    }

    private Throwable getCauseFromException(final Throwable exception) {
        if (this.stackTraceLevel <= SchedulerServiceConstants.STACK_TRACE_LEVEL
                && exception.getCause() != null
                && (exception.getCause().toString().contains(SchedulerServiceConstants.SCHEDULER_EXCEPTION)
                        || exception.getCause().toString().contains(SchedulerServiceConstants.JOB_EXECUTION_EXCEPTION) || exception
                        .getCause().toString().contains(SchedulerServiceConstants.JOB_METHOD_INVOCATION_FAILED_EXCEPTION))) {
            this.stackTraceLevel++;
            return getCauseFromException(exception.getCause());
        } else if (exception.getCause() != null) { return exception.getCause(); }
        return exception;
    }

}
