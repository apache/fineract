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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.JobDetailHistoryData;
import org.apache.fineract.client.models.JobExecuteRequest;
import org.awaitility.Awaitility;

public class FeignSchedulerHelper {

    private final FineractFeignClient fineractClient;

    public FeignSchedulerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public void stopScheduler() {
        FeignCalls.executeVoid(() -> fineractClient.scheduler().handleCommandsScheduler("stop"));
    }

    public void startScheduler() {
        FeignCalls.executeVoid(() -> fineractClient.scheduler().handleCommandsScheduler("start"));
    }

    public void executeAndAwaitJob(String jobDisplayName) {
        stopScheduler();

        List<GetJobsResponse> allJobs = ok(() -> fineractClient.schedulerJob().retrieveAllSchedulerJobs());
        GetJobsResponse targetJob = allJobs.stream().filter(j -> jobDisplayName.equals(j.getDisplayName())).findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobDisplayName));
        Long jobId = targetJob.getJobId();

        // Capture the last completed run's end time BEFORE triggering execution.
        // Note: GetJobsResponse.lastRunHistory.id is never populated (the read query behind
        // retrieveOneSchedulerJob doesn't select jh.id), so completion must be detected via
        // jobRunEndTime advancing rather than the run history id.
        OffsetDateTime previousRunEndTime = getLastRunEndTime(jobId);

        FeignCalls.executeVoid(() -> fineractClient.schedulerJob().executeJob(jobId, "executeJob", new JobExecuteRequest()));

        Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).pollDelay(Duration.ofSeconds(1)).until(() -> {
            GetJobsResponse job = ok(() -> fineractClient.schedulerJob().retrieveOneSchedulerJob(jobId));
            JobDetailHistoryData history = job.getLastRunHistory();
            if (history == null || history.getJobRunEndTime() == null) {
                return false;
            }
            OffsetDateTime endTime = history.getJobRunEndTime();
            return previousRunEndTime == null || endTime.isAfter(previousRunEndTime);
        });
    }

    private OffsetDateTime getLastRunEndTime(Long jobId) {
        GetJobsResponse job = ok(() -> fineractClient.schedulerJob().retrieveOneSchedulerJob(jobId));
        JobDetailHistoryData history = job.getLastRunHistory();
        return history == null ? null : history.getJobRunEndTime();
    }
}
