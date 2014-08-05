/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
    public void triggerFired(@SuppressWarnings("unused") final Trigger trigger,
            @SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public boolean vetoJobExecution(final Trigger trigger, final JobExecutionContext context) {

        final String tenantIdentifier = trigger.getJobDataMap().getString(SchedulerServiceConstants.TENANT_IDENTIFIER);
        final MifosPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
        final JobKey key = trigger.getJobKey();
        final String jobKey = key.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        String triggerType = SchedulerServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        return this.schedularService.processJobDetailForExecution(jobKey, triggerType);
    }

    @Override
    public void triggerMisfired(@SuppressWarnings("unused") final Trigger trigger) {

    }

    @Override
    public void triggerComplete(@SuppressWarnings("unused") final Trigger trigger,
            @SuppressWarnings("unused") final JobExecutionContext context,
            @SuppressWarnings("unused") final CompletedExecutionInstruction triggerInstructionCode) {

    }

}
