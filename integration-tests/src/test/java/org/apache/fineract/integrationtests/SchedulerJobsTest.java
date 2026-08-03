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
package org.apache.fineract.integrationtests;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.ParallelExecutionHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class SchedulerJobsTest {

    private final Map<Integer, Boolean> originalJobStatus = new ConcurrentHashMap<>();
    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;
    private Boolean originalSchedulerStatus;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        originalSchedulerStatus = schedulerJobHelper.getSchedulerStatus();
        ParallelExecutionHelper.runInParallel(schedulerJobHelper.getAllSchedulerJobIds(), (jobId) -> {
            GetJobsResponse schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);
            originalJobStatus.put(jobId, schedulerJob.getActive());
        });
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @AfterEach
    public void tearDown() {
        schedulerJobHelper.updateSchedulerStatus(originalSchedulerStatus);
        ParallelExecutionHelper.runInParallel(schedulerJobHelper.getAllSchedulerJobIds(),
                (jobId) -> schedulerJobHelper.updateSchedulerJob(jobId, originalJobStatus.get(jobId)));
    }

    @Test // FINERACT-926
    public void testDateFormat() {
        // must start scheduler and make job active to have nextRunTime (which
        // is a
        // java.util.Date)
        schedulerJobHelper.updateSchedulerStatus(true);
        int minJobId = schedulerJobHelper.getAllSchedulerJobIds().stream().mapToInt(number -> number).min().orElse(Integer.MAX_VALUE);
        schedulerJobHelper.updateSchedulerJob(minJobId, true);
        // Feign already deserializes nextRunTime into a typed OffsetDateTime, so a malformed date would fail here
        // with a deserialization error rather than needing to be manually parsed.
        await().until(() -> schedulerJobHelper.getSchedulerJobById(minJobId).getNextRunTime(), Objects::nonNull);
    }

    @Test
    public void testFlippingSchedulerStatus() throws InterruptedException {
        // Retrieving Status of Scheduler
        Boolean schedulerStatus = schedulerJobHelper.getSchedulerStatus();
        if (schedulerStatus == true) {
            schedulerJobHelper.updateSchedulerStatus(false);
            schedulerStatus = schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after stopping
            assertEquals(false, schedulerStatus, "Verifying Scheduler Job Status");
        } else {
            schedulerJobHelper.updateSchedulerStatus(true);
            schedulerStatus = schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after starting
            assertEquals(true, schedulerStatus, "Verifying Scheduler Job Status");
        }
    }

    @Test
    public void testNumberOfJobs() {
        List<Integer> jobIds = schedulerJobHelper.getAllSchedulerJobIds();
        assertEquals(JobName.values().length, jobIds.size(), "Number of jobs in database and code do not match: " + jobIds);
    }

    @Test
    public void testFlippingJobsActiveStatus() throws InterruptedException {
        // Stop the Scheduler while we test flapping jobs' active on/off, to
        // avoid side
        // effects
        schedulerJobHelper.updateSchedulerStatus(false);

        // For each retrieved scheduled job (by ID)...
        ParallelExecutionHelper.runInParallel(schedulerJobHelper.getAllSchedulerJobIds(), this::updateJobStatus);
    }

    private void updateJobStatus(Integer jobId) {
        // Retrieving Scheduler Job by ID
        GetJobsResponse schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);

        boolean active = !schedulerJob.getActive();

        // Updating Scheduler Job
        Map<String, Object> changes = schedulerJobHelper.updateSchedulerJob(jobId, active);

        // Verifying Scheduler Job updates
        assertEquals(active, changes.get("active"), "Verifying Scheduler Job Updates");

        schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);
        assertEquals(active, schedulerJob.getActive(), "Verifying Get Scheduler Job");
    }

    @Test
    public void testTriggeringManualExecutionOfAllSchedulerJobs() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            ParallelExecutionHelper.runInParallel(schedulerJobHelper.getAllSchedulerJobNames(), schedulerJobHelper::executeAndAwaitJob);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }
}
