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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistoryRepository;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetailRepository;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameData;
import org.apache.fineract.infrastructure.jobs.service.jobname.JobNameService;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Wires the real {@link JobRegisterServiceImpl}, {@link JobSchedulerServiceImpl}, {@link SchedulerVetoer},
 * {@link SchedulerTriggerListener}, {@link SchedulerJobListener} and a real in-memory Quartz scheduler. Only the job
 * table, the scheduler_detail row and the Spring Batch launch are replaced by in-memory stand-ins, so the tests observe
 * what Quartz really fires and what the veto and the listener really do to {@code next_run_time}.
 */
final class MissedRunsFixture implements AutoCloseable {

    private static final AtomicLong TENANT_IDS = new AtomicLong(7000);
    private static final AtomicLong JOB_IDS = new AtomicLong(1);

    final FineractPlatformTenant tenant;
    final FineractProperties properties = new FineractProperties();
    final SchedulerDetail schedulerDetail = new SchedulerDetail();
    final List<ScheduledJobDetail> jobs = new CopyOnWriteArrayList<>();
    final JobRegisterServiceImpl jobRegisterService = new JobRegisterServiceImpl();
    final JobSchedulerServiceImpl jobSchedulerService;
    final Map<String, List<Instant>> executions = new ConcurrentHashMap<>();
    final AtomicInteger concurrentExecutions = new AtomicInteger();
    final AtomicInteger maxConcurrentExecutions = new AtomicInteger();
    volatile Consumer<ScheduledJobDetail> jobBody = job -> { // by default the job does nothing
    };

    private final java.util.TimeZone systemZone = java.util.TimeZone.getDefault();

    MissedRunsFixture(boolean executeMissedRuns) throws Exception {
        this(executeMissedRuns, "UTC");
    }

    MissedRunsFixture(boolean executeMissedRuns, String tenantZone) throws Exception {
        final long tenantId = TENANT_IDS.incrementAndGet();
        tenant = new FineractPlatformTenant(tenantId, "tenant" + tenantId, "Tenant " + tenantId, tenantZone, null);
        ThreadLocalContextUtil.setTenant(tenant);

        properties.setNodeId("1");
        final FineractProperties.FineractModeProperties mode = new FineractProperties.FineractModeProperties();
        mode.setBatchManagerEnabled(true);
        properties.setMode(mode);
        final FineractProperties.FineractJobProperties jobProperties = new FineractProperties.FineractJobProperties();
        jobProperties.setExecuteMissedRuns(executeMissedRuns);
        properties.setJob(jobProperties);

        final ScheduledJobDetailRepository jobRepository = mock(ScheduledJobDetailRepository.class);
        when(jobRepository.findAllJobs(anyInt())).thenAnswer(inv -> List.copyOf(jobs));
        when(jobRepository.findByJobKey(anyString())).thenAnswer(inv -> byJobKey(inv.getArgument(0)));
        when(jobRepository.findByJobKeyWithLock(anyString())).thenAnswer(inv -> byJobKey(inv.getArgument(0)));
        when(jobRepository.save(any(ScheduledJobDetail.class))).thenAnswer(inv -> inv.getArgument(0));
        final ScheduledJobRunHistoryRepository historyRepository = mock(ScheduledJobRunHistoryRepository.class);
        final SchedulerDetailRepository schedulerDetailRepository = mock(SchedulerDetailRepository.class);
        when(schedulerDetailRepository.findAllSchedulerDetails()).thenAnswer(inv -> List.of(schedulerDetail));
        final SchedularWritePlatformService writeService = new SchedularWritePlatformServiceJpaRepositoryImpl(jobRepository,
                historyRepository, null, schedulerDetailRepository);

        final BusinessDateReadPlatformService businessDateService = mock(BusinessDateReadPlatformService.class);
        when(businessDateService.getBusinessDates()).thenAnswer(inv -> new HashMap<BusinessDateType, LocalDate>());
        final TenantDetailsService tenantDetailsService = mock(TenantDetailsService.class);
        when(tenantDetailsService.loadTenantById(tenant.getTenantIdentifier())).thenReturn(tenant);
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of(tenant));

        final JobNameService jobNameService = mock(JobNameService.class);
        when(jobNameService.getJobByHumanReadableName(anyString())).thenAnswer(inv -> {
            final String name = inv.getArgument(0);
            final JobName jobName = Arrays.stream(JobName.values()).filter(candidate -> candidate.toString().equals(name)).findFirst()
                    .orElseThrow();
            return new JobNameData(jobName.name(), jobName.toString());
        });
        final JobRegistry jobRegistry = mock(JobRegistry.class);
        when(jobRegistry.getJob(anyString())).thenReturn(mock(Job.class));

        final JobStarter jobStarter = mock(JobStarter.class);
        doAnswer(inv -> {
            final ScheduledJobDetail job = inv.getArgument(1);
            final int running = concurrentExecutions.incrementAndGet();
            maxConcurrentExecutions.accumulateAndGet(running, Math::max);
            try {
                executions.computeIfAbsent(job.getJobName(), name -> new CopyOnWriteArrayList<>()).add(Instant.now());
                jobBody.accept(job);
            } finally {
                concurrentExecutions.decrementAndGet();
            }
            return null;
        }).when(jobStarter).run(any(), any(), any(), anyString());

        final SchedulerVetoer vetoer = new SchedulerVetoer(writeService, businessDateService);
        ReflectionTestUtils.setField(jobRegisterService, "schedularWritePlatformService", writeService);
        ReflectionTestUtils.setField(jobRegisterService, "schedulerJobListener",
                new SchedulerJobListener(writeService, tenantDetailsService));
        ReflectionTestUtils.setField(jobRegisterService, "globalSchedulerTriggerListener",
                new SchedulerTriggerListener(tenantDetailsService, vetoer));
        ReflectionTestUtils.setField(jobRegisterService, "fineractProperties", properties);
        ReflectionTestUtils.setField(jobRegisterService, "jobRegistry", jobRegistry);
        ReflectionTestUtils.setField(jobRegisterService, "jobStarter", jobStarter);
        ReflectionTestUtils.setField(jobRegisterService, "jobNameService", jobNameService);

        jobSchedulerService = new JobSchedulerServiceImpl(properties, writeService, tenantDetailsService, jobRegisterService,
                businessDateService);
    }

    ScheduledJobDetail addJob(JobName jobName, String cronExpression, Date nextRunTime) {
        return addJob(jobName, cronExpression, nextRunTime, 5, 0);
    }

    /** Jobs are registered in the order they are added, as {@code findAllJobs} returns them. */
    ScheduledJobDetail addJob(JobName jobName, String cronExpression, Date nextRunTime, int taskPriority, int schedulerGroup) {
        final ScheduledJobDetail job = new ScheduledJobDetail();
        job.setId(JOB_IDS.getAndIncrement());
        job.setJobName(jobName.toString());
        job.setShortName(jobName.name());
        job.setGroupName("group");
        job.setCronExpression(cronExpression);
        job.setTaskPriority((short) taskPriority);
        job.setSchedulerGroup((short) schedulerGroup);
        job.setNodeId(0);
        job.setActiveSchedular(true);
        job.setNextRunTime(nextRunTime);
        jobs.add(job);
        return job;
    }

    /** A cron expression that fires every day at midnight, and the midnight {@code daysAgo} days back (UTC). */
    static final String DAILY_AT_MIDNIGHT = "0 0 0 * * ?";

    static Date midnightDaysAgo(int daysAgo) {
        return Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS).minus(daysAgo, ChronoUnit.DAYS));
    }

    /** Number of midnights from {@code daysAgo} days back up to and including today's: what a daily job missed. */
    static int missedDailyRuns(int daysAgo) {
        return daysAgo + 1;
    }

    void boot() {
        jobSchedulerService.onApplicationEvent(null);
    }

    /** Runs {@code action} the way a REST call would: with the tenant bound to the thread. */
    void asRequest(Runnable action) {
        ThreadLocalContextUtil.setTenant(tenant);
        try {
            action.run();
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

    int executionCount(JobName jobName) {
        return executions.getOrDefault(jobName.toString(), List.of()).size();
    }

    /** The JVM default zone: what the column mapping of a Date uses. Restored on close. */
    void setSystemZone(String zoneId) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(zoneId));
    }

    @Override
    public void close() {
        java.util.TimeZone.setDefault(systemZone);
        jobRegisterService.stopAllSchedulers();
        ThreadLocalContextUtil.reset();
    }

    private ScheduledJobDetail byJobKey(String jobKey) {
        return jobs.stream().filter(job -> jobKey.equals(job.getJobKey())).findFirst().orElseThrow();
    }
}
