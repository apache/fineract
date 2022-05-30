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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.domain.JobParameter;
import org.apache.fineract.infrastructure.jobs.domain.JobParameterRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.exception.JobNodeIdMismatchingException;
import org.apache.fineract.infrastructure.jobs.exception.JobNotFoundException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
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

    @Autowired
    private SchedularWritePlatformService schedularWritePlatformService;

    @Autowired
    private SchedulerJobListener schedulerJobListener;

    @Autowired
    private SchedulerTriggerListener globalSchedulerTriggerListener;

    @Autowired
    private JobParameterRepository jobParameterRepository;

    @Autowired
    private JobExplorer jobExplorer;

    private final HashMap<String, Scheduler> schedulers = new HashMap<>(4);

    // This cannot be injected as Autowired due to circular dependency
    private SchedulerStopListener schedulerStopListener = new SchedulerStopListener(this);

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    private static final String JOB_LAUNCHER_STARTER_METHOD_NAME = "run";

    public void executeJob(final ScheduledJobDetail scheduledJobDetail, String triggerType) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (triggerType == null) {
                triggerType = SchedulerServiceConstants.TRIGGER_TYPE_APPLICATION;
            }
            jobDataMap.put(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE, triggerType);
            jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            final String schedulerName = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = this.schedulers.get(schedulerName);
            final JobDetail jobDetail = createJobDetail(scheduledJobDetail);
            JobKey jobKey = jobDetail.getKey();
            if (scheduler == null || !scheduler.checkExists(jobKey)) {
                final String tempSchedulerName = "temp" + scheduledJobDetail.getId();
                final Scheduler tempScheduler = createScheduler(tempSchedulerName, 1, schedulerJobListener, schedulerStopListener);
                jobDataMap.put(SchedulerServiceConstants.SCHEDULER_NAME, tempSchedulerName);
                schedulers.put(tempSchedulerName, tempScheduler);
                tempScheduler.addJob(jobDetail, true);
                tempScheduler.triggerJob(jobKey, jobDataMap);
            } else {
                scheduler.addJob(jobDetail, true);
                scheduler.triggerJob(jobKey, jobDataMap);
            }
        } catch (final Exception e) {
            final String msg = "Job execution failed for job with id:" + scheduledJobDetail.getId();
            log.error("{}", msg, e);
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", msg, scheduledJobDetail.getId(), e);
        }

    }

    public void rescheduleJob(final ScheduledJobDetail scheduledJobDetail) {
        try {
            final String jobIdentity = scheduledJobDetail.getJobKey();
            final JobKey jobKey = constructJobKey(jobIdentity);
            final String schedulername = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = this.schedulers.get(schedulername);
            if (scheduler != null) {
                scheduler.deleteJob(jobKey);
            }
            scheduleJob(scheduledJobDetail);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        } catch (final Throwable throwable) {
            final String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetail.updateErrorLog(stackTrace);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        }
    }

    @Override
    public void pauseScheduler() {
        final SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (!schedulerDetail.isSuspended()) {
            schedulerDetail.updateSuspendedState(true);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
        }
    }

    @Override
    public void startScheduler() {
        final SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (schedulerDetail.isSuspended()) {
            schedulerDetail.updateSuspendedState(false);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            if (schedulerDetail.isExecuteInstructionForMisfiredJobs()) {
                final List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService
                        .retrieveAllJobs(fineractProperties.getNodeId());
                for (final ScheduledJobDetail jobDetail : scheduledJobDetails) {
                    if (jobDetail.isTriggerMisfired() || jobDetail.getIsMismatchedJob()) {
                        if (jobDetail.isActiveSchedular()) {
                            executeJob(jobDetail, SchedulerServiceConstants.TRIGGER_TYPE_CRON);
                            jobDetail.setIsMismatchedJob(false);
                        }
                        final String schedulerName = getSchedulerName(jobDetail);
                        final Scheduler scheduler = this.schedulers.get(schedulerName);
                        if (scheduler != null) {
                            final String key = jobDetail.getJobKey();
                            final JobKey jobKey = constructJobKey(key);
                            try {
                                final List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                                for (final Trigger trigger : triggers) {
                                    if (trigger.getNextFireTime() != null && trigger.getNextFireTime().after(jobDetail.getNextRunTime())) {
                                        jobDetail.updateNextRunTime(trigger.getNextFireTime());
                                    }
                                }
                            } catch (final SchedulerException e) {
                                log.error("Error occured.", e);
                            }
                        }
                        jobDetail.updateTriggerMisfired(false);
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
            scheduledJobDetail.setIsMismatchedJob(true);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
            throw new JobNodeIdMismatchingException(nodeIdStored, fineractProperties.getNodeId());
        }
    }

    @Override
    public void executeJob(final Long jobId) {
        final ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        if (scheduledJobDetail == null) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
        final String nodeIdStored = scheduledJobDetail.getNodeId().toString();

        if (nodeIdStored.equals(fineractProperties.getNodeId()) || nodeIdStored.equals("0")) {
            executeJob(scheduledJobDetail, null);
            scheduledJobDetail.setIsMismatchedJob(false);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        } else {
            scheduledJobDetail.setIsMismatchedJob(true);
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
        if (!scheduledJobDetails.isActiveSchedular()) {
            scheduledJobDetails.updateNextRunTime(null);
            scheduledJobDetails.updateCurrentlyRunningStatus(false);
            return;
        }
        try {
            final JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            final Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            final Scheduler scheduler = getScheduler(scheduledJobDetails);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduledJobDetails.updateJobKey(getJobKeyAsString(jobDetail.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateErrorLog(null);
        } catch (final Throwable throwable) {
            scheduledJobDetails.updateNextRunTime(null);
            final String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
            log.error("Could not schedule job: {}", scheduledJobDetails.getJobName(), throwable);
        }
        scheduledJobDetails.updateCurrentlyRunningStatus(false);
    }

    @Override
    public void stopAllSchedulers() {
        for (Scheduler scheduler : this.schedulers.values()) {
            try {
                scheduler.shutdown();
            } catch (final SchedulerException e) {
                log.error("Error occured.", e);
            }
        }
    }

    private Scheduler getScheduler(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final String schedulername = getSchedulerName(scheduledJobDetail);
        Scheduler scheduler = this.schedulers.get(schedulername);
        if (scheduler == null) {
            int noOfThreads = SchedulerServiceConstants.DEFAULT_THREAD_COUNT;
            if (scheduledJobDetail.getSchedulerGroup() > 0) {
                noOfThreads = SchedulerServiceConstants.GROUP_THREAD_COUNT;
            }
            scheduler = createScheduler(schedulername, noOfThreads, schedulerJobListener);
            this.schedulers.put(schedulername, scheduler);
        }
        return scheduler;
    }

    @Override
    public void stopScheduler(final String name) {
        final Scheduler scheduler = this.schedulers.remove(name);
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

    private JobDetail createJobDetail(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();

        JobName jobName = JobName.getJobByName(scheduledJobDetail.getJobName());
        Job job = jobLocator.getJob(jobName.name());

        final MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetail.getJobName() + "JobDetail" + tenant.getId());
        jobDetailFactoryBean.setTargetObject(jobLauncher);
        jobDetailFactoryBean.setTargetMethod(JOB_LAUNCHER_STARTER_METHOD_NAME);
        jobDetailFactoryBean.setGroup(scheduledJobDetail.getGroupName());
        jobDetailFactoryBean.setConcurrent(false);

        Map<String, org.springframework.batch.core.JobParameter> jobParameterMap = getJobParameter(scheduledJobDetail);
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(job)
                .addJobParameters(new JobParameters(jobParameterMap)).toJobParameters();

        jobDetailFactoryBean.setArguments(job, jobParameters);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    public Map<String, org.springframework.batch.core.JobParameter> getJobParameter(ScheduledJobDetail scheduledJobDetail) {
        List<JobParameter> jobParameterList = jobParameterRepository.findJobParametersByJobId(scheduledJobDetail.getId());
        Map<String, org.springframework.batch.core.JobParameter> jobParameterMap = new HashMap<>();
        for (JobParameter jobparameter : jobParameterList) {
            jobParameterMap.put(jobparameter.getParameterName(),
                    new org.springframework.batch.core.JobParameter(jobparameter.getParameterValue()));
        }
        return jobParameterMap;
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
        final JobKey jobKey = new JobKey(keyParams.get(0), keyParams.get(1));
        return jobKey;
    }
}
