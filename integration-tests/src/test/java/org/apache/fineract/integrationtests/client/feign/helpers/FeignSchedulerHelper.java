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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.SchedulerJobApi.RetrieveHistoryQueryParams;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsJobIDJobRunHistoryResponse;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetSchedulerResponse;
import org.apache.fineract.client.models.JobDetailHistoryDataSwagger;
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

    public boolean getSchedulerStatus() {
        GetSchedulerResponse response = ok(() -> fineractClient.scheduler().retrieveSchedulerStatus());
        return Boolean.TRUE.equals(response.getActive());
    }

    public void updateSchedulerStatus(boolean enabled) {
        if (enabled) {
            startScheduler();
        } else {
            stopScheduler();
        }
    }

    public void executeAndAwaitJob(String jobDisplayName) {
        stopScheduler();

        List<GetJobsResponse> allJobs = ok(() -> fineractClient.schedulerJob().retrieveAllSchedulerJobs());
        GetJobsResponse targetJob = allJobs.stream().filter(j -> jobDisplayName.equals(j.getDisplayName())).findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobDisplayName));
        Long jobId = targetJob.getJobId();

        Long previousRunHistoryId = getRunHistoryId(getLatestJobRunHistory(jobId));
        FeignCalls.executeVoid(() -> fineractClient.schedulerJob().executeJob(jobId, "executeJob", new ExecuteJobRequest()));

        JobDetailHistoryDataSwagger finalRunHistory = Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1))
                .pollDelay(Duration.ofSeconds(1))
                .until(() -> getLatestJobRunHistory(jobId), runHistory -> isNewCompletedRunHistory(runHistory, previousRunHistoryId));

        assertEquals("application", finalRunHistory.getTriggerType(),
                "Job was not triggered by the test for jobId=" + jobId + ": " + finalRunHistory);
        String status = finalRunHistory.getStatus();
        if (!"success".equals(status)) {
            fail("Job status is not success for jobId=" + jobId + ": " + finalRunHistory);
        }
    }

    private boolean isNewCompletedRunHistory(JobDetailHistoryDataSwagger latestRunHistory, Long previousRunHistoryId) {
        if (latestRunHistory == null || latestRunHistory.getJobRunEndTime() == null) {
            return false;
        }
        Long runHistoryId = latestRunHistory.getId();
        return runHistoryId != null && (previousRunHistoryId == null || runHistoryId > previousRunHistoryId);
    }

    private Long getRunHistoryId(JobDetailHistoryDataSwagger runHistory) {
        return runHistory == null ? null : runHistory.getId();
    }

    private JobDetailHistoryDataSwagger getLatestJobRunHistory(Long jobId) {
        RetrieveHistoryQueryParams queryParams = new RetrieveHistoryQueryParams().offset(0).limit(1).orderBy("id").sortOrder("DESC");
        GetJobsJobIDJobRunHistoryResponse response = ok(() -> fineractClient.schedulerJob().retrieveHistory(jobId, queryParams));
        List<JobDetailHistoryDataSwagger> pageItems = response.getPageItems();
        if (pageItems == null || pageItems.isEmpty()) {
            return null;
        }
        return pageItems.get(0);
    }
}
