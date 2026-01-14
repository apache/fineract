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
package org.apache.fineract.test.initializer.suite;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.PutJobsJobIDRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobSuiteInitializerStep implements FineractSuiteInitializerStep {

    public static final String SEND_ASYNCHRONOUS_EVENTS_JOB_NAME = "Send Asynchronous Events";
    public static final String EVERY_1_SECONDS = "0/1 * * * * ?";
    public static final String EVERY_60_SECONDS = "0 0/1 * * * ?";

    private final FineractFeignClient fineractClient;

    public JobSuiteInitializerStep(FineractFeignClient fineractClient) {
        log.debug("=== JobSuiteInitializerStep: Constructor called - bean is being created ===");
        this.fineractClient = fineractClient;
        log.debug("=== JobSuiteInitializerStep: FineractFeignClient injected successfully ===");
    }

    @Override
    public void initializeForSuite() throws InterruptedException {
        log.debug("=== JobSuiteInitializerStep.initializeForSuite() - START ===");
        enableAndExecuteEventJob();
        log.debug("=== JobSuiteInitializerStep.initializeForSuite() - COMPLETED successfully ===");
    }

    private void enableAndExecuteEventJob() throws InterruptedException {
        log.debug("=== Initializing Send Asynchronous Events job ===");
        Long jobId = updateExternalEventJobFrequency(EVERY_1_SECONDS);
        log.debug("=== Updated cron expression to EVERY_1_SECONDS ===");

        // CRITICAL: SchedulerGlobalInitializerStep stops the scheduler globally
        // Solution: START the scheduler so the job runs every 1 second automatically
        log.debug("Starting scheduler to enable automatic job execution every 1 second...");
        executeVoid(() -> fineractClient.scheduler().changeSchedulerStatus("start", Map.of()));
        log.debug("Scheduler started successfully");

        // Manually execute once immediately to publish any queued events from initialization
        log.debug("Manually executing '{}' job once to publish queued events...", SEND_ASYNCHRONOUS_EVENTS_JOB_NAME);
        executeVoid(() -> fineractClient.schedulerJob().executeJob(jobId, new ExecuteJobRequest(), Map.of("command", "executeJob")));

        // Poll job history to confirm it ran
        log.debug("Polling job history to confirm initial execution...");
        Long initialRunCount = getJobRunCount(jobId);
        log.debug("Initial job run count: {}", initialRunCount);

        boolean jobRan = false;
        for (int i = 0; i < 30; i++) {
            Thread.sleep(200);
            Long currentRunCount = getJobRunCount(jobId);
            if (currentRunCount > initialRunCount) {
                log.debug("Job execution confirmed! Run count increased from {} to {}", initialRunCount, currentRunCount);
                jobRan = true;
                break;
            }
        }

        if (!jobRan) {
            log.warn("WARNING: Job execution could not be confirmed via history polling");
        }

        // Wait for events to propagate to ActiveMQ
        log.debug("Waiting 1 second for event propagation to ActiveMQ...");
        Thread.sleep(1000);
        log.debug("Scheduler is now running - job will execute every 1 second automatically");
    }

    private Long getJobRunCount(Long jobId) {
        try {
            var history = ok(() -> fineractClient.schedulerJob().retrieveHistory(jobId, Map.of()));
            return (long) history.getTotalFilteredRecords();
        } catch (Exception e) {
            log.warn("Failed to retrieve job history: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public void resetAfterSuite() {
        log.debug("=== JobSuiteInitializerStep.resetAfterSuite() - START ===");

        // Stop the scheduler to prevent jobs from running between test suites
        log.debug("Stopping scheduler...");
        try {
            executeVoid(() -> fineractClient.scheduler().changeSchedulerStatus(Map.of("command", "stop")));
            log.debug("Scheduler stopped successfully");
        } catch (Exception e) {
            log.warn("Failed to stop scheduler: {}", e.getMessage());
        }

        // Reset cron expression to default
        updateExternalEventJobFrequency(EVERY_60_SECONDS);
        log.debug("=== JobSuiteInitializerStep.resetAfterSuite() - COMPLETED ===");
    }

    private Long updateExternalEventJobFrequency(String cronExpression) {
        GetJobsResponse externalEventJobResponse = ok(() -> fineractClient.schedulerJob().retrieveAll8()).stream()
                .filter(r -> r.getDisplayName().equals(SEND_ASYNCHRONOUS_EVENTS_JOB_NAME)).findAny()
                .orElseThrow(() -> new IllegalStateException(SEND_ASYNCHRONOUS_EVENTS_JOB_NAME + " is not found"));
        Long jobId = externalEventJobResponse.getJobId();
        executeVoid(() -> fineractClient.schedulerJob().updateJobDetail(jobId, new PutJobsJobIDRequest().cronExpression(cronExpression)));
        return jobId;
    }
}
