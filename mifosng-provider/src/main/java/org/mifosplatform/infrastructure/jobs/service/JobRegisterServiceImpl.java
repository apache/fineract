package org.mifosplatform.infrastructure.jobs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.quartz.JobListener;
import org.quartz.Scheduler;
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

    private final SchedularWritePlatformService schedularWritePlatformService;

    private final TenantDetailsService tenantDetailsService;

    private final HashMap<Long, Boolean> schedulerStatus = new HashMap<Long, Boolean>(2);

    private final HashMap<String, Scheduler> schedulers = new HashMap<String, Scheduler>(4);

    @Autowired
    public JobRegisterServiceImpl(final ApplicationContext applicationContext, final SchedularWritePlatformService schedularService,
            final TenantDetailsService tenantDetailsService) {
        this.applicationContext = applicationContext;
        this.schedularWritePlatformService = schedularService;
        this.tenantDetailsService = tenantDetailsService;
    }

    @PostConstruct
    public void loadAllJobs() {
        List<MifosPlatformTenant> allTenants = this.tenantDetailsService.findAllTenants();
        for (MifosPlatformTenant tenant : allTenants) {
            ThreadLocalContextUtil.setTenant(tenant);
            List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService.retrieveAllJobs();
            schedulerStatus.put(tenant.getId(), true);
            for (ScheduledJobDetail jobDetails : scheduledJobDetails) {
                scheduleJob(jobDetails);
                this.schedularWritePlatformService.saveOrUpdate(jobDetails);
            }
        }
    }

    public void executeJob(final ScheduledJobDetail scheduledJobDetail) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(SchedularServiceConstants.TRIGGER_TYPE_REFERENCE, SchedularServiceConstants.TRIGGER_TYPE_APPLICATION);
            jobDataMap.put(SchedularServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            String key = scheduledJobDetail.getJobKey();
            JobKey jobKey = constructJobKey(key);
            String schedulerName = getSchedulerName(scheduledJobDetail);
            Scheduler scheduler = schedulers.get(schedulerName);
            if (scheduler != null && scheduler.checkExists(jobKey)) {
                try {
                    scheduler.triggerJob(jobKey, jobDataMap);
                } catch (SchedulerException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                throw new JobNotFoundException(jobKey.toString());
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void rescheduleJob(ScheduledJobDetail scheduledJobDetail) {
        try {
            String jobIdentity = scheduledJobDetail.getJobKey();
            JobKey jobKey = constructJobKey(jobIdentity);
            String schedulername = getSchedulerName(scheduledJobDetail);
            Scheduler scheduler = schedulers.get(schedulername);
            if (scheduler != null) {
                scheduler.deleteJob(jobKey);
            }
            scheduleJob(scheduledJobDetail);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        } catch (Throwable throwable) {
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetail.updateErrorLog(stackTrace);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        }
    }

    @Override
    public void stopScheduler() {
        schedulerStatus.put(ThreadLocalContextUtil.getTenant().getId(), false);
        for (Map.Entry<String, Scheduler> schedulerEntry : this.schedulers.entrySet()) {
            try {
                String schedulerName = schedulerEntry.getValue().getSchedulerName();
                if (isCurrentTenantsScheduler(schedulerName)) {
                    schedulerEntry.getValue().standby();
                }
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void startScheduler() {
        schedulerStatus.put(ThreadLocalContextUtil.getTenant().getId(), true);
        for (Map.Entry<String, Scheduler> schedulerEntry : this.schedulers.entrySet()) {
            try {
                String schedulerName = schedulerEntry.getValue().getSchedulerName();
                if (isCurrentTenantsScheduler(schedulerName)) {
                    schedulerEntry.getValue().start();
                }
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void rescheduleJob(Long jobId) {
        ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        rescheduleJob(scheduledJobDetail);
    }

    @Override
    public void executeJob(Long jobId) {
        try {
            ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
            if (scheduledJobDetail != null) {
                executeJob(scheduledJobDetail);
            } else {
                throw new JobNotFoundException(String.valueOf(jobId));
            }
        } catch (JobNotFoundException e) {
            throw new JobNotFoundException(String.valueOf(jobId));
        }
    }

    @Override
    public boolean isSchedulerRunning() {
        return this.schedulerStatus.get(ThreadLocalContextUtil.getTenant().getId());
    }

    private boolean isCurrentTenantsScheduler(String schedulerName) {
        int beginIndex = SchedularServiceConstants.SCHEDULER.length();
        int endIndex = schedulerName.length();
        if (schedulerName.contains(SchedularServiceConstants.SCHEDULER_GROUP)) {
            endIndex = schedulerName.indexOf(SchedularServiceConstants.SCHEDULER_GROUP);
        }
        String tenantId = schedulerName.substring(beginIndex, endIndex);
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        return tenant.getId() == Long.valueOf(tenantId);
    }

    private void scheduleJob(ScheduledJobDetail scheduledJobDetails) {
        if (!scheduledJobDetails.isActiveSchedular()) {
            scheduledJobDetails.updateNextRunTime(null);
            scheduledJobDetails.updateCurrentlyRunningStatus(false);
            return;
        }
        try {
            Scheduler scheduler = getScheduler(scheduledJobDetails);
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduledJobDetails.updateJobKey(getJobKeyAsString(jobDetail.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateErrorLog(null);
        } catch (Throwable throwable) {
            scheduledJobDetails.updateNextRunTime(null);
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
        }
        scheduledJobDetails.updateCurrentlyRunningStatus(false);
    }

    private Scheduler getScheduler(ScheduledJobDetail scheduledJobDetail) throws Exception {
        String schedulername = getSchedulerName(scheduledJobDetail);
        Scheduler scheduler = this.schedulers.get(schedulername);
        if (scheduler == null) {
            int noOfThreads = SchedularServiceConstants.DEFAULT_THREAD_COUNT;
            if (scheduledJobDetail.getSchedulerGroup() > 0) {
                noOfThreads = SchedularServiceConstants.GROUP_THREAD_COUNT;
            }
            scheduler = createScheduler(schedulername, noOfThreads);
            this.schedulers.put(schedulername, scheduler);
        }
        return scheduler;
    }

    private String getSchedulerName(ScheduledJobDetail scheduledJobDetail) {
        StringBuilder sb = new StringBuilder(20);
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        sb.append(SchedularServiceConstants.SCHEDULER).append(tenant.getId());
        if (scheduledJobDetail.getSchedulerGroup() > 0) {
            sb.append(SchedularServiceConstants.SCHEDULER_GROUP).append(scheduledJobDetail.getSchedulerGroup());
        }
        return sb.toString();
    }

    private Scheduler createScheduler(String name, int noOfThreads) throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(name);
        schedulerFactoryBean.setGlobalJobListeners(getGlobalListener());
        Properties quartzProperties = new Properties();
        quartzProperties.put(SchedulerFactoryBean.PROP_THREAD_COUNT, Integer.toString(noOfThreads));
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        return schedulerFactoryBean.getScheduler();
    }

    private JobListener[] getGlobalListener() throws ClassNotFoundException {
        JobListener listener = (JobListener) getBeanObject(SchedulerJobListener.class);
        JobListener[] listenerArray = { listener };
        return listenerArray;
    }

    private JobDetail createJobDetail(ScheduledJobDetail scheduledJobDetails) throws Exception {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetails.getJobName());
        Object targetObject = getBeanObject(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
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
     * @param jobDetails
     * @return
     * @throws ClassNotFoundException
     */
    private Object getBeanObject(Class<?> classType) throws ClassNotFoundException {
        String[] beanNames = this.applicationContext.getBeanNamesForType(classType);
        Object targetObject = this.applicationContext.getBean(beanNames[0]);
        for (String beanName : beanNames) {
            Object nextObject = this.applicationContext.getBean(beanName);
            if (nextObject != null && !targetObject.getClass().isAssignableFrom(nextObject.getClass())) {
                targetObject = nextObject;
            }
        }
        return targetObject;
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
        cronTriggerFactoryBean.setCronExpression(scheduledJobDetails.getCronExpression());
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
