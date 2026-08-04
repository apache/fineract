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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.services.SchedulerJobApi.RetrieveHistoryQueryParams;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetJobsJobIDJobRunHistoryResponse;
import org.apache.fineract.client.models.GetJobsResponse;
import org.apache.fineract.client.models.GetSchedulerResponse;
import org.apache.fineract.client.models.JobDetailHistoryDataSwagger;
import org.apache.fineract.client.models.PutJobsJobIDRequest;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign-based, fully static.
 */
public final class SchedulerJobHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerJobHelper.class);

    private SchedulerJobHelper() {

    }

    private static List<GetJobsResponse> getAllSchedulerJobs() {
        LOG.info("------------------------ RETRIEVING ALL SCHEDULER JOBS -------------------------");
        List<GetJobsResponse> response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().retrieveAllSchedulerJobs());
        assertNotNull(response);
        return response;
    }

    private static <T> List<T> getAllSchedulerJobDetails(Function<GetJobsResponse, T> mapper) {
        return getAllSchedulerJobs().stream().map(mapper).collect(Collectors.toList());
    }

    public static List<Integer> getAllSchedulerJobIds() {
        return getAllSchedulerJobDetails(job -> job.getJobId().intValue());
    }

    public static List<String> getAllSchedulerJobNames() {
        return getAllSchedulerJobDetails(GetJobsResponse::getDisplayName);
    }

    public static GetJobsResponse getSchedulerJobById(int jobId) {
        LOG.info("------------------------ RETRIEVING SCHEDULER JOB BY ID -------------------------");
        GetJobsResponse response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().retrieveOneSchedulerJob((long) jobId));
        assertNotNull(response);
        LOG.info("{}", response);
        return response;
    }

    public static Boolean getSchedulerStatus() {
        LOG.info("------------------------ RETRIEVING SCHEDULER STATUS -------------------------");
        GetSchedulerResponse response = ok(() -> FineractFeignClientHelper.getFineractFeignClient().scheduler().retrieveSchedulerStatus());
        return response.getActive();
    }

    public static void updateSchedulerStatus(final boolean on) {
        String command = on ? "start" : "stop";
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().scheduler().handleCommandsScheduler(command));
    }

    public static Map<String, Object> updateSchedulerJob(int jobId, final boolean active) {
        LOG.info("------------------------ UPDATING SCHEDULER JOB -------------------------");
        CommandProcessingResult response = ok(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob()
                .updateJobDetail((long) jobId, new PutJobsJobIDRequest().active(active)));
        return response.getChanges();
    }

    public static void updateSchedulerJob(long jobId, PutJobsJobIDRequest request) {
        ok(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().updateJobDetail(jobId, request));
    }

    public static void runSchedulerJob(int jobId) {
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().executeJob((long) jobId, "executeJob",
                new ExecuteJobRequest()));
    }

    public static void runSchedulerJobByShortName(String shortName) {
        LOG.info("------------------------ RUN SCHEDULER JOB -------------------------");
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().executeJobByShortName(shortName, "executeJob",
                new ExecuteJobRequest()));
    }

    public static int getSchedulerJobIdByName(String jobName) {
        List<GetJobsResponse> allSchedulerJobsData = getAllSchedulerJobs();
        for (GetJobsResponse job : allSchedulerJobsData) {
            if (jobName.equals(job.getDisplayName())) {
                return job.getJobId().intValue();
            }
        }
        throw new IllegalArgumentException(
                "No such named Job (see org.apache.fineract.infrastructure.jobs.service.JobName enum):" + jobName);
    }

    public static Long getSchedulerJobIdByShortName(String shortName) {
        LOG.info("------------------------ RETRIEVING SCHEDULER JOB ID BY SHORT NAME -------------------------");
        GetJobsResponse job = ok(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().retrieveByShortName(shortName));
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
    public static void executeAndAwaitJob(String jobName) {
        int jobId = getSchedulerJobIdByName(jobName);
        executeAndAwaitJob(jobId, jobId, SchedulerJobHelper::runSchedulerJob);
    }

    /**
     * Launches a Job and awaits its completion.
     *
     * @param shortName
     *            shortName of Scheduler Job
     *
     * @author Michael Vorburger.ch
     */
    public static void executeAndAwaitJobByShortName(String shortName) {
        Long jobId = getSchedulerJobIdByShortName(shortName);
        executeAndAwaitJob(jobId, shortName, SchedulerJobHelper::runSchedulerJobByShortName);
    }

    private static <T> void executeAndAwaitJob(long jobId, T jobParam, Consumer<T> runSchedulerJob) {
        // Stop the Scheduler while we manually trigger execution of job, to
        // avoid side effects and simplify debugging when readings logs
        updateSchedulerStatus(false);

        Long previousRunHistoryId = getRunHistoryId(getLatestJobRunHistory(jobId));
        // Executing Scheduler Job
        runSchedulerJob.accept(jobParam);

        awaitJob(jobId, previousRunHistoryId);
    }

    private static void awaitJob(long jobId, Long previousRunHistoryId) {
        final Duration timeout = Duration.ofMinutes(2);
        final Duration pause = Duration.ofSeconds(1);
        // Await a new completed run-history entry for this job. The history id is
        // monotonic and avoids false positives from timestamp precision.
        JobDetailHistoryDataSwagger finalRunHistory = await().atMost(timeout) //
                .pollInterval(pause) //
                .pollDelay(pause) //
                .until(() -> getLatestJobRunHistory(jobId), //
                        lastRunHistory -> {
                            if (lastRunHistory == null || lastRunHistory.getJobRunEndTime() == null) {
                                return false;
                            }
                            Long jobRunHistoryId = getRunHistoryId(lastRunHistory);
                            if (jobRunHistoryId == null) {
                                return false;
                            }
                            return previousRunHistoryId == null || jobRunHistoryId > previousRunHistoryId;
                        });

        // Verify triggerType
        MatcherAssert.assertThat(finalRunHistory.getTriggerType(), is("application"));

        // Verify status & propagate jobRunErrorMessage and/or jobRunErrorLog
        // (if any)
        String status = finalRunHistory.getStatus();
        if (!"success".equals(status)) {
            fail("Job status is not success for jobId=" + jobId + ": " + finalRunHistory);
        }
    }

    private static Long getRunHistoryId(JobDetailHistoryDataSwagger runHistory) {
        return runHistory == null ? null : runHistory.getId();
    }

    private static JobDetailHistoryDataSwagger getLatestJobRunHistory(long jobId) {
        LOG.info("------------------------ RETRIEVING LATEST SCHEDULER JOB RUN HISTORY -------------------------");
        RetrieveHistoryQueryParams queryParams = new RetrieveHistoryQueryParams().offset(0).limit(1).orderBy("id").sortOrder("DESC");
        GetJobsJobIDJobRunHistoryResponse response = ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().retrieveHistory(jobId, queryParams));
        List<JobDetailHistoryDataSwagger> pageItems = response.getPageItems();
        if (pageItems == null || pageItems.isEmpty()) {
            return null;
        }
        return pageItems.get(0);
    }
}
