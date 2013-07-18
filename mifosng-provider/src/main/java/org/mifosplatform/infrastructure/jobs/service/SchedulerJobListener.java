package org.mifosplatform.infrastructure.jobs.service;

import java.util.Date;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.mifosplatform.infrastructure.jobs.exception.JobInProcessExecution;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
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

    private final String name = SchedularServiceConstants.DEFAULT_LISTENER_NAME;

    private final SchedularWritePlatformService schedularService;

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public SchedulerJobListener(final SchedularWritePlatformService schedularService, final TenantDetailsService tenantDetailsService) {
        this.schedularService = schedularService;
        this.tenantDetailsService = tenantDetailsService;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        String tenantIdentifier = context.getTrigger().getJobDataMap().getString(SchedularServiceConstants.TENANT_IDENTIFIER);
        MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
        JobKey key = context.getTrigger().getJobKey();
        String jobKey = key.getName() + SchedularServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        final ScheduledJobDetail scheduledJobDetail = schedularService.findByJobKey(jobKey);
        if (scheduledJobDetail.isCurrentlyRunning()) { throw new JobInProcessExecution(scheduledJobDetail.getJobName()); }
        scheduledJobDetail.updateCurrentlyRunningStatus(true);
        this.schedularService.saveOrUpdate(scheduledJobDetail);

    }

    @Override
    public void jobExecutionVetoed(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        Trigger trigger = context.getTrigger();
        JobKey key = context.getJobDetail().getKey();
        String jobKey = key.getName() + SchedularServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        final ScheduledJobDetail scheduledJobDetails = schedularService.findByJobKey(jobKey);
        Long version = schedularService.fetchMaxVersionBy(jobKey) + 1;
        String status = SchedularServiceConstants.STATUS_SUCCESS;
        String errorMessage = null;
        String errorLog = null;
        if (jobException != null) {
            status = SchedularServiceConstants.STATUS_FAILED;
            stackTraceLevel = 0;
            Throwable throwable = getCauseFromException(jobException);
            stackTraceLevel = 0;
            StackTraceElement[] stackTraceElements = null;
            errorMessage = throwable.getMessage();
            stackTraceElements = throwable.getStackTrace();
            StringBuffer sb = new StringBuffer(throwable.toString());
            for (StackTraceElement element : stackTraceElements) {
                sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                        .append(element.getLineNumber()).append(")");
            }
            errorLog = sb.toString();

        }
        String triggerType = SchedularServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedularServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedularServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        if (triggerType == SchedularServiceConstants.TRIGGER_TYPE_CRON) {
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
        }

        scheduledJobDetails.updatePreviousRunStartTime(context.getFireTime());
        scheduledJobDetails.updateCurrentlyRunningStatus(false);

        ScheduledJobRunHistory runHistory = new ScheduledJobRunHistory(scheduledJobDetails, version, context.getFireTime(), new Date(),
                status, errorMessage, triggerType, errorLog);
        // scheduledJobDetails.addRunHistory(runHistory);

        schedularService.saveOrUpdate(scheduledJobDetails, runHistory);

    }

    private Throwable getCauseFromException(final Throwable exception) {
        if (stackTraceLevel <= SchedularServiceConstants.STACK_TRACE_LEVEL
                && exception.getCause() != null
                && (exception.getCause().toString().contains(SchedularServiceConstants.SCHEDULAR_EXCEPTION)
                        || exception.getCause().toString().contains(SchedularServiceConstants.JOB_EXECUTION_EXCEPTION) || exception
                        .getCause().toString().contains(SchedularServiceConstants.JOB_METHOD_INVOCATION_FAILED_EXCEPTION))) {
            stackTraceLevel++;
            return getCauseFromException(exception.getCause());
        } else if (exception.getCause() != null) { return exception.getCause(); }
        return exception;
    }

}
