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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SchedulerJobsTest {

    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;
    private Boolean originalSchedulerStatus;
    private final Map<Integer, Boolean> originalJobStatus = new HashMap<>();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        originalSchedulerStatus = schedulerJobHelper.getSchedulerStatus();
        for (Integer jobId : schedulerJobHelper.getAllSchedulerJobIds()) {
            Map<String, Object> schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);
            Boolean active = (Boolean) schedulerJob.get("active");
            originalJobStatus.put(jobId, active);
        }
    }

    @AfterEach
    public void tearDown() {
        schedulerJobHelper.updateSchedulerStatus(originalSchedulerStatus);
        for (int jobId = 1; jobId < JobName.values().length; jobId++) {
            schedulerJobHelper.updateSchedulerJob(jobId, originalJobStatus.get(jobId));
        }
    }

    @Test // FINERACT-926
    public void testDateFormat() {
        // must start scheduler and make job active to have nextRunTime (which
        // is a
        // java.util.Date)
        schedulerJobHelper.updateSchedulerStatus(true);
        schedulerJobHelper.updateSchedulerJob(1, true);
        String nextRunTimeText = await().until(() -> (String) schedulerJobHelper.getSchedulerJobById(1).get("nextRunTime"),
                nextRunTime -> nextRunTime != null);
        DateTimeFormatter.ISO_INSTANT.parse(nextRunTimeText);
    }

    @Test
    @Disabled // TODO FINERACT-1167
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
        for (Integer jobId : schedulerJobHelper.getAllSchedulerJobIds()) {
            // Retrieving Scheduler Job by ID
            Map<String, Object> schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);

            Boolean active = (Boolean) schedulerJob.get("active");
            active = !active;

            // Updating Scheduler Job
            Map<String, Object> changes = schedulerJobHelper.updateSchedulerJob(jobId, active);

            // Verifying Scheduler Job updates
            assertEquals(active, changes.get("active"), "Verifying Scheduler Job Updates");

            schedulerJob = schedulerJobHelper.getSchedulerJobById(jobId);
            assertEquals(active, schedulerJob.get("active"), "Verifying Get Scheduler Job");
        }
    }

    @Test
    public void testTriggeringManualExecutionOfAllSchedulerJobs() {
        for (String jobName : schedulerJobHelper.getAllSchedulerJobNames()) {
            schedulerJobHelper.executeAndAwaitJob(jobName);
        }
    }
}
