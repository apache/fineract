package org.mifosplatform.infrastructure.jobs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.exception.PlatformInternalServerException;
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
            jobDataMap.put(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE, SchedulerServiceConstants.TRIGGER_TYPE_APPLICATION);
            jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            String key = scheduledJobDetail.getJobKey();
            JobKey jobKey = constructJobKey(key);
            String schedulerName = getSchedulerName(scheduledJobDetail);
            Scheduler scheduler = schedulers.get(schedulerName);
            if (scheduler == null || scheduler.isInStandbyMode() || !scheduler.checkExists(jobKey)) {
                JobDetail jobDetail = createJobDetail(scheduledJobDetail);
                String tempSchedulerName = "temp" + scheduledJobDetail.getId();
                List<Class<? extends JobListener>> listenerClasses = new ArrayList<Class<? extends JobListener>>(2);
                listenerClasses.add(SchedulerJobListener.class);
                listenerClasses.add(SchedulerStopListener.class);
                Scheduler tempScheduler = createScheduler(tempSchedulerName, 1, listenerClasses);
                tempScheduler.addJob(jobDetail, true);
                jobDataMap.put(SchedulerServiceConstants.SCHEDULER_NAME, tempSchedulerName);
                this.schedulers.put(tempSchedulerName, tempScheduler);
                tempScheduler.triggerJob(jobDetail.getKey(), jobDataMap);
            } else {
                scheduler.triggerJob(jobKey, jobDataMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", "Job execution failed for job with id:"
                    + scheduledJobDetail.getId(), scheduledJobDetail.getId());
        }

    }

    public void rescheduleJob(final ScheduledJobDetail scheduledJobDetail) {
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
    public void pauseScheduler() {
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
        schedulerStatus.put(ThreadLocalContextUtil.getTenant().getId(), true);
    }

    @Override
    public void rescheduleJob(final Long jobId) {
        ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        rescheduleJob(scheduledJobDetail);
    }

    @Override
    public void executeJob(final Long jobId) {
        ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        if (scheduledJobDetail == null) { throw new JobNotFoundException(String.valueOf(jobId)); }
        executeJob(scheduledJobDetail);
    }

    @Override
    public boolean isSchedulerRunning() {
        return this.schedulerStatus.get(ThreadLocalContextUtil.getTenant().getId());
    }

    private boolean isCurrentTenantsScheduler(final String schedulerName) {
        int beginIndex = SchedulerServiceConstants.SCHEDULER.length();
        int endIndex = schedulerName.length();
        if (schedulerName.contains(SchedulerServiceConstants.SCHEDULER_GROUP)) {
            endIndex = schedulerName.indexOf(SchedulerServiceConstants.SCHEDULER_GROUP);
        }
        String tenantId = schedulerName.substring(beginIndex, endIndex);
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        return tenant.getId() == Long.valueOf(tenantId);
    }

    private void scheduleJob(final ScheduledJobDetail scheduledJobDetails) {
        if (!scheduledJobDetails.isActiveSchedular()) {
            scheduledJobDetails.updateNextRunTime(null);
            scheduledJobDetails.updateCurrentlyRunningStatus(false);
            return;
        }
        try {
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            Scheduler scheduler = getScheduler(scheduledJobDetails);
            if (!this.schedulerStatus.get(ThreadLocalContextUtil.getTenant().getId()) && !scheduler.isInStandbyMode()) {
                scheduler.standby();
            }
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

    private Scheduler getScheduler(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        String schedulername = getSchedulerName(scheduledJobDetail);
        Scheduler scheduler = this.schedulers.get(schedulername);
        if (scheduler == null) {
            int noOfThreads = SchedulerServiceConstants.DEFAULT_THREAD_COUNT;
            if (scheduledJobDetail.getSchedulerGroup() > 0) {
                noOfThreads = SchedulerServiceConstants.GROUP_THREAD_COUNT;
            }
            scheduler = createScheduler(schedulername, noOfThreads, null);
            this.schedulers.put(schedulername, scheduler);
        }
        return scheduler;
    }

    @Override
    public void stopScheduler(String name) {
        Scheduler scheduler = this.schedulers.remove(name);
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSchedulerName(final ScheduledJobDetail scheduledJobDetail) {
        StringBuilder sb = new StringBuilder(20);
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        sb.append(SchedulerServiceConstants.SCHEDULER).append(tenant.getId());
        if (scheduledJobDetail.getSchedulerGroup() > 0) {
            sb.append(SchedulerServiceConstants.SCHEDULER_GROUP).append(scheduledJobDetail.getSchedulerGroup());
        }
        return sb.toString();
    }

    private Scheduler createScheduler(final String name, final int noOfThreads, List<Class<? extends JobListener>> listenerClasses)
            throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(name);
        if (listenerClasses == null) {
            listenerClasses = new ArrayList<Class<? extends JobListener>>(1);
            listenerClasses.add(SchedulerJobListener.class);
        }
        schedulerFactoryBean.setGlobalJobListeners(getGlobalListener(listenerClasses));
        Properties quartzProperties = new Properties();
        quartzProperties.put(SchedulerFactoryBean.PROP_THREAD_COUNT, Integer.toString(noOfThreads));
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        return schedulerFactoryBean.getScheduler();
    }

    private JobListener[] getGlobalListener(List<Class<? extends JobListener>> listenerClasses) throws ClassNotFoundException {
        List<JobListener> listeners = new ArrayList<JobListener>(listenerClasses.size());
        for (Class<?> listenerClass : listenerClasses) {
            JobListener listener = (JobListener) getBeanObject(listenerClass);
            listeners.add(listener);
        }
        JobListener[] listenerArray = new JobListener[listeners.size()];
        return listeners.toArray(listenerArray);
    }

    private JobDetail createJobDetail(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetail.getJobName());
        Object targetObject = getBeanObject(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetail.getJobName() + "JobDetail" + tenant.getId());
        jobDetailFactoryBean.setTargetObject(targetObject);
        jobDetailFactoryBean.setTargetMethod(jobDetails[CronMethodParser.METHOD_INDEX]);
        jobDetailFactoryBean.setGroup(scheduledJobDetail.getGroupName());
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
        List<Class<?>> typesList = new ArrayList<Class<?>>();
        Class<?>[] interfaceType = classType.getInterfaces();
        if (interfaceType.length > 0) {
            typesList.addAll(Arrays.asList(interfaceType));
        } else {
            Class<?> superclassType = classType;
            while (!Object.class.getName().equals(superclassType.getSuperclass().getName())) {
                superclassType = superclassType.getSuperclass();
            }
            typesList.add(superclassType);
        }
        List<String> beanNames = new ArrayList<String>();
        for (Class<?> clazz : typesList) {
            beanNames.addAll(Arrays.asList(this.applicationContext.getBeanNamesForType(clazz)));
        }
        Object targetObject = null;
        for (String beanName : beanNames) {
            Object nextObject = this.applicationContext.getBean(beanName);
            String targetObjName = nextObject.toString();
            targetObjName = targetObjName.substring(0, targetObjName.lastIndexOf("@"));
            if (classType.getName().equals(targetObjName)) {
                targetObject = nextObject;
                break;
            }
        }
        return targetObject;
    }

    /**
     * @param scheduledJobDetails
     * @return
     */
    private Trigger createTrigger(final ScheduledJobDetail scheduledJobDetails, final JobDetail jobDetail) {
        MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setName(scheduledJobDetails.getJobName() + "Trigger" + tenant.getId());
        cronTriggerFactoryBean.setJobDetail(jobDetail);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, tenant.getTenantIdentifier());
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

    private String getJobKeyAsString(final JobKey jobKey) {
        return jobKey.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + jobKey.getGroup();
    }

    private JobKey constructJobKey(final String Key) {
        String[] keyParams = Key.split(SchedulerServiceConstants.JOB_KEY_SEPERATOR);
        JobKey JobKey = new JobKey(keyParams[0], keyParams[1]);
        return JobKey;
    }
}
