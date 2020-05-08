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

import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import org.junit.Assert;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SchedulerJobHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification response200Spec;
    private final ResponseSpecification response202Spec;

    public SchedulerJobHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.response202Spec = new ResponseSpecBuilder().expectStatusCode(202).build();
    }

    public SchedulerJobHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = responseSpec;
        this.response202Spec = responseSpec;
    }

    private List getAllSchedulerJobs() {
        final String GET_ALL_SCHEDULER_JOBS_URL = "/fineract-provider/api/v1/jobs?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        final ArrayList response = Utils.performServerGet(requestSpec, response200Spec, GET_ALL_SCHEDULER_JOBS_URL, "");
        return response;
    }

    public List<Integer> getAllSchedulerJobIds() {
        ToIntFunction<Map> mapper = map -> (Integer) map.get("jobId");
        return getAllSchedulerJobs().stream().mapToInt(mapper).boxed().collect(Collectors.toList());
    }

    public Map<String, Object> getSchedulerJobById(int jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        System.out.println(response);
        assertNotNull(response);
        return response;
    }

    public Boolean getSchedulerStatus() {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (Boolean) response.get("active");
    }

    public void updateSchedulerStatus(final boolean on) {
        String command = on ? "start" : "stop";
        final String UPDATE_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER STATUS -------------------------");
        Utils.performServerPost(requestSpec, response202Spec, UPDATE_SCHEDULER_STATUS_URL, runSchedulerJobAsJSON(), null);
    }

    public Map<String, Object> updateSchedulerJob(int jobId, final String active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final Map<String, Object> response = Utils.performServerPut(requestSpec, response200Spec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    private static String updateSchedulerJobAsJSON(final String active) {
        final Map<String, String> map = new HashMap<>();
        map.put("active", active);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public List getSchedulerJobHistory(int jobId) {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/jobs/" + jobId + "/runhistory?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING SCHEDULER JOB HISTORY -------------------------");
        final Map response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (ArrayList) response.get("pageItems");
    }

    public static void runSchedulerJob(final RequestSpecification requestSpec, final String jobId) {
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?command=executeJob&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    private static String runSchedulerJobAsJSON() {
        final Map<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        System.out.println(runSchedulerJob);
        return runSchedulerJob;
    }

    public void executeJob(String jobName) throws InterruptedException {
        List<Map> allSchedulerJobsData = getAllSchedulerJobs();
        Assert.assertNotNull(allSchedulerJobsData);

        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(jobName)) {
                Integer jobId = (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");

                // Executing Scheduler Job
                runSchedulerJob(this.requestSpec, jobId.toString());

                // Retrieving Scheduler Job by ID
                Map schedulerJob = getSchedulerJobById(jobId);
                Assert.assertNotNull(schedulerJob);

                // Waiting for Job to complete
                while ((Boolean) schedulerJob.get("currentlyRunning") == true) {
                    Thread.sleep(15000);
                    schedulerJob = getSchedulerJobById(jobId);
                    Assert.assertNotNull(schedulerJob);
                    System.out.println("Job is Still Running");
                }

                List<Map> jobHistoryData = getSchedulerJobHistory(jobId);

                Assert.assertFalse("Job History is empty :(  Was it too slow? Failures in background job?", jobHistoryData.isEmpty());

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
