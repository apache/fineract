package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.annotation.CronMethodParser;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.exception.JobNotFoundException;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * Service class to create and load batch jobs to Scheduler using
 * {@link SchedulerFactoryBean} ,{@link MethodInvokingJobDetailFactoryBean} and
 * {@link CronTriggerFactoryBean}
 * 
 */
@Service
public class JobRegisterServiceImpl implements JobRegisterService {

    private final static Logger logger = LoggerFactory.getLogger(JobRegisterServiceImpl.class);

    private final ApplicationContext applicationContext;

    private final SchedulerFactoryBean schedulerFactoryBean;

    private final SchedularService schedularService;

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public JobRegisterServiceImpl(final ApplicationContext applicationContext, final SchedulerFactoryBean schedulerFactoryBean,
            final SchedularService schedularService, final TenantDetailsService tenantDetailsService) {
        this.applicationContext = applicationContext;
        this.schedularService = schedularService;
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.tenantDetailsService = tenantDetailsService;
    }

    @PostConstruct
    public void loadAllJobs() {
        List<MifosPlatformTenant> allTenants = this.tenantDetailsService.findAllTenants();
        for (MifosPlatformTenant tenant : allTenants) {
            ThreadLocalContextUtil.setTenant(tenant);
            List<ScheduledJobDetail> scheduledJobDetails = this.schedularService.retrieveAllJobs();
            for (ScheduledJobDetail jobDetails : scheduledJobDetails) {
                if (jobDetails.isActiveSchedular()) {
                    scheduleJob(jobDetails);
                }else{
                    jobDetails.updateNextRunTime(null);
                }
                
                this.schedularService.saveOrUpdate(jobDetails);
            }
        }
    }

    public void executeJob(final JobKey jobKey) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(SchedularServiceConstants.TRIGGER_TYPE_REFERENCE, SchedularServiceConstants.TRIGGER_TYPE_APPLICATION);
            jobDataMap.put(SchedularServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            if (schedulerFactoryBean.getScheduler().checkExists(jobKey)) {
                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final JobKey threadLocalJobKey = jobKey;
                        final JobDataMap threadLocalJobDataMap = jobDataMap;
                        try {
                            schedulerFactoryBean.getScheduler().triggerJob(threadLocalJobKey, threadLocalJobDataMap);
                        } catch (SchedulerException e) {
                            logger.error(e.getMessage(), e);
                        }

                    }
                });
                newThread.run();
            } else {
                throw new JobNotFoundException(jobKey.toString());
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public void rescheduleJob(ScheduledJobDetail scheduledJobDetails) {
        try {
            String jobIdentity = scheduledJobDetails.getJobKey();
            JobKey jobKey = constructJobKey(jobIdentity);
            schedulerFactoryBean.getScheduler().deleteJob(jobKey);
            scheduleJob(scheduledJobDetails);
            this.schedularService.saveOrUpdate(scheduledJobDetails);
        } catch (Throwable throwable) {
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
            this.schedularService.saveOrUpdate(scheduledJobDetails);
        }
    }

    @Override
    public void executeJob(Long jobId) {
        try {
            ScheduledJobDetail scheduledJobDetail = this.schedularService.findByJobId(jobId);
            if (scheduledJobDetail != null) {
                String key = scheduledJobDetail.getJobKey();
                JobKey jobKey = constructJobKey(key);
                executeJob(jobKey);
            } else {
                throw new JobNotFoundException(String.valueOf(jobId));
            }
        } catch (JobNotFoundException e) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
    }

    private void scheduleJob(ScheduledJobDetail scheduledJobDetails) {
        try {
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
            scheduledJobDetails.updateJobKey(getJobKeyAsString(jobDetail.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateCurrentlyRunningStatus(false);
            scheduledJobDetails.updateErrorLog(null);
        } catch (Throwable throwable) {
            scheduledJobDetails.updateNextRunTime(null);
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
        }
    }

    private JobDetail createJobDetail(ScheduledJobDetail scheduledJobDetails) throws Exception {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetails.getJobName());
        String[] beanNames = this.applicationContext.getBeanNamesForType(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
        Object targetObject = this.applicationContext.getBean(beanNames[0]);
        for (String beanName : beanNames) {
            Object nextObject = this.applicationContext.getBean(beanName);
            if (nextObject != null && !targetObject.getClass().isAssignableFrom(nextObject.getClass())) {
                targetObject = nextObject;
            }
        }
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetails.getJobName() + "JobDetail" + tenant.getId());
        jobDetailFactoryBean.setTargetObject(targetObject);
        jobDetailFactoryBean.setTargetMethod(jobDetails[CronMethodParser.METHOD_INDEX]);
        jobDetailFactoryBean.setGroup(scheduledJobDetails.getGroupName());
        jobDetailFactoryBean.setConcurrent(false);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    /**
     * @param scheduledJobDetails
     * @return
     */
    private Trigger createTrigger(ScheduledJobDetail scheduledJobDetails, JobDetail jobDetail) {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setName(scheduledJobDetails.getJobName() + "Trigger" + tenant.getId());
        cronTriggerFactoryBean.setJobDetail(jobDetail);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SchedularServiceConstants.TENANT_IDENTIFIER, tenant.getTenantIdentifier());
        cronTriggerFactoryBean.setJobDataMap(jobDataMap);
        TimeZone timeZone = TimeZone.getTimeZone(tenant.getTimezoneId());
        cronTriggerFactoryBean.setTimeZone(timeZone);
        cronTriggerFactoryBean.setGroup(scheduledJobDetails.getGroupName());
        cronTriggerFactoryBean.setCronExpression(scheduledJobDetails.getCroneExpression());
        cronTriggerFactoryBean.setPriority(scheduledJobDetails.getTaskPriority());
        cronTriggerFactoryBean.afterPropertiesSet();
        return cronTriggerFactoryBean.getObject();
    }

    /**
     * @param throwable
     * @return
     */
    private String getStackTraceAsString(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuffer sb = new StringBuffer(throwable.toString());
        for (StackTraceElement element : stackTraceElements) {
            sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                    .append(element.getLineNumber()).append(")");
        }
        return sb.toString();
    }

    private String getJobKeyAsString(JobKey jobKey) {
        return jobKey.getName() + SchedularServiceConstants.JOB_KEY_SEPERATOR + jobKey.getGroup();
    }

    private JobKey constructJobKey(String Key) {
        String[] keyParams = Key.split(SchedularServiceConstants.JOB_KEY_SEPERATOR);
        JobKey JobKey = new JobKey(keyParams[0], keyParams[1]);
        return JobKey;
    }

}
