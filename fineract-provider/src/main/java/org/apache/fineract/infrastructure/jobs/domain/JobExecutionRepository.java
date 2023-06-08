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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobExecutionRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FineractProperties fineractProperties;
    private final DatabaseTypeResolver databaseTypeResolver;

    public List<String> getStuckJobNames(NamedParameterJdbcTemplate jdbcTemplate) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return jdbcTemplate.queryForList("""
                SELECT DISTINCT(BJI.JOB_NAME) as STUCK_JOB_NAME
                FROM BATCH_JOB_INSTANCE BJI
                INNER JOIN BATCH_JOB_EXECUTION BJE
                ON BJI.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
                WHERE
                    BJE.STATUS IN (:statuses)
                    AND
                    BJE.JOB_INSTANCE_ID NOT IN (
                        SELECT IBJE.JOB_INSTANCE_ID
                        FROM BATCH_JOB_INSTANCE IBJI
                        INNER JOIN BATCH_JOB_EXECUTION IBJE
                        ON IBJI.JOB_INSTANCE_ID = IBJE.JOB_INSTANCE_ID
                        WHERE IBJE.STATUS IN (:completedStatuses)
                    )
                GROUP BY BJI.JOB_INSTANCE_ID
                HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), String.class);
    }

    public Long getStuckJobCountByJobName(String jobName) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return namedParameterJdbcTemplate.queryForObject("""
                    SELECT COUNT(DISTINCT BJI.JOB_NAME) as STUCK_JOB_COUNT
                    FROM BATCH_JOB_INSTANCE BJI
                    INNER JOIN BATCH_JOB_EXECUTION BJE
                    ON BJI.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
                    WHERE
                        BJE.STATUS IN (:statuses)
                        AND
                        BJI.JOB_NAME = :jobName
                        AND
                        BJE.JOB_INSTANCE_ID NOT IN (
                            SELECT IBJE.JOB_INSTANCE_ID
                            FROM BATCH_JOB_INSTANCE IBJI
                            INNER JOIN BATCH_JOB_EXECUTION IBJE
                            ON IBJI.JOB_INSTANCE_ID = IBJE.JOB_INSTANCE_ID
                            WHERE
                                IBJE.STATUS IN (:completedStatuses)
                                AND
                                IBJI.JOB_NAME = :jobName
                        )
                    GROUP BY BJI.JOB_INSTANCE_ID
                    HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "jobName", jobName, "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), Long.class);
    }

    public List<Long> getStuckJobIdsByJobName(String jobName) {
        int threshold = fineractProperties.getJob().getStuckRetryThreshold();
        return namedParameterJdbcTemplate.queryForList("""
                    SELECT BJE.JOB_EXECUTION_ID
                    FROM BATCH_JOB_INSTANCE BJI
                    INNER JOIN BATCH_JOB_EXECUTION BJE
                    ON BJI.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
                    WHERE
                        BJE.STATUS IN (:statuses)
                        AND
                        BJI.JOB_NAME = :jobName
                        AND
                        BJE.JOB_INSTANCE_ID NOT IN (
                            SELECT IBJE.JOB_INSTANCE_ID
                            FROM BATCH_JOB_INSTANCE IBJI
                            INNER JOIN BATCH_JOB_EXECUTION IBJE
                            ON IBJI.JOB_INSTANCE_ID = IBJE.JOB_INSTANCE_ID
                            WHERE
                            IBJE.STATUS IN (:completedStatuses)
                            AND
                            IBJI.JOB_NAME = :jobName
                        )
                    GROUP BY BJI.JOB_INSTANCE_ID, BJE.JOB_EXECUTION_ID
                    HAVING COUNT(BJI.JOB_INSTANCE_ID) <= :threshold
                """, Map.of("statuses", List.of(STARTED.name()), "jobName", jobName, "completedStatuses",
                List.of(COMPLETED.name(), FAILED.name(), UNKNOWN.name()), "threshold", threshold), Long.class);
    }

    public Long getNotCompletedPartitionsCount(Long jobExecutionId, String partitionerStepName) {
        return namedParameterJdbcTemplate.queryForObject("""
                    SELECT COUNT(bse.STEP_EXECUTION_ID)
                    FROM BATCH_STEP_EXECUTION BSE
                    WHERE
                        BSE.JOB_EXECUTION_ID = :jobExecutionId
                        AND
                        BSE.STEP_NAME <> :stepName
                        AND
                        BSE.status <> :status
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

    public LocalDate getBusinessDateOfRunningJobByExecutionParameter(String jobName, String jobCustomParamKeyName, String parameterKeyName,
            String parameterValue, String dateParameterName) {
        try {
            if (databaseTypeResolver.isPostgreSQL()) {
                return namedParameterJdbcTemplate.queryForObject("""
                        SELECT
                                J2->>'parameterValue'
                        FROM
                            BATCH_JOB_INSTANCE BJI
                                INNER JOIN BATCH_JOB_EXECUTION BJE ON BJI.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
                                INNER JOIN BATCH_JOB_EXECUTION_PARAMS BJEP ON BJE.JOB_EXECUTION_ID = BJEP.JOB_EXECUTION_ID
                                inner join batch_custom_job_parameters CJP ON cast(BJEP.parameter_value as bigint) = CJP.id
                                AND BJEP.parameter_name = :jobCustomParamKeyName
                                CROSS JOIN LATERAL json_array_elements(CJP.parameter_json) J
                                CROSS JOIN LATERAL json_array_elements(CJP.parameter_json) J2
                        WHERE
                                    J ->> 'parameterName' = :filterParameterName
                          AND J ->> 'parameterValue' = :filterParameterValue
                          AND J2 ->> 'parameterName' = :dateParameterName
                          AND BJE.STATUS IN (:statuses)
                          AND BJI.JOB_NAME = :jobName
                          AND BJE.JOB_INSTANCE_ID NOT IN (
                            SELECT
                                IBJE.JOB_INSTANCE_ID
                            FROM
                                BATCH_JOB_INSTANCE IBJI
                                    INNER JOIN BATCH_JOB_EXECUTION IBJE ON IBJI.JOB_INSTANCE_ID = IBJE.JOB_INSTANCE_ID
                            WHERE
                                    IBJE.STATUS = :completedStatus
                              AND IBJI.JOB_NAME = :jobName)
                        """,
                        Map.of("jobCustomParamKeyName", jobCustomParamKeyName, "filterParameterName", parameterKeyName,
                                "filterParameterValue", parameterValue, "statuses", List.of(STARTED.name(), STARTING.name()),
                                "completedStatus", COMPLETED.name(), "jobName", jobName, "dateParameterName", dateParameterName),
                        LocalDate.class);
            } else if (databaseTypeResolver.isMySQL()) {
                return namedParameterJdbcTemplate.queryForObject(
                        """
                                SELECT
                                     J2.parameter_value
                                FROM
                                    BATCH_JOB_INSTANCE BJI
                                        INNER JOIN BATCH_JOB_EXECUTION BJE ON BJI.JOB_INSTANCE_ID = BJE.JOB_INSTANCE_ID
                                        INNER JOIN BATCH_JOB_EXECUTION_PARAMS BJEP ON BJE.JOB_EXECUTION_ID = BJEP.JOB_EXECUTION_ID
                                        inner join batch_custom_job_parameters CJP ON BJEP.parameter_value = CJP.id
                                        AND BJEP.parameter_name = :jobCustomParamKeyName
                                        CROSS JOIN json_table(CJP.parameter_json, '$[*]' COLUMNS(parameter_name VARCHAR(100) PATH "$.parameterName", parameter_value VARCHAR(100) PATH "$.parameterValue")) J
                                        CROSS JOIN json_table(CJP.parameter_json, '$[*]' COLUMNS(parameter_name VARCHAR(100) PATH "$.parameterName", parameter_value VARCHAR(100) PATH "$.parameterValue")) J2
                                WHERE
                                        J.parameter_name = :filterParameterName
                                  AND J.parameter_value = :filterParameterValue
                                  AND J2.parameter_name = :dateParameterName
                                  AND BJE.STATUS IN (:statuses)
                                  AND BJI.JOB_NAME = :jobName
                                  AND BJE.JOB_INSTANCE_ID NOT IN (
                                    SELECT
                                        IBJE.JOB_INSTANCE_ID
                                    FROM
                                        BATCH_JOB_INSTANCE IBJI
                                            INNER JOIN BATCH_JOB_EXECUTION IBJE ON IBJI.JOB_INSTANCE_ID = IBJE.JOB_INSTANCE_ID
                                    WHERE
                                            IBJE.STATUS = :completedStatus
                                      AND IBJI.JOB_NAME = :jobName);
                                """,
                        Map.of("jobCustomParamKeyName", jobCustomParamKeyName, "filterParameterName", parameterKeyName,
                                "filterParameterValue", parameterValue, "dateParameterName", dateParameterName, "statuses",
                                List.of(STARTED.name(), STARTING.name()), "jobName", jobName, "completedStatus", COMPLETED.name()),
                        LocalDate.class);
            } else {
                throw new IllegalStateException("Database type is not supported for json query " + databaseTypeResolver.databaseType());
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
