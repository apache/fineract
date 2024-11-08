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
package org.apache.fineract.test.service;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.services.SchedulerJobApi;
import org.apache.fineract.test.data.job.Job;
import org.apache.fineract.test.data.job.JobResolver;
import org.apache.fineract.test.helper.ErrorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobService {

    @Autowired
    private SchedulerJobApi schedulerJobApi;

    private final JobResolver jobResolver;

    public void execute(Job job) {
        try {
            Long jobId = jobResolver.resolve(job);
            Response<Void> response = schedulerJobApi.executeJob(jobId, "executeJob", new ExecuteJobRequest()).execute();
            ErrorHelper.checkSuccessfulApiCall(response);
        } catch (IOException e) {
            throw new RuntimeException("Exception while executing job %s".formatted(job.getName()), e);
        }
    }

    public void executeAndWait(Job job) {
        execute(job);
        waitUntilJobIsFinished(job);
    }

    private void waitUntilJobIsFinished(Job job) {
        String jobName = job.getName();
        await().atMost(Duration.ofMinutes(2)) //
                .alias("%s didn't finish on time".formatted(jobName)) //
                .pollInterval(Duration.ofSeconds(5)) //
                .pollDelay(Duration.ofSeconds(5)) //
                .until(() -> {
                    log.info("Waiting for job {} to finish", jobName);
                    Long jobId = jobResolver.resolve(job);
                    Response<GetJobsResponse> getJobsResponse = schedulerJobApi.retrieveOne5(jobId).execute();
                    ErrorHelper.checkSuccessfulApiCall(getJobsResponse);
                    Boolean currentlyRunning = getJobsResponse.body().getCurrentlyRunning();
                    return BooleanUtils.isFalse(currentlyRunning);
                });
    }
}
