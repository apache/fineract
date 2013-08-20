package org.mifosplatform.infrastructure.jobs.service;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTriggerListener implements TriggerListener {

    private final String name = "Global trigger Listner";

    private final SchedularWritePlatformService schedularService;

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public SchedulerTriggerListener(final SchedularWritePlatformService schedularService, final TenantDetailsService tenantDetailsService) {
        this.schedularService = schedularService;
        this.tenantDetailsService = tenantDetailsService;

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void triggerFired(@SuppressWarnings("unused") Trigger trigger, @SuppressWarnings("unused") JobExecutionContext context) {

    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {

        String tenantIdentifier = trigger.getJobDataMap().getString(SchedulerServiceConstants.TENANT_IDENTIFIER);
        MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
        JobKey key = trigger.getJobKey();
        String jobKey = key.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        String triggerType = SchedulerServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        return schedularService.processJobDetailForExecution(jobKey, triggerType);
    }

    @Override
    public void triggerMisfired(@SuppressWarnings("unused") Trigger trigger) {

    }

    @Override
    public void triggerComplete(@SuppressWarnings("unused") Trigger trigger, @SuppressWarnings("unused") JobExecutionContext context,
            @SuppressWarnings("unused") CompletedExecutionInstruction triggerInstructionCode) {

    }

}
