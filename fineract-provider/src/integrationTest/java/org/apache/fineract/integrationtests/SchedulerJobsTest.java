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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
public class SchedulerJobsTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpecForSchedulerJob;
    private SchedulerJobHelper schedulerJobHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForSchedulerJob = new ResponseSpecBuilder().expectStatusCode(202).build();
    }

    @Test
    public void testSchedulerJobs() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        // Retrieving Status of Scheduler
        HashMap schedulerStatus = this.schedulerJobHelper.getSchedulerStatus(this.requestSpec, this.responseSpec);
        Boolean status = (Boolean) schedulerStatus.get("active");
        if (status == true) {
            this.schedulerJobHelper.updateSchedulerStatus(this.requestSpec, this.responseSpecForSchedulerJob, "stop");
            schedulerStatus = this.schedulerJobHelper.getSchedulerStatus(this.requestSpec, this.responseSpec);
            // Verifying Status of the Scheduler after updation
            Assert.assertEquals("Verifying Scheduler Job Status", false, schedulerStatus.get("active"));
        } else {
            this.schedulerJobHelper.updateSchedulerStatus(this.requestSpec, this.responseSpecForSchedulerJob, "start");
            schedulerStatus = this.schedulerJobHelper.getSchedulerStatus(this.requestSpec, this.responseSpec);
            // Verifying Status of the Scheduler after updation
            Assert.assertEquals("Verifying Scheduler Job Status", true, schedulerStatus.get("active"));
        }

        // Retrieving All Scheduler Jobs
        ArrayList<HashMap> allSchedulerJobsData = this.schedulerJobHelper.getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allSchedulerJobsData);

        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {

            Integer jobId = (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");

            // Retrieving Scheduler Job by ID
            HashMap schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            Assert.assertNotNull(schedulerJob);

            Boolean active = (Boolean) schedulerJob.get("active");

            if (active == true) {
                active = false;
            } else {
                active = true;
            }

            // Updating Scheduler Job
            HashMap changes = this.schedulerJobHelper.updateSchedulerJob(this.requestSpec, this.responseSpec, jobId.toString(),
                    active.toString());
            // Verifying Scheduler Job updation
            Assert.assertEquals("Verifying Scheduler Job Updation", active, changes.get("active"));

            // Executing Scheduler Job
            this.schedulerJobHelper.runSchedulerJob(this.requestSpec, jobId.toString());

            // Retrieving Scheduler Job by ID
            schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
            Assert.assertNotNull(schedulerJob);

            // Waiting for Job to complete
            while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
                Thread.sleep(15000);
                schedulerJob = this.schedulerJobHelper.getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
                Assert.assertNotNull(schedulerJob);
                System.out.println("Job is Still Running");
            }
            ArrayList<HashMap> jobHistoryData = this.schedulerJobHelper.getSchedulerJobHistory(this.requestSpec, this.responseSpec,
                    jobId.toString());

            // Verifying the Status of the Recently executed Scheduler Job
            Assert.assertEquals("Verifying Last Scheduler Job Status", "success",
                    jobHistoryData.get(jobHistoryData.size() - 1).get("status"));
        }

    }

}