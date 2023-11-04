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

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Global job Listener class to set Tenant details to {@link ThreadLocalContextUtil} for batch Job and stores the batch
 * job status to database after the execution
 */
@Component
@RequiredArgsConstructor
public class SchedulerJobListener implements JobListener {

    private final SchedularWritePlatformService schedularService;
    private final AppUserRepositoryWrapper userRepository;
    private final BusinessDateReadPlatformService businessDateReadPlatformService;
    private int stackTraceLevel = 0;

    @Override
    public String getName() {
        return SchedulerServiceConstants.DEFAULT_LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(@SuppressWarnings("unused") final JobExecutionContext context) {
        AppUser user = this.userRepository.fetchSystemUser();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        HashMap<BusinessDateType, LocalDate> businessDates = businessDateReadPlatformService.getBusinessDates();
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(businessDates);
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
            final StringBuilder sb = new StringBuilder(throwable.toString());
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
        if (SchedulerServiceConstants.TRIGGER_TYPE_CRON.equals(triggerType) && trigger.getNextFireTime() != null
                && trigger.getNextFireTime().after(scheduledJobDetails.getNextRunTime())) {
            scheduledJobDetails.setNextRunTime(trigger.getNextFireTime());
        }

        scheduledJobDetails.setPreviousRunStartTime(context.getFireTime());
        scheduledJobDetails.setCurrentlyRunning(false);

        final ScheduledJobRunHistory runHistory = new ScheduledJobRunHistory().setScheduledJobDetail(scheduledJobDetails)
                .setVersion(version).setStartTime(context.getFireTime()).setEndTime(new Date()).setStatus(status)
                .setErrorMessage(errorMessage).setTriggerType(triggerType).setErrorLog(errorLog);
        // scheduledJobDetails.addRunHistory(runHistory);

        this.schedularService.saveOrUpdate(scheduledJobDetails, runHistory);

    }

    private Throwable getCauseFromException(final Throwable exception) {
        if (this.stackTraceLevel <= SchedulerServiceConstants.STACK_TRACE_LEVEL && exception.getCause() != null
                && (exception.getCause().toString().contains(SchedulerServiceConstants.SCHEDULER_EXCEPTION)
                        || exception.getCause().toString().contains(SchedulerServiceConstants.JOB_EXECUTION_EXCEPTION)
                        || exception.getCause().toString().contains(SchedulerServiceConstants.JOB_METHOD_INVOCATION_FAILED_EXCEPTION))) {
            this.stackTraceLevel++;
            return getCauseFromException(exception.getCause());
        } else if (exception.getCause() != null) {
            return exception.getCause();
        }
        return exception;
    }

}
