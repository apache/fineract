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
package org.apache.fineract.test.data.job;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.services.SchedulerJobApi;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobResolver {

    private final SchedulerJobApi schedulerJobApi;

    @Cacheable(key = "#job.getName()", value = "jobsByName")
    public long resolve(Job job) {
        try {
            String jobName = job.getName();
            log.debug("Resolving job by name [{}]", jobName);
            Response<List<GetJobsResponse>> response = schedulerJobApi.retrieveAll8().execute();
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Unable to get jobs list. Status code was HTTP " + response.code());
            }

            List<GetJobsResponse> jobsResponses = response.body();
            GetJobsResponse foundJob = jobsResponses.stream().filter(j -> jobName.equals(j.getDisplayName())).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Job [%s] not found".formatted(jobName)));
            return foundJob.getJobId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
