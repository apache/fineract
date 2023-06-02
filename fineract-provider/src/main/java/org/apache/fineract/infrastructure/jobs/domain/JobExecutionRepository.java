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
import static org.springframework.batch.core.BatchStatus.STARTING;
import static org.springframework.batch.core.BatchStatus.UNKNOWN;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExecutionRepository implements InitializingBean {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FineractProperties fineractProperties;
    private final DatabaseTypeResolver databaseTypeResolver;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final GoogleGsonSerializerHelper gsonFactory;
    private Gson gson;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.gson = gsonFactory.createSimpleGson();
    }

    public List<String> getStuckJobNames(NamedParameterJdbcTemplate jdbcTemplate) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT(bji.JOB_NAME) as STUCK_JOB_NAME
                FROM BATCH_JOB_INSTANCE bji
                INNER JOIN BATCH_JOB_EXECUTION bje
                ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                WHERE
                    bje.STATUS IN (:statuses)
                    AND
                    bje.JOB_INSTANCE_ID NOT IN (
                        SELECT bje.JOB_INSTANCE_ID
                        FROM BATCH_JOB_INSTANCE bji
                        INNER JOIN BATCH_JOB_EXECUTION bje
                        ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                        WHERE bje.STATUS IN (:completedStatuses)
                    )
                GROUP BY BJI.JOB_INSTANCE_ID
                HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), String.class);
    }

    public Long getStuckJobCountByJobName(String jobName) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return namedParameterJdbcTemplate.queryForObject("""
                    SELECT COUNT(DISTINCT bji.JOB_NAME) as STUCK_JOB_COUNT
                    FROM BATCH_JOB_INSTANCE bji
                    INNER JOIN BATCH_JOB_EXECUTION bje
                    ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                    WHERE
                        bje.STATUS IN (:statuses)
                        AND
                        bji.JOB_NAME = :jobName
                        AND
                        bje.JOB_INSTANCE_ID NOT IN (
                            SELECT bje.JOB_INSTANCE_ID
                            FROM BATCH_JOB_INSTANCE bji
                            INNER JOIN BATCH_JOB_EXECUTION bje
                            ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                            WHERE
                                bje.STATUS IN (:completedStatuses)
                                AND
                                bji.JOB_NAME = :jobName
                        )
                    GROUP BY BJI.JOB_INSTANCE_ID
                    HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "jobName", jobName, "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), Long.class);
    }

    public List<Long> getStuckJobIdsByJobName(String jobName) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return namedParameterJdbcTemplate.queryForList("""
                    SELECT bje.JOB_EXECUTION_ID
                    FROM BATCH_JOB_INSTANCE bji
                    INNER JOIN BATCH_JOB_EXECUTION bje
                    ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                    WHERE
                        bje.STATUS IN (:statuses)
                        AND
                        bji.JOB_NAME = :jobName
                        AND
                        bje.JOB_INSTANCE_ID NOT IN (
                            SELECT bje.JOB_INSTANCE_ID
                            FROM BATCH_JOB_INSTANCE bji
                            INNER JOIN BATCH_JOB_EXECUTION bje
                            ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID
                            WHERE
                            bje.STATUS IN (:completedStatuses)
                            AND
                            bji.JOB_NAME = :jobName
                        )
                    GROUP BY BJI.JOB_INSTANCE_ID, BJE.JOB_EXECUTION_ID
                    HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "jobName", jobName, "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), Long.class);
    }

    public Long getNotCompletedPartitionsCount(Long jobExecutionId, String partitionerStepName) {
        return namedParameterJdbcTemplate.queryForObject("""
                    SELECT COUNT(bse.STEP_EXECUTION_ID)
                    FROM BATCH_STEP_EXECUTION bse
                    WHERE
                        bse.JOB_EXECUTION_ID = :jobExecutionId
                        AND
                        bse.STEP_NAME <> :stepName
                        AND
                        bse.status <> :status
                """, Map.of("jobExecutionId", jobExecutionId, "stepName", partitionerStepName, "status", COMPLETED.name()), Long.class);
    }

    public void updateJobStatusToFailed(Long stuckJobId, String partitionerStepName) {
        namedParameterJdbcTemplate.update("""
                    UPDATE BATCH_STEP_EXECUTION
                    SET STATUS = :status
                    WHERE
                        JOB_EXECUTION_ID = :jobExecutionId
                        AND
                        STEP_NAME = :stepName
                """, Map.of("status", FAILED.name(), "jobExecutionId", stuckJobId, "stepName", partitionerStepName));
        namedParameterJdbcTemplate.update("""
                    UPDATE BATCH_JOB_EXECUTION
                    SET
                        STATUS = :status,
                        START_TIME = null,
                        END_TIME = null
                    WHERE
                        JOB_EXECUTION_ID = :jobExecutionId
                """, Map.of("status", FAILED.name(), "jobExecutionId", stuckJobId));
    }

    public List<Long> getRunningJobsIdsByExecutionParameter(String jobName, String jobCustomParamKeyName, String parameterKeyName,
            String parameterValue) {
        final StringBuilder sqlStatementBuilder = new StringBuilder();
        String jsonString = gson.toJson(new JobParameterDTO(parameterKeyName, parameterValue));
        sqlStatementBuilder.append(
                "SELECT bje.JOB_EXECUTION_ID FROM BATCH_JOB_INSTANCE bji INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID INNER JOIN BATCH_JOB_EXECUTION_PARAMS bjep ON bje.JOB_EXECUTION_ID = bjep.JOB_EXECUTION_ID"
                        + " WHERE bje.STATUS IN (:statuses) AND bji.JOB_NAME = :jobName AND bjep.PARAMETER_NAME = :jobCustomParamKeyName AND "
                        + sqlGenerator.castBigInt("bjep.PARAMETER_VALUE") + " IN (" + getSubQueryForCustomJobParameters()
                        + ") AND bje.JOB_INSTANCE_ID NOT IN (SELECT bje.JOB_INSTANCE_ID FROM BATCH_JOB_INSTANCE bji INNER JOIN BATCH_JOB_EXECUTION bje ON bji.JOB_INSTANCE_ID = bje.JOB_INSTANCE_ID"
                        + " WHERE bje.STATUS = :completedStatus AND bji.JOB_NAME = :jobName)");
        return namedParameterJdbcTemplate.queryForList(
                sqlStatementBuilder.toString(), Map.of("statuses", List.of(STARTED.name(), STARTING.name()), "jobName", jobName,
                        "completedStatus", COMPLETED.name(), "jobCustomParamKeyName", jobCustomParamKeyName, "jsonString", jsonString),
                Long.class);
    }

    private String getSubQueryForCustomJobParameters() {
        if (databaseTypeResolver.isMySQL()) {
            return "SELECT cjp.id FROM batch_custom_job_parameters cjp WHERE JSON_CONTAINS(cjp.parameter_json,:jsonString)";
        } else if (databaseTypeResolver.isPostgreSQL()) {
            return "SELECT cjp.id FROM (SELECT id,json_array_elements(parameter_json) AS json_data FROM batch_custom_job_parameters) AS cjp WHERE (cjp.json_data ::jsonb @> :jsonString ::jsonb)";
        } else {
            throw new IllegalStateException("Database type is not supported for json query " + databaseTypeResolver.databaseType());
        }
    }

}
