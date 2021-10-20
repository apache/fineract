/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.jobs.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTriggerListener implements TriggerListener {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerTriggerListener.class);
    private static final SecureRandom random = new SecureRandom();

    private final SchedularWritePlatformService schedularService;
    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public SchedulerTriggerListener(final SchedularWritePlatformService schedularService, final TenantDetailsService tenantDetailsService) {
        this.schedularService = schedularService;
        this.tenantDetailsService = tenantDetailsService;

    }

    @Override
    public String getName() {
        return "Fineract Global Scheduler Trigger Listener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        LOG.debug("triggerFired() trigger={}, context={}", trigger, context);
    }

    @Override
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public boolean vetoJobExecution(final Trigger trigger, final JobExecutionContext context) {
        final String tenantIdentifier = trigger.getJobDataMap().getString(SchedulerServiceConstants.TENANT_IDENTIFIER);
        final FineractPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);
        final JobKey key = trigger.getJobKey();
        final String jobKey = key.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        String triggerType = SchedulerServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        Integer maxNumberOfRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxRetriesOnDeadlock();
        Integer maxIntervalBetweenRetries = ThreadLocalContextUtil.getTenant().getConnection().getMaxIntervalBetweenRetries();
        Integer numberOfRetries = 0;
        boolean vetoJob = false;
        while (numberOfRetries <= maxNumberOfRetries) {
            try {
                vetoJob = this.schedularService.processJobDetailForExecution(jobKey, triggerType);
                numberOfRetries = maxNumberOfRetries + 1;
            } catch (Exception exception) { // Adding generic exception as it
                                            // depends on JPA provider
                LOG.warn("vetoJobExecution() not able to acquire the lock to update job running status at retry {} (of {}) for JobKey: {}",
                        numberOfRetries, maxNumberOfRetries, jobKey, exception);
                try {
                    int randomNum = random.nextInt(maxIntervalBetweenRetries + 1);
                    Thread.sleep(1000 + (randomNum * 1000));
                    numberOfRetries = numberOfRetries + 1;
                } catch (InterruptedException e) {
                    LOG.error("vetoJobExecution() caught an InterruptedException", e);
                }
            }
        }
        if (vetoJob) {
            LOG.warn(
                    "vetoJobExecution() WILL veto the execution (returning vetoJob == true; the job's execute method will NOT be called); "
                            + "maxNumberOfRetries={}, tenant={}, jobKey={}, triggerType={}, trigger={}, context={}",
                    maxNumberOfRetries, tenantIdentifier, jobKey, triggerType, trigger, context);
        }
        return vetoJob;
    }

    @Override
    public void triggerMisfired(final Trigger trigger) {
        LOG.error("triggerMisfired() trigger={}", trigger);
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        LOG.debug("triggerComplete() trigger={}, context={}, completedExecutionInstruction={}", trigger, context, triggerInstructionCode);
    }
}
