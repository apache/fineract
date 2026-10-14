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
package org.apache.fineract.infrastructure.jobs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the stuck partitioned job recovery against a real Spring Batch schema: a COB worker killed mid-partition leaves
 * its step STARTED forever and the manager must not wait for it.
 */
@Testcontainers(disabledWithoutDocker = true)
public class StuckJobExecutorServiceImplTest {

    private static final String JOB_NAME = "LOAN_COB";
    private static final String PARTITIONER_STEP = LoanCOBConstant.LOAN_COB_PARTITIONER_STEP;
    private static final Duration STUCK_PARTITION_TIMEOUT = Duration.ofMinutes(10);

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource pool;
    private JdbcTemplate jdbc;
    private JobOperator jobOperator;
    private JobExecutionRepository jobExecutionRepository;
    private StuckJobExecutorServiceImpl service;
    private long nextId = 1;

    @BeforeEach
    public void setUp() {
        pool = new HikariDataSource();
        pool.setJdbcUrl(POSTGRES.getJdbcUrl());
        pool.setUsername(POSTGRES.getUsername());
        pool.setPassword(POSTGRES.getPassword());
        jdbc = new JdbcTemplate(pool);
        jdbc.execute("DROP SCHEMA public CASCADE");
        jdbc.execute("CREATE SCHEMA public");
        new ResourceDatabasePopulator(new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql")).execute(pool);

        FineractProperties.FineractJobProperties jobProperties = new FineractProperties.FineractJobProperties();
        jobProperties.setStuckRetryThreshold(5);
        jobProperties.setStuckPartitionTimeout(STUCK_PARTITION_TIMEOUT);
        FineractProperties fineractProperties = mock(FineractProperties.class);
        when(fineractProperties.getJob()).thenReturn(jobProperties);

        jobExecutionRepository = new JobExecutionRepository(new NamedParameterJdbcTemplate(pool), fineractProperties,
                mock(DatabaseTypeResolver.class));
        jobOperator = mock(JobOperator.class);
        JobRepository jobRepository = mock(JobRepository.class);
        when(jobRepository.getJobExecution(any(Long.class))).thenReturn(mock(JobExecution.class));
        service = new StuckJobExecutorServiceImpl(jobExecutionRepository, new TransactionTemplate(new DataSourceTransactionManager(pool)),
                jobOperator, jobRepository, fineractProperties);
    }

    @AfterEach
    public void tearDown() {
        pool.close();
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void startedPartitionOfKilledWorkerIsFailedAndOnlyUnfinishedPartitionsAreLeftToRestart() throws Exception {
        long jobExecutionId = insertStartedJobExecution();
        long managerStep = insertStep(jobExecutionId, PARTITIONER_STEP, "STARTED", minutesAgo(30));
        long completedPartition = insertStep(jobExecutionId, "partition0", "COMPLETED", minutesAgo(25));
        long killedPartition = insertStep(jobExecutionId, "partition1", "STARTED", minutesAgo(20));

        service.resumeStuckJob(JOB_NAME);

        assertThat(statusOf(killedPartition)).isEqualTo("FAILED");
        assertThat(statusOf(completedPartition)).isEqualTo("COMPLETED");
        assertThat(statusOf(managerStep)).isEqualTo("FAILED");
        verify(jobOperator).restart(any(JobExecution.class));
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void failedPartitionDoesNotMakeTheJobWait() throws Exception {
        long jobExecutionId = insertStartedJobExecution();
        insertStep(jobExecutionId, PARTITIONER_STEP, "STARTED", minutesAgo(5));
        long failedPartition = insertStep(jobExecutionId, "partition0", "FAILED", minutesAgo(1));

        service.resumeStuckJob(JOB_NAME);

        assertThat(statusOf(failedPartition)).isEqualTo("FAILED");
        verify(jobOperator).restart(any(JobExecution.class));
    }

    @Test
    public void recentlyUpdatedPartitionBelongsToALiveWorkerAndIsNotFailed() {
        long jobExecutionId = insertStartedJobExecution();
        long liveWorker = insertStep(jobExecutionId, "partition0", "STARTED", minutesAgo(1));

        int failed = jobExecutionRepository.failStalePartitions(jobExecutionId, PARTITIONER_STEP,
                DateUtils.getLocalDateTimeOfSystem().minus(STUCK_PARTITION_TIMEOUT));

        assertThat(failed).isZero();
        assertThat(statusOf(liveWorker)).isEqualTo("STARTED");
        assertThat(jobExecutionRepository.getActivePartitionsCount(jobExecutionId, PARTITIONER_STEP)).isEqualTo(1L);
    }

    private LocalDateTime minutesAgo(long minutes) {
        return DateUtils.getLocalDateTimeOfSystem().minusMinutes(minutes);
    }

    private long insertStartedJobExecution() {
        long instanceId = nextId++;
        long executionId = nextId++;
        jdbc.update("INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY) VALUES (?, 0, ?, ?)", instanceId,
                JOB_NAME, "key" + instanceId);
        jdbc.update("""
                INSERT INTO BATCH_JOB_EXECUTION (JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID, CREATE_TIME, START_TIME, STATUS, LAST_UPDATED)
                VALUES (?, 0, ?, ?, ?, 'STARTED', ?)
                """, executionId, instanceId, minutesAgo(40), minutesAgo(40), minutesAgo(40));
        return executionId;
    }

    private long insertStep(long jobExecutionId, String stepName, String status, LocalDateTime lastUpdated) {
        long stepId = nextId++;
        jdbc.update(
                """
                        INSERT INTO BATCH_STEP_EXECUTION (STEP_EXECUTION_ID, VERSION, STEP_NAME, JOB_EXECUTION_ID, CREATE_TIME, START_TIME, STATUS, LAST_UPDATED)
                        VALUES (?, 0, ?, ?, ?, ?, ?, ?)
                        """,
                stepId, stepName, jobExecutionId, minutesAgo(40), minutesAgo(40), status, lastUpdated);
        return stepId;
    }

    private String statusOf(long stepId) {
        return jdbc.queryForObject("SELECT STATUS FROM BATCH_STEP_EXECUTION WHERE STEP_EXECUTION_ID = ?", String.class, stepId);
    }
}
