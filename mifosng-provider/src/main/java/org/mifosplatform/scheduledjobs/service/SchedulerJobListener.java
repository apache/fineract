package org.mifosplatform.scheduledjobs.service;

import java.util.Date;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobDetails;
import org.mifosplatform.scheduledjobs.domain.ScheduledJobRunHistory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
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

    private String name = SchedularServiceConstants.DEFAULT_LISTENER_NAME;

    private final SchedularService schedularService;

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public SchedulerJobListener(SchedularService schedularService, TenantDetailsService tenantDetailsService) {
        this.schedularService = schedularService;
        this.tenantDetailsService = tenantDetailsService;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String tenantIdentifier = context.getTrigger().getJobDataMap().getString(SchedularServiceConstants.TENANT_IDENTIFIER);
        MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Trigger trigger = context.getTrigger();
        TriggerKey key = trigger.getKey();
        String triggerKey = key.getName() + SchedularServiceConstants.TRIGGER_KEY_SEPERATOR + key.getGroup();
        final ScheduledJobDetails scheduledJobDetails = schedularService.getByTriggerKey(triggerKey);
        Long version = schedularService.getMaxVersionBy(triggerKey) + 1;
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

        scheduledJobDetails.updatePreviousRunStartTime(context.getFireTime());
        scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());

        ScheduledJobRunHistory runHistory = new ScheduledJobRunHistory(scheduledJobDetails, version, context.getFireTime(), new Date(),
                status, errorMessage, triggerType, errorLog);
        // scheduledJobDetails.addRunHistory(runHistory);

        schedularService.saveOrUpdate(scheduledJobDetails, runHistory);

    }

    private Throwable getCauseFromException(Throwable exception) {
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
