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
package org.apache.fineract.infrastructure.jobs.domain;

import static org.springframework.batch.core.BatchStatus.COMPLETED;
import static org.springframework.batch.core.BatchStatus.FAILED;
import static org.springframework.batch.core.BatchStatus.STARTED;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExecutionRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<String> getStuckJobNames(NamedParameterJdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList(
                "SELECT bji.JOB_NAME as STUCK_JOB_NAME FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS IN (:statuses) AND bje.JOB_INSTANCE_ID NOT IN ("
                        + "SELECT bje.JOB_INSTANCE_ID FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS  = :completedStatus)",
                Map.of("statuses", List.of(STARTED.name(), FAILED.name()), "completedStatus", COMPLETED.name()), String.class);
    }

    public Long getStuckJobCountByJobName(String jobName) {
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) as STUCK_JOB_COUNT FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS IN (:statuses) AND bji.JOB_NAME = :jobName AND bje.JOB_INSTANCE_ID NOT IN ("
                        + "SELECT bje.JOB_INSTANCE_ID FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS  = :completedStatus AND bji.JOB_NAME = :jobName)",
                Map.of("statuses", List.of(STARTED.name(), FAILED.name()), "jobName", jobName, "completedStatus", COMPLETED.name()),
                Long.class);
    }

    public List<Long> getStuckJobIdsByJobName(String jobName) {
        return namedParameterJdbcTemplate.queryForList(
                "SELECT bje.JOB_EXECUTION_ID FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS IN (:statuses) AND bji.JOB_NAME = :jobName AND bje.JOB_INSTANCE_ID NOT IN ("
                        + "SELECT bje.JOB_INSTANCE_ID FROM BATCH_JOB_INSTANCE bji "
                        + "INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID "
                        + "WHERE bje.STATUS = :completedStatus AND bji.JOB_NAME = :jobName)",
                Map.of("statuses", List.of(STARTED.name(), FAILED.name()), "jobName", jobName, "completedStatus", COMPLETED.name()),
                Long.class);
    }

    public Long getNotCompletedPartitionsCount(Long jobExecutionId, String partitionerStepName) {
        return namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(bse.STEP_EXECUTION_ID) FROM BATCH_STEP_EXECUTION bse "
                        + "WHERE bse.JOB_EXECUTION_ID = :jobExecutionId AND bse.STEP_NAME <> :stepName AND bse.status <> :status",
                Map.of("jobExecutionId", jobExecutionId, "stepName", partitionerStepName, "status", COMPLETED.name()), Long.class);
    }

    public void updateJobStatusToFailed(Long stuckJobId, String partitionerStepName) {
        namedParameterJdbcTemplate.update(
                "UPDATE BATCH_STEP_EXECUTION SET STATUS = :status WHERE JOB_EXECUTION_ID = :jobExecutionId AND STEP_NAME = :stepName",
                Map.of("status", FAILED.name(), "jobExecutionId", stuckJobId, "stepName", partitionerStepName));
        namedParameterJdbcTemplate.update(
                "UPDATE BATCH_JOB_EXECUTION SET STATUS = :status, START_TIME = null, END_TIME = null WHERE JOB_EXECUTION_ID = :jobExecutionId",
                Map.of("status", FAILED.name(), "jobExecutionId", stuckJobId));
    }
}
