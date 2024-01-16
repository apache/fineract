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

import com.google.common.base.Splitter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.JobIsNotFoundOrNotEnabledException;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.exception.JobNodeIdMismatchingException;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameData;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * Service class to create and load batch jobs to Scheduler using {@link SchedulerFactoryBean}
 * ,{@link MethodInvokingJobDetailFactoryBean} and {@link CronTriggerFactoryBean}
 */
@Service
@Slf4j
public class JobRegisterServiceImpl implements JobRegisterService, ApplicationListener<ContextClosedEvent> {

    private static final String JOB_EXECUTION_FAILED_MESSAGE = "Job execution failed for job with name: ";

    @Autowired
    private SchedularWritePlatformService schedularWritePlatformService;

    @Autowired
    private SchedulerJobListener schedulerJobListener;

    @Autowired
    private SchedulerTriggerListener globalSchedulerTriggerListener;

    private static final HashMap<String, Scheduler> SCHEDULERS = new HashMap<>(4);

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private JobStarter jobStarter;

    @Autowired
    private JobParameterDataParser dataParser;

    @Autowired
    private JobNameService jobNameService;

    private static final String JOB_STARTER_METHOD_NAME = "run";

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void executeJob(final ScheduledJobDetail scheduledJobDetail, String triggerType, Set<JobParameterDTO> jobParameterDTOSet) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (triggerType == null) {
                triggerType = SchedulerServiceConstants.TRIGGER_TYPE_APPLICATION;
            }
            jobDataMap.put(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE, triggerType);
            jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            final String schedulerName = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = SCHEDULERS.get(schedulerName);
            final JobDetail jobDetail = createJobDetail(scheduledJobDetail, jobParameterDTOSet);
            JobKey jobKey = jobDetail.getKey();
            if (scheduler == null || !scheduler.checkExists(jobKey)) {
                SchedulerStopListener schedulerStopListener = new SchedulerStopListener(this);
                final String tempSchedulerName = "temp" + scheduledJobDetail.getId();
                final Scheduler tempScheduler = createScheduler(tempSchedulerName, 1, schedulerJobListener, schedulerStopListener);
                jobDataMap.put(SchedulerServiceConstants.SCHEDULER_NAME, tempSchedulerName);
                SCHEDULERS.put(tempSchedulerName, tempScheduler);
                tempScheduler.addJob(jobDetail, true);
                tempScheduler.triggerJob(jobKey, jobDataMap);
            } else {
                scheduler.addJob(jobDetail, true);
                scheduler.triggerJob(jobKey, jobDataMap);
            }
        } catch (JobIsNotFoundOrNotEnabledException e) {
            final String msg = "Job is not found or it is disabled with job ID: " + scheduledJobDetail.getId();
            log.error("{}", msg, e);
            throw e;
        } catch (final Exception e) {
            final String msg = "Job execution failed for job with id:" + scheduledJobDetail.getId();
            log.error("{}", msg, e);
            throw new PlatformInternalServerException("error.msg.scheduler.job.execution.failed", msg, scheduledJobDetail.getId(), e);
        }

    }

    public void rescheduleJob(final ScheduledJobDetail scheduledJobDetail) {
        try {
            final String jobIdentity = scheduledJobDetail.getJobKey();
            final JobKey jobKey = constructJobKey(jobIdentity);
            final String schedulername = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = SCHEDULERS.get(schedulername);
            if (scheduler != null) {
                scheduler.deleteJob(jobKey);
            }
            scheduleJob(scheduledJobDetail);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        } catch (final Exception throwable) {
            final String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetail.setErrorLog(stackTrace);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        }
    }

    @Override
    public void pauseScheduler() {
        final SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (!schedulerDetail.isSuspended()) {
            schedulerDetail.setSuspended(true);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
        }
    }

    @Override
    public void startScheduler() {
        final SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (schedulerDetail.isSuspended()) {
            schedulerDetail.setSuspended(false);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            if (schedulerDetail.isExecuteInstructionForMisfiredJobs()) {
                final List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService
                        .retrieveAllJobs(fineractProperties.getNodeId());
                for (final ScheduledJobDetail jobDetail : scheduledJobDetails) {
                    if (jobDetail.isTriggerMisfired()) {
                        if (jobDetail.isActiveSchedular()) {
                            executeJob(jobDetail, SchedulerServiceConstants.TRIGGER_TYPE_CRON, Collections.emptySet());
                            jobDetail.setMismatchedJob(false);
                        }
                        final String schedulerName = getSchedulerName(jobDetail);
                        final Scheduler scheduler = SCHEDULERS.get(schedulerName);
                        if (scheduler != null) {
                            final String key = jobDetail.getJobKey();
                            final JobKey jobKey = constructJobKey(key);
                            try {
                                final List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                                for (final Trigger trigger : triggers) {
                                    if (trigger.getNextFireTime() != null && trigger.getNextFireTime().after(jobDetail.getNextRunTime())) {
                                        jobDetail.setNextRunTime(trigger.getNextFireTime());
                                    }
                                }
                            } catch (final SchedulerException e) {
                                log.error("Error occured.", e);
                            }
                        }
                        jobDetail.setTriggerMisfired(false);
                        this.schedularWritePlatformService.saveOrUpdate(jobDetail);
                    }
                }
            }
        }
    }

    @Override
    public void rescheduleJob(final Long jobId) {
        final ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        final String nodeIdStored = scheduledJobDetail.getNodeId().toString();
        if (nodeIdStored.equals(fineractProperties.getNodeId()) || nodeIdStored.equals("0")) {
            rescheduleJob(scheduledJobDetail);
        } else {
            scheduledJobDetail.setMismatchedJob(true);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
            throw new JobNodeIdMismatchingException(nodeIdStored, fineractProperties.getNodeId());
        }
    }

    @Override
    public void executeJobWithParameters(final Long jobId, String jobParametersJson) {
        Set<JobParameterDTO> jobParameterDTOSet = dataParser.parseExecution(jobParametersJson);
        final ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        if (scheduledJobDetail == null) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
        final String nodeIdStored = scheduledJobDetail.getNodeId().toString();

        if (nodeIdStored.equals(fineractProperties.getNodeId()) || nodeIdStored.equals("0")) {
            executeJob(scheduledJobDetail, null, jobParameterDTOSet);
        } else {
            scheduledJobDetail.setMismatchedJob(true);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
            throw new JobNodeIdMismatchingException(nodeIdStored, fineractProperties.getNodeId());
        }
    }

    @Override
    public boolean isSchedulerRunning() {
        return !this.schedularWritePlatformService.retriveSchedulerDetail().isSuspended();
    }

    /**
     * Need to use ContextClosedEvent instead of ContextStoppedEvent because in case Spring Boot fails to start-up (e.g.
     * because Tomcat port is already in use) then org.springframework.boot.SpringApplication.run(String...) does a
     * context.close(); and not a context.stop();
     */
    @Override
    public void onApplicationEvent(@SuppressWarnings("unused") ContextClosedEvent event) {
        this.stopAllSchedulers();
    }

    @Override
    public void scheduleJob(final ScheduledJobDetail scheduledJobDetails) {
        try {
            final JobDetail jobDetail = createJobDetail(scheduledJobDetails, Collections.emptySet());
            scheduledJobDetails.setJobKey(getJobKeyAsString(jobDetail.getKey()));
            if (!scheduledJobDetails.isActiveSchedular()) {
                scheduledJobDetails.setNextRunTime(null);
                scheduledJobDetails.setCurrentlyRunning(false);
                return;
            }

            final Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            final Scheduler scheduler = getScheduler(scheduledJobDetails);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduledJobDetails.setNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.setErrorLog(null);
        } catch (final Exception throwable) {
            scheduledJobDetails.setNextRunTime(null);
            final String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.setErrorLog(stackTrace);
            log.error("Could not schedule job: {}", scheduledJobDetails.getJobName(), throwable);
        }
        scheduledJobDetails.setCurrentlyRunning(false);
    }

    @Override
    public void stopAllSchedulers() {
        for (Scheduler scheduler : SCHEDULERS.values()) {
            try {
                scheduler.shutdown();
            } catch (final SchedulerException e) {
                log.error("Error occured.", e);
            }
        }
    }

    private Scheduler getScheduler(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final String schedulername = getSchedulerName(scheduledJobDetail);
        Scheduler scheduler = SCHEDULERS.get(schedulername);
        if (scheduler == null) {
            int noOfThreads = SchedulerServiceConstants.DEFAULT_THREAD_COUNT;
            if (scheduledJobDetail.getSchedulerGroup() > 0) {
                noOfThreads = SchedulerServiceConstants.GROUP_THREAD_COUNT;
            }
            scheduler = createScheduler(schedulername, noOfThreads, schedulerJobListener);
            SCHEDULERS.put(schedulername, scheduler);
        }
        return scheduler;
    }

    @Override
    public void stopScheduler(final String name) {
        final Scheduler scheduler = SCHEDULERS.remove(name);
        try {
            scheduler.shutdown();
        } catch (final SchedulerException e) {
            log.error("Error occurred.", e);
        }
    }

    private String getSchedulerName(final ScheduledJobDetail scheduledJobDetail) {
        final StringBuilder sb = new StringBuilder(20);
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        sb.append(SchedulerServiceConstants.SCHEDULER).append(tenant.getId());
        if (scheduledJobDetail.getSchedulerGroup() > 0) {
            sb.append(SchedulerServiceConstants.SCHEDULER_GROUP).append(scheduledJobDetail.getSchedulerGroup());
        }
        return sb.toString();
    }

    private Scheduler createScheduler(final String name, final int noOfThreads, JobListener... jobListeners) throws Exception {
        final SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(name);
        schedulerFactoryBean.setGlobalJobListeners(jobListeners);
        final TriggerListener[] globalTriggerListeners = { globalSchedulerTriggerListener };
        schedulerFactoryBean.setGlobalTriggerListeners(globalTriggerListeners);
        final Properties quartzProperties = new Properties();
        quartzProperties.put(SchedulerFactoryBean.PROP_THREAD_COUNT, Integer.toString(noOfThreads));
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        return schedulerFactoryBean.getScheduler();
    }

    private JobDetail createJobDetail(final ScheduledJobDetail scheduledJobDetail, Set<JobParameterDTO> jobParameterDTOSet)
            throws Exception {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        JobNameData jobName = jobNameService.getJobByHumanReadableName(scheduledJobDetail.getJobName());
        Job job;
        try {
            job = jobLocator.getJob(jobName.getEnumStyleName());
        } catch (NoSuchJobException e) {
            throw new JobIsNotFoundOrNotEnabledException(e, jobName.getEnumStyleName());
        }

        final MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetail.getJobName() + "JobDetail" + tenant.getId());
        jobDetailFactoryBean.setTargetObject(jobStarter);
        jobDetailFactoryBean.setTargetMethod(JOB_STARTER_METHOD_NAME);
        jobDetailFactoryBean.setGroup(scheduledJobDetail.getGroupName());
        jobDetailFactoryBean.setConcurrent(false);

        jobDetailFactoryBean.setArguments(job, scheduledJobDetail, jobParameterDTOSet);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    private Trigger createTrigger(final ScheduledJobDetail scheduledJobDetails, final JobDetail jobDetail) throws ParseException {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        final CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setName(scheduledJobDetails.getJobName() + "Trigger" + tenant.getId());
        cronTriggerFactoryBean.setJobDetail(jobDetail);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, tenant.getTenantIdentifier());
        cronTriggerFactoryBean.setJobDataMap(jobDataMap);
        final TimeZone timeZone = TimeZone.getTimeZone(tenant.getTimezoneId());
        cronTriggerFactoryBean.setTimeZone(timeZone);
        cronTriggerFactoryBean.setGroup(scheduledJobDetails.getGroupName());
        cronTriggerFactoryBean.setCronExpression(scheduledJobDetails.getCronExpression());
        cronTriggerFactoryBean.setPriority(scheduledJobDetails.getTaskPriority());
        cronTriggerFactoryBean.afterPropertiesSet();
        return cronTriggerFactoryBean.getObject();
    }

    private String getStackTraceAsString(final Throwable throwable) {
        final StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        final StringBuilder sb = new StringBuilder(throwable.toString());
        for (final StackTraceElement element : stackTraceElements) {
            sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                    .append(element.getLineNumber()).append(")");
        }
        return sb.toString();
    }

    private String getJobKeyAsString(final JobKey jobKey) {
        return jobKey.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + jobKey.getGroup();
    }

    private JobKey constructJobKey(final String Key) {
        final List<String> keyParams = Splitter.onPattern(SchedulerServiceConstants.JOB_KEY_SEPERATOR).splitToList(Key);
        return new JobKey(keyParams.get(0), keyParams.get(1));
    }
}
