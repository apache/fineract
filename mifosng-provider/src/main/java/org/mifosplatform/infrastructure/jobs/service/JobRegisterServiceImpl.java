package org.mifosplatform.infrastructure.jobs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.exception.PlatformInternalServerException;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.jobs.annotation.CronMethodParser;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.mifosplatform.infrastructure.jobs.domain.SchedulerDetail;
import org.mifosplatform.infrastructure.jobs.exception.JobNotFoundException;
import org.mifosplatform.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
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
        final List<MifosPlatformTenant> allTenants = this.tenantDetailsService.findAllTenants();
        for (final MifosPlatformTenant tenant : allTenants) {
            ThreadLocalContextUtil.setTenant(tenant);
            final List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService.retrieveAllJobs();
            for (final ScheduledJobDetail jobDetails : scheduledJobDetails) {
                scheduleJob(jobDetails);
                jobDetails.updateTriggerMisfired(false);
                this.schedularWritePlatformService.saveOrUpdate(jobDetails);
            }
            final SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
            if (schedulerDetail.isResetSchedulerOnBootup()) {
                schedulerDetail.updateSuspendedState(false);
                this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            }
        }
    }

    public void executeJob(final ScheduledJobDetail scheduledJobDetail, String triggerType) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (triggerType == null) {
                triggerType = SchedulerServiceConstants.TRIGGER_TYPE_APPLICATION;
            }
            jobDataMap.put(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE, triggerType);
            jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            final String key = scheduledJobDetail.getJobKey();
            final JobKey jobKey = constructJobKey(key);
            final String schedulerName = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = this.schedulers.get(schedulerName);
            if (scheduler == null || !scheduler.checkExists(jobKey)) {
                final JobDetail jobDetail = createJobDetail(scheduledJobDetail);
                final String tempSchedulerName = "temp" + scheduledJobDetail.getId();
                final List<Class<? extends JobListener>> listenerClasses = new ArrayList<Class<? extends JobListener>>(2);
                listenerClasses.add(SchedulerJobListener.class);
                listenerClasses.add(SchedulerStopListener.class);
                final Scheduler tempScheduler = createScheduler(tempSchedulerName, 1, listenerClasses);
                tempScheduler.addJob(jobDetail, true);
                jobDataMap.put(SchedulerServiceConstants.SCHEDULER_NAME, tempSchedulerName);
                this.schedulers.put(tempSchedulerName, tempScheduler);
                tempScheduler.triggerJob(jobDetail.getKey(), jobDataMap);
            } else {
                scheduler.triggerJob(jobKey, jobDataMap);
            }

        } catch (final Exception e) {
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", "Job execution failed for job with id:"
                    + scheduledJobDetail.getId(), scheduledJobDetail.getId());
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
                final List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService.retrieveAllJobs();
                for (final ScheduledJobDetail jobDetail : scheduledJobDetails) {
                    if (jobDetail.isTriggerMisfired()) {
                        if (jobDetail.isActiveSchedular()) {
                            executeJob(jobDetail, SchedulerServiceConstants.TRIGGER_TYPE_CRON);
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
                                logger.error(e.getMessage(), e);
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
        rescheduleJob(scheduledJobDetail);
    }

    @Override
    public void executeJob(final Long jobId) {
        final ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        if (scheduledJobDetail == null) { throw new JobNotFoundException(String.valueOf(jobId)); }
        executeJob(scheduledJobDetail, null);
    }

    @Override
    public boolean isSchedulerRunning() {
        return !this.schedularWritePlatformService.retriveSchedulerDetail().isSuspended();
    }

    private void scheduleJob(final ScheduledJobDetail scheduledJobDetails) {
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
        }
        scheduledJobDetails.updateCurrentlyRunningStatus(false);
    }

    private Scheduler getScheduler(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final String schedulername = getSchedulerName(scheduledJobDetail);
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
    public void stopScheduler(final String name) {
        final Scheduler scheduler = this.schedulers.remove(name);
        try {
            scheduler.shutdown();
        } catch (final SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSchedulerName(final ScheduledJobDetail scheduledJobDetail) {
        final StringBuilder sb = new StringBuilder(20);
        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        sb.append(SchedulerServiceConstants.SCHEDULER).append(tenant.getId());
        if (scheduledJobDetail.getSchedulerGroup() > 0) {
            sb.append(SchedulerServiceConstants.SCHEDULER_GROUP).append(scheduledJobDetail.getSchedulerGroup());
        }
        return sb.toString();
    }

    private Scheduler createScheduler(final String name, final int noOfThreads, List<Class<? extends JobListener>> listenerClasses)
            throws Exception {
        final SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(name);
        if (listenerClasses == null) {
            listenerClasses = new ArrayList<Class<? extends JobListener>>(1);
            listenerClasses.add(SchedulerJobListener.class);
        }
        schedulerFactoryBean.setGlobalJobListeners(getGlobalListener(listenerClasses));
        final TriggerListener globalTriggerListener = this.applicationContext.getBean("schedulerTriggerListener", TriggerListener.class);
        final TriggerListener[] globalTriggerListeners = { globalTriggerListener };
        schedulerFactoryBean.setGlobalTriggerListeners(globalTriggerListeners);
        final Properties quartzProperties = new Properties();
        quartzProperties.put(SchedulerFactoryBean.PROP_THREAD_COUNT, Integer.toString(noOfThreads));
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        return schedulerFactoryBean.getScheduler();
    }

    private JobListener[] getGlobalListener(final List<Class<? extends JobListener>> listenerClasses) throws ClassNotFoundException {
        final List<JobListener> listeners = new ArrayList<JobListener>(listenerClasses.size());
        for (final Class<?> listenerClass : listenerClasses) {
            final JobListener listener = (JobListener) getBeanObject(listenerClass);
            listeners.add(listener);
        }
        final JobListener[] listenerArray = new JobListener[listeners.size()];
        return listeners.toArray(listenerArray);
    }

    private JobDetail createJobDetail(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        final String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetail.getJobName());
        final Object targetObject = getBeanObject(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
        final MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
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
    private Object getBeanObject(final Class<?> classType) throws ClassNotFoundException {
        final List<Class<?>> typesList = new ArrayList<Class<?>>();
        final Class<?>[] interfaceType = classType.getInterfaces();
        if (interfaceType.length > 0) {
            typesList.addAll(Arrays.asList(interfaceType));
        } else {
            Class<?> superclassType = classType;
            while (!Object.class.getName().equals(superclassType.getSuperclass().getName())) {
                superclassType = superclassType.getSuperclass();
            }
            typesList.add(superclassType);
        }
        final List<String> beanNames = new ArrayList<String>();
        for (final Class<?> clazz : typesList) {
            beanNames.addAll(Arrays.asList(this.applicationContext.getBeanNamesForType(clazz)));
        }
        Object targetObject = null;
        for (final String beanName : beanNames) {
            final Object nextObject = this.applicationContext.getBean(beanName);
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
        final MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
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
        final StringBuffer sb = new StringBuffer(throwable.toString());
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
        final String[] keyParams = Key.split(SchedulerServiceConstants.JOB_KEY_SEPERATOR);
        final JobKey JobKey = new JobKey(keyParams[0], keyParams[1]);
        return JobKey;
    }
}
