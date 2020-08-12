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

import static java.time.Instant.now;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerJobHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification response200Spec;
    private final ResponseSpecification response202Spec;

    public SchedulerJobHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.response202Spec = new ResponseSpecBuilder().expectStatusCode(202).build();
    }

    private List<Map<String, Object>> getAllSchedulerJobs() {
        final String GET_ALL_SCHEDULER_JOBS_URL = "/fineract-provider/api/v1/jobs?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        List<Map<String, Object>> response = Utils.performServerGet(requestSpec, response200Spec, GET_ALL_SCHEDULER_JOBS_URL, "");
        assertNotNull(response);
        return response;
    }

    private <T> List<T> getAllSchedulerJobDetails(Function<Map<String, Object>, T> mapper) {
        return getAllSchedulerJobs().stream().map(mapper).collect(Collectors.toList());
    }

    public List<Integer> getAllSchedulerJobIds() {
        return getAllSchedulerJobDetails(map -> (Integer) map.get("jobId"));
    }

    public List<String> getAllSchedulerJobNames() {
        return getAllSchedulerJobDetails(map -> (String) map.get("displayName"));
    }

    public Map<String, Object> getSchedulerJobById(int jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        LOG.info("{}", response.toString());
        assertNotNull(response);
        return response;
    }

    public Boolean getSchedulerStatus() {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (Boolean) response.get("active");
    }

    public void updateSchedulerStatus(final boolean on) {
        String command = on ? "start" : "stop";
        final String UPDATE_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ UPDATING SCHEDULER STATUS -------------------------");
        Utils.performServerPost(requestSpec, response202Spec, UPDATE_SCHEDULER_STATUS_URL, runSchedulerJobAsJSON(), null);
    }

    public Map<String, Object> updateSchedulerJob(int jobId, final boolean active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final Map<String, Object> response = Utils.performServerPut(requestSpec, response200Spec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    private static String updateSchedulerJobAsJSON(final boolean active) {
        final Map<String, String> map = new HashMap<>();
        map.put("active", Boolean.toString(active));
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    private void runSchedulerJob(int jobId) {
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?command=executeJob&" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    private static String runSchedulerJobAsJSON() {
        final Map<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        LOG.info(runSchedulerJob);
        return runSchedulerJob;
    }

    private int getSchedulerJobIdByName(String jobName) {
        List<Map<String, Object>> allSchedulerJobsData = getAllSchedulerJobs();
        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(jobName)) {
                return (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");
            }
        }
        throw new IllegalArgumentException(
                "No such named Job (see org.apache.fineract.infrastructure.jobs.service.JobName enum):" + jobName);
    }

    /**
     * Launches a Job and awaits its completion.
     *
     * @param jobName
     *            displayName (see {@link org.apache.fineract.infrastructure.jobs.service.JobName}) of Scheduler Job
     *
     * @author Michael Vorburger.ch
     */
    public void executeAndAwaitJob(String jobName) {
        final Duration timeout = Duration.ofSeconds(30);
        final Duration pause = Duration.ofMillis(500);
        DateTimeFormatter df = DateTimeFormatter.ISO_INSTANT; // FINERACT-926
        Instant beforeExecuteTime = now().truncatedTo(ChronoUnit.SECONDS);

        // Stop the Scheduler while we manually trigger execution of job, to
        // avoid side effects and simplify debugging when readings logs
        updateSchedulerStatus(false);

        // Executing Scheduler Job
        int jobId = getSchedulerJobIdByName(jobName);
        runSchedulerJob(jobId);

        // Await JobDetailData.lastRunHistory [JobDetailHistoryData]
        // jobRunStartTime >= beforeExecuteTime (or timeout)
        await().atMost(timeout).pollInterval(pause).until(jobLastRunHistorySupplier(jobId), lastRunHistory -> {
            String jobRunStartText = lastRunHistory.get("jobRunStartTime");
            if (jobRunStartText == null) {
                return false;
            }
            Instant jobRunStartTime = df.parse(jobRunStartText, Instant::from);
            return jobRunStartTime.equals(beforeExecuteTime) || jobRunStartTime.isAfter(beforeExecuteTime);
        });

        // Await JobDetailData.lastRunHistory [JobDetailHistoryData]
        // jobRunEndTime to be both set and >= jobRunStartTime (or timeout)
        Map<String, String> finalLastRunHistory = await().atMost(timeout).pollInterval(pause).until(jobLastRunHistorySupplier(jobId),
                lastRunHistory -> {
                    String jobRunEndText = lastRunHistory.get("jobRunEndTime");
                    if (jobRunEndText == null) {
                        return false;
                    }
                    Instant jobRunEndTime = df.parse(jobRunEndText, Instant::from);
                    Instant jobRunStartTime = df.parse(lastRunHistory.get("jobRunStartTime"), Instant::from);
                    return jobRunEndTime.equals(jobRunStartTime) || jobRunEndTime.isAfter(jobRunStartTime);
                });

        // Verify triggerType
        assertThat(finalLastRunHistory.get("triggerType"), is("application"));

        // Verify status & propagate jobRunErrorMessage and/or jobRunErrorLog
        // (if any)
        String status = finalLastRunHistory.get("status");
        if (!status.equals("success")) {
            fail("Job status is not success: " + finalLastRunHistory.toString());
        }

        // PS: Checking getSchedulerJobHistory() [/runhistory] is pointless,
        // because the lastRunHistory JobDetailHistoryData is already part of
        // JobDetailData anyway.
    }

    @SuppressWarnings("unchecked")
    private Callable<Map<String, String>> jobLastRunHistorySupplier(int jobId) {
        return () -> {
            Map<String, Object> job = getSchedulerJobById(jobId);
            if (job == null) {
                return null;
            }
            return (Map<String, String>) job.get("lastRunHistory");
        };
    }
}
