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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
public class SchedulerJobsTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
    }

    @Test
    public void testGetAndUpdateSchedulerStatus() throws InterruptedException {
        // Retrieving Status of Scheduler
        Boolean schedulerStatus = this.schedulerJobHelper.getSchedulerStatus();
        if (schedulerStatus == true) {
            this.schedulerJobHelper.updateSchedulerStatus("stop");
            schedulerStatus = this.schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after stopping
            assertEquals("Verifying Scheduler Job Status", false, schedulerStatus);
        } else {
            this.schedulerJobHelper.updateSchedulerStatus("start");
            schedulerStatus = this.schedulerJobHelper.getSchedulerStatus();
            // Verifying Status of the Scheduler after starting
            assertEquals("Verifying Scheduler Job Status", true, schedulerStatus);
        }
    }

    @Test
    @Ignore // TODO FINERACT-852
    public void testSchedulerJobs() throws InterruptedException {
        // Retrieving All Scheduler Jobs
        List<Map> allSchedulerJobsData = this.schedulerJobHelper.getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        assertNotNull(allSchedulerJobsData);

        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {

            Integer jobId = (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");

            // Retrieving Scheduler Job by ID
            Map schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            assertNotNull(schedulerJob);

            Boolean active = (Boolean) schedulerJob.get("active");

            if (active == true) {
                active = false;
            } else {
                active = true;
            }

            // Updating Scheduler Job
            Map changes = this.schedulerJobHelper.updateSchedulerJob(this.requestSpec, this.responseSpec, jobId.toString(),
                    active.toString());
            // Verifying Scheduler Job updates
            assertEquals("Verifying Scheduler Job Updates", active, changes.get("active"));

            // Executing Scheduler Job
            this.schedulerJobHelper.runSchedulerJob(this.requestSpec, jobId.toString());

            // Retrieving Scheduler Job by ID
            schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            assertNotNull(schedulerJob);

            // Waiting for Job to complete
            while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
                Thread.sleep(15000);
                schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
                assertNotNull(schedulerJob);
                System.out.println("Job " +jobId.toString() +" is Still Running");
            }
            List<Map> jobHistoryData = this.schedulerJobHelper.getSchedulerJobHistory(this.requestSpec, this.responseSpec,
                    jobId.toString());

            // Verifying the Status of the Recently executed Scheduler Job
            assertFalse("Job History is empty :(  Was it too slow? Failures in background job?", jobHistoryData.isEmpty());
            assertEquals("Verifying Last Scheduler Job Status", "success",
                    jobHistoryData.get(jobHistoryData.size() - 1).get("status"));
        }
    }
}