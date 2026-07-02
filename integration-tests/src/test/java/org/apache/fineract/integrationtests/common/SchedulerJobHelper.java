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

import static org.apache.fineract.infrastructure.jobs.api.SchedulerJobApiConstants.SHORT_NAME_PARAM;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.PutJobsJobIDRequest;
import org.apache.fineract.client.util.Calls;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerJobHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification response200Spec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public SchedulerJobHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
        this.response200Spec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
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

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Map<String, Object> getSchedulerJobById(int jobId) {
        final String GET_SCHEDULER_JOB_BY_ID_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_JOB_BY_ID_URL, "");
        LOG.info("{}", response.toString());
        assertNotNull(response);
        return response;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Boolean getSchedulerStatus() {
        final String GET_SCHEDULER_STATUS_URL = "/fineract-provider/api/v1/scheduler?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        final Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_SCHEDULER_STATUS_URL, "");
        return (Boolean) response.get("active");
    }

    public void updateSchedulerStatus(final boolean on) {
        String command = on ? "start" : "stop";
        Calls.ok(FineractClientHelper.getFineractClient().jobsScheduler.handleCommandsScheduler(command));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Map<String, Object> updateSchedulerJob(int jobId, final boolean active) {
        final String UPDATE_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ UPDATING SCHEDULER JOB -------------------------");
        final Map<String, Object> response = Utils.performServerPut(requestSpec, response200Spec, UPDATE_SCHEDULER_JOB_URL,
                updateSchedulerJobAsJSON(active), "changes");
        return response;
    }

    public void updateSchedulerJob(long jobId, PutJobsJobIDRequest request) {
        Calls.ok(FineractClientHelper.getFineractClient().jobs.updateJobDetail(jobId, request));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String updateSchedulerJobAsJSON(final boolean active) {
        final Map<String, String> map = new HashMap<>();
        map.put("active", Boolean.toString(active));
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public void runSchedulerJob(int jobId) {
        Calls.ok(FineractClientHelper.getFineractClient().jobs.executeJob((long) jobId, "executeJob"));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void runSchedulerJob(int jobId, ResponseSpecification responseSpec) {
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + jobId + "?command=executeJob&" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void runSchedulerJobByShortName(String shortName) {
        final ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(202).build();
        runSchedulerJobByShortName(shortName, responseSpec);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void runSchedulerJobByShortName(String shortName, ResponseSpecification responseSpec) {
        final String RUN_SCHEDULER_JOB_URL = "/fineract-provider/api/v1/jobs/" + SHORT_NAME_PARAM + "/" + shortName + "?command=executeJob&"
                + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RUN SCHEDULER JOB -------------------------");
        Utils.performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON(), null);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String runSchedulerJobAsJSON() {
        final Map<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        LOG.info(runSchedulerJob);
        return runSchedulerJob;
    }

    public int getSchedulerJobIdByName(String jobName) {
        List<Map<String, Object>> allSchedulerJobsData = getAllSchedulerJobs();
        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(jobName)) {
                return (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");
            }
        }
        throw new IllegalArgumentException(
                "No such named Job (see org.apache.fineract.infrastructure.jobs.service.JobName enum):" + jobName);
    }

    public Long getSchedulerJobIdByShortName(String shortName) {
        LOG.info("------------------------ RETRIEVING SCHEDULER JOB ID BY SHORT NAME -------------------------");
        GetJobsResponse job = Calls.ok(FineractClientHelper.getFineractClient().jobs.retrieveByShortName(shortName));
        assertNotNull(job);
        return job.getJobId();
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
        int jobId = getSchedulerJobIdByName(jobName);
        executeAndAwaitJob(jobId, jobId, this::runSchedulerJob);
    }

    /**
     * Launches a Job and awaits its completion.
     *
     * @param shortName
     *            shortName of Scheduler Job
     *
     * @author Michael Vorburger.ch
     */
    public void executeAndAwaitJobByShortName(String shortName) {
        Long jobId = getSchedulerJobIdByShortName(shortName);
        executeAndAwaitJob(jobId, shortName, this::runSchedulerJobByShortName);
    }

    private <T> void executeAndAwaitJob(long jobId, T jobParam, Consumer<T> runSchedulerJob) {
        // Stop the Scheduler while we manually trigger execution of job, to
        // avoid side effects and simplify debugging when readings logs
        updateSchedulerStatus(false);

        Long previousRunHistoryId = getRunHistoryId(getLatestJobRunHistory(jobId));
        // Executing Scheduler Job
        runSchedulerJob.accept(jobParam);

        awaitJob(jobId, previousRunHistoryId);
    }

    private void awaitJob(long jobId, Long previousRunHistoryId) {
        final Duration timeout = Duration.ofMinutes(2);
        final Duration pause = Duration.ofSeconds(1);
        // Await a new completed run-history entry for this job. The history id is
        // monotonic and avoids false positives from timestamp precision.
        Map<String, Object> finalRunHistory = await().atMost(timeout) //
                .pollInterval(pause) //
                .pollDelay(pause) //
                .until(() -> getLatestJobRunHistory(jobId), //
                        lastRunHistory -> {
                            if (lastRunHistory == null || lastRunHistory.get("jobRunEndTime") == null) {
                                return false;
                            }
                            Long jobRunHistoryId = getRunHistoryId(lastRunHistory);
                            if (jobRunHistoryId == null) {
                                return false;
                            }
                            return previousRunHistoryId == null || jobRunHistoryId > previousRunHistoryId;
                        });

        // Verify triggerType
        MatcherAssert.assertThat(finalRunHistory.get("triggerType"), is("application"));

        // Verify status & propagate jobRunErrorMessage and/or jobRunErrorLog
        // (if any)
        String status = (String) finalRunHistory.get("status");
        if (!status.equals("success")) {
            fail("Job status is not success: " + finalRunHistory.toString());
        }
    }

    private Long getRunHistoryId(Map<String, Object> runHistory) {
        if (runHistory == null) {
            return null;
        }
        Object id = runHistory.get("id");
        if (id instanceof Number number) {
            return number.longValue();
        }
        if (id instanceof String text) {
            return Long.valueOf(text);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLatestJobRunHistory(long jobId) {
        final String GET_LATEST_SCHEDULER_JOB_RUN_HISTORY_URL = "/fineract-provider/api/v1/jobs/" + jobId
                + "/runhistory?offset=0&limit=1&orderBy=id&sortOrder=DESC&" + Utils.TENANT_IDENTIFIER;
        LOG.info("------------------------ RETRIEVING LATEST SCHEDULER JOB RUN HISTORY -------------------------");
        Map<String, Object> response = Utils.performServerGet(requestSpec, response200Spec, GET_LATEST_SCHEDULER_JOB_RUN_HISTORY_URL, "");
        List<Map<String, Object>> pageItems = (List<Map<String, Object>>) response.get("pageItems");
        if (pageItems == null || pageItems.isEmpty()) {
            return null;
        }
        return pageItems.get(0);
    }
}
