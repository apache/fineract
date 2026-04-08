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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.JobDetailHistoryData;
import org.awaitility.Awaitility;

public class FeignSchedulerHelper {

    private final FineractFeignClient fineractClient;

    public FeignSchedulerHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public void stopScheduler() {
        FeignCalls.executeVoid(() -> fineractClient.scheduler().changeSchedulerStatus("stop"));
    }

    public void startScheduler() {
        FeignCalls.executeVoid(() -> fineractClient.scheduler().changeSchedulerStatus("start"));
    }

    public void executeAndAwaitJob(String jobDisplayName) {
        stopScheduler();

        List<GetJobsResponse> allJobs = ok(() -> fineractClient.schedulerJob().retrieveAllSchedulerJobs());
        GetJobsResponse targetJob = allJobs.stream().filter(j -> jobDisplayName.equals(j.getDisplayName())).findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobDisplayName));

        Instant beforeExecuteTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        FeignCalls.executeVoid(() -> fineractClient.schedulerJob().executeJob(targetJob.getJobId(), "executeJob", new ExecuteJobRequest()));

        Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).pollDelay(Duration.ofSeconds(1)).until(() -> {
            GetJobsResponse job = ok(() -> fineractClient.schedulerJob().retrieveOneSchedulerJob(targetJob.getJobId()));
            JobDetailHistoryData history = job.getLastRunHistory();
            if (history == null || history.getJobRunStartTime() == null) {
                return false;
            }
            OffsetDateTime startTime = history.getJobRunStartTime();
            if (startTime.toInstant().isBefore(beforeExecuteTime)) {
                return false;
            }
            return history.getJobRunEndTime() != null && !history.getJobRunEndTime().toInstant().isBefore(startTime.toInstant());
        });
    }
}
