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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.test.data.job.Job;
import org.apache.fineract.test.data.job.JobResolver;
import org.apache.fineract.test.messaging.config.JobPollingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobService {

    @Autowired
    private FineractFeignClient fineractClient;

    @Autowired
    private JobPollingProperties jobPollingProperties;

    private final JobResolver jobResolver;

    public void execute(Job job) {
        Long jobId = jobResolver.resolve(job);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("command", "executeJob");
        executeVoid(() -> fineractClient.schedulerJob().executeJob(jobId, new ExecuteJobRequest(), queryParams));
    }

    public void executeAndWait(Job job) {
        execute(job);
        waitUntilJobIsFinished(job);
    }

    private void waitUntilJobIsFinished(Job job) {
        String jobName = job.getName();
        await().atMost(Duration.ofMillis(jobPollingProperties.getTimeoutInMillis())) //
                .alias("%s didn't finish on time".formatted(jobName)) //
                .pollInterval(Duration.ofMillis(jobPollingProperties.getIntervalInMillis())) //
                .pollDelay(Duration.ofMillis(jobPollingProperties.getDelayInMillis())) //
                .until(() -> {
                    log.debug("Waiting for job {} to finish", jobName);
                    Long jobId = jobResolver.resolve(job);
                    GetJobsResponse getJobsResponse = ok(() -> fineractClient.schedulerJob().retrieveOne5(jobId));
                    Boolean currentlyRunning = getJobsResponse.getCurrentlyRunning();
                    return BooleanUtils.isFalse(currentlyRunning);
                });
    }
}
