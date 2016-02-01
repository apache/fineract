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
package org.apache.fineract.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;

import com.google.gson.Gson;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SchedulerJobHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public SchedulerJobHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList getAllSchedulerJobs(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_ALL_SCHEDULER_JOBS_URL = "/fineract-provider/api/v1/jobs?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        final ArrayList response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_SCHEDULER_JOBS_URL, "");
        return response;
    }

    public static HashMap getSchedulerJobById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        return response;
    }

    public static HashMap getSchedulerStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_SCHEDULER_STATUS_URL, "");
        return response;
    }

    public static void updateSchedulerStatus(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String command) {
        final String UPDATE_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER STATUS -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, UPDATE_SCHEDULER_STATUS_URL, runSchedulerJobAsJSON(), null);
    }

    public static HashMap updateSchedulerJob(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jobId, final String active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final HashMap response = Utils.performServerPut(requestSpec, responseSpec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    public static String updateSchedulerJobAsJSON(final String active) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("active", active);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static ArrayList getSchedulerJobHistory(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String jobId) {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/jobs/" + jobId + "/runhistory?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB HISTORY -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_SCHEDULER_STATUS_URL, "");
        return (ArrayList) response.get("pageItems");
    }

    public static void runSchedulerJob(final RequestSpecification requestSpec, final String jobId) {
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?command=executeJob&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    public static String runSchedulerJobAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        System.out.println(runSchedulerJob);
        return runSchedulerJob;
    }

    public void executeJob(String JobName) throws InterruptedException {
        ArrayList<HashMap> allSchedulerJobsData = getAllSchedulerJobs(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(allSchedulerJobsData);

        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(JobName)) {
                Integer jobId = (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");

                // Executing Scheduler Job
                runSchedulerJob(this.requestSpec, jobId.toString());

                // Retrieving Scheduler Job by ID
                HashMap schedulerJob = getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
                Assert.assertNotNull(schedulerJob);

                // Waiting for Job to complete
                while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
                    Thread.sleep(15000);
                    schedulerJob = getSchedulerJobById(this.requestSpec, this.responseSpec, jobId.toString());
                    Assert.assertNotNull(schedulerJob);
                    System.out.println("Job is Still Running");
                }

                ArrayList<HashMap> jobHistoryData = getSchedulerJobHistory(this.requestSpec, this.responseSpec, jobId.toString());

                // print error associated with recent job failure (if any)
                System.out.println("Job run error message (printed only if the job fails: "
                        + jobHistoryData.get(jobHistoryData.size() - 1).get("jobRunErrorMessage"));
                System.out.println("Job failure error log (printed only if the job fails: "
                        + jobHistoryData.get(jobHistoryData.size() - 1).get("jobRunErrorLog"));

                // Verifying the Status of the Recently executed Scheduler Job
                Assert.assertEquals("Verifying Last Scheduler Job Status", "success",
                        jobHistoryData.get(jobHistoryData.size() - 1).get("status"));

                break;
            }
        }
    }
}