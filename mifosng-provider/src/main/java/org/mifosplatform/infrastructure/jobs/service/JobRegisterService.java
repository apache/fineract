package org.mifosplatform.infrastructure.jobs.service;

import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.annotation.CronMethodParser;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
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
public class JobRegisterService {

    private final ApplicationContext applicationContext;

    private final SchedulerFactoryBean schedulerFactoryBean;

    private final SchedularService schedularService;

    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public JobRegisterService(final ApplicationContext applicationContext, final SchedulerFactoryBean schedulerFactoryBean,
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
            List<ScheduledJobDetail> scheduledJobDetails = this.schedularService.getScheduledJobDetails();
            for (ScheduledJobDetail jobDetails : scheduledJobDetails) {
                if (jobDetails.isActiveSchedular()) {
                    scheduleJob(jobDetails);
                    this.schedularService.saveOrUpdate(jobDetails);
                }
            }
        }
    }

    public void excuteJob(TriggerKey triggerKey) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SchedularServiceConstants.TRIGGER_TYPE_REFERENCE, SchedularServiceConstants.TRIGGER_TYPE_APPLICATION);
        try {
            JobKey jobKey = schedulerFactoryBean.getScheduler().getTrigger(triggerKey).getJobKey();
            schedulerFactoryBean.getScheduler().triggerJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    public void rescheduleJob(ScheduledJobDetail scheduledJobDetails) {
        try {
            String triggerIdentity = scheduledJobDetails.getTriggerKey();
            String[] triggerKeyIds = triggerIdentity.split(SchedularServiceConstants.TRIGGER_KEY_SEPERATOR);
            TriggerKey triggerKey = new TriggerKey(triggerKeyIds[0].trim(), triggerKeyIds[1].trim());
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            schedulerFactoryBean.getScheduler().rescheduleJob(triggerKey, trigger);
            scheduledJobDetails.updateTriggerKey(getTriggerKey(trigger.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateErrorLog(null);
            this.schedularService.saveOrUpdate(scheduledJobDetails);
        } catch (Throwable throwable) {
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
            this.schedularService.saveOrUpdate(scheduledJobDetails);
        }
    }

    private void scheduleJob(ScheduledJobDetail scheduledJobDetails) {
        try {
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
            scheduledJobDetails.updateTriggerKey(getTriggerKey(trigger.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateErrorLog(null);
        } catch (Throwable throwable) {
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
        }
    }

    private JobDetail createJobDetail(ScheduledJobDetail scheduledJobDetails) throws Exception {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetails.getJobName());
        String[] beanNames = this.applicationContext.getBeanNamesForType(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
        Object targetObject = this.applicationContext.getBean(beanNames[0]);
        for(String beanName:beanNames){
            Object nextObject = this.applicationContext.getBean(beanName);
            if (nextObject != null && !targetObject.getClass().isAssignableFrom(nextObject.getClass())){
                targetObject = nextObject;
            }
        }
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetails.getJobName() + "JobDetail-" + tenant.getId());
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
        cronTriggerFactoryBean.setName(scheduledJobDetails.getJobName() + "Trigger-" + tenant.getId());
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

    private String getTriggerKey(TriggerKey triggerKey) {
        return triggerKey.getName() + SchedularServiceConstants.TRIGGER_KEY_SEPERATOR + triggerKey.getGroup();
    }

}
