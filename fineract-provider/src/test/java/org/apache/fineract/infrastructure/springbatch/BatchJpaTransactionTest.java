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
package org.apache.fineract.infrastructure.springbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.common.COBStepExecutionSplitter;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.data.COBPartition;
import org.apache.fineract.cob.loan.LoanCOBBusinessStep;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.cob.loan.LoanCOBPartitioner;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.apache.fineract.infrastructure.core.persistence.ExtendedDataSourceTransactionManager;
import org.apache.fineract.infrastructure.core.persistence.ExtendedJpaTransactionManager;
import org.apache.fineract.infrastructure.core.persistence.visibility.VisibilityProbe;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.apache.fineract.infrastructure.jobs.ScheduledJobRunnerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.support.IteratorItemReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public class BatchJpaTransactionTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
    private HikariDataSource pool;
    private LocalContainerEntityManagerFactoryBean factory;
    private AnnotationConfigApplicationContext context;
    private ExtendedJpaTransactionManager transactionManager;
    private JobRepository repository;
    private JdbcTemplate jdbc;

    @BeforeEach
    public void setUp() {
        pool = new HikariDataSource();
        pool.setJdbcUrl(POSTGRES.getJdbcUrl());
        pool.setUsername(POSTGRES.getUsername());
        pool.setPassword(POSTGRES.getPassword());
        RoutingDataSource dataSource = new RoutingDataSource() {

            @Override
            public DataSource determineTargetDataSource() {
                return pool;
            }
        };
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP SCHEMA public CASCADE");
        jdbc.execute("CREATE SCHEMA public");
        new ResourceDatabasePopulator(new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql")).execute(dataSource);
        // Fineract keeps the Spring Batch 5 job instance sequence name rather than migrating to Batch 6's, so that
        // Batch 5 and Batch 6 instances can share a database - see FineractDataFieldMaxValueIncrementerFactory and
        // 0021_add_spring_batch_db_structure.xml. Align the stock Batch 6 schema this fixture bootstraps from.
        jdbc.execute("ALTER SEQUENCE BATCH_JOB_INSTANCE_SEQ RENAME TO BATCH_JOB_SEQ");
        jdbc.execute("create table visibility_probe (id bigint primary key, name varchar(255))");
        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("batch-jpa-" + UUID.randomUUID());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(VisibilityProbe.class.getPackageName());
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("eclipselink.weaving", "false", "eclipselink.logging.level", "SEVERE"));
        factory.afterPropertiesSet();
        transactionManager = new ExtendedJpaTransactionManager(false);
        transactionManager.setEntityManagerFactory(factory.getObject());
        transactionManager.afterPropertiesSet();
        ExtendedDataSourceTransactionManager jdbcManager = new ExtendedDataSourceTransactionManager(false);
        jdbcManager.setDataSource(dataSource);
        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("dataSource", dataSource);
        context.getBeanFactory().registerSingleton("transactionManager", transactionManager);
        context.getBeanFactory().registerSingleton("jdbcTransactionManager", jdbcManager);
        context.register(ScheduledJobRunnerConfig.class);
        context.refresh();
        repository = context.getBean(JobRepository.class);
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
        if (factory != null) {
            factory.destroy();
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Test
    public void repositoryUpdateMustNotCommitTheEnclosingJpaTransaction() {
        JobExecution job = repository.createJobExecution(repository.createJobInstance("rollback", new JobParameters()), new JobParameters(),
                new ExecutionContext());
        StepExecution step = repository.createStepExecution("worker", job);
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var em = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
            em.persist(new VisibilityProbe(1L, "first"));
            em.persist(new VisibilityProbe(2L, "second"));
            em.flush();
            step.getExecutionContext().putInt("checkpoint", 2);
            repository.updateExecutionContext(step);
            repository.update(step);
            assertThat(countFromIndependentConnection()).as("business writes must remain uncommitted until the chunk commits").isZero();
            throw new IllegalStateException("force chunk rollback");
        })).isInstanceOf(IllegalStateException.class).hasMessage("force chunk rollback");
        assertThat(jdbc.queryForObject("select count(*) from visibility_probe", Integer.class)).isZero();
        assertThat(repository.getStepExecution(step.getId()).getExecutionContext().containsKey("checkpoint")).isFalse();
    }

    @Test
    public void commitFailureRollsBackBusinessWritesAndCheckpoint() throws Exception {
        jdbc.execute("alter table visibility_probe add constraint valid_name check (name <> 'fail')");
        var job = repository.createJobExecution(repository.createJobInstance("commit-failure", new JobParameters()), new JobParameters(),
                new ExecutionContext());
        var execution = repository.createStepExecution("worker", job);
        var step = new StepBuilder("worker", repository).<Long, Long>chunk(2).transactionManager(transactionManager)
                .reader(new IteratorItemReader<>(List.of(1L, 2L))).processor(id -> {
                    var em = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
                    em.persist(new VisibilityProbe(id, "ok"));
                    em.flush();
                    return id;
                }).writer(items -> {
                    var em = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
                    // Defer the invalid update until JPA commit, after Batch has attempted to save its checkpoint.
                    em.find(VisibilityProbe.class, 2L).setName("fail");
                    execution.getExecutionContext().putInt("checkpoint", 2);
                }).build();
        step.execute(execution);
        assertThat(jdbc.queryForObject("select count(*) from visibility_probe", Integer.class)).isZero();
        StepExecution failed = repository.getStepExecution(execution.getId());
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(failed.getExecutionContext().containsKey("checkpoint")).isFalse();
        assertThat(failed.getWriteCount()).isZero();
        assertThat(failed.getCommitCount()).isZero();
        assertThat(failed.getRollbackCount()).isEqualTo(1);
    }

    @Test
    public void taskletCommitFailureRemainsRestartable() throws Exception {
        jdbc.execute("alter table visibility_probe add constraint valid_name check (name <> 'fail')");
        var job = repository.createJobExecution(repository.createJobInstance("tasklet-failure", new JobParameters()), new JobParameters(),
                new ExecutionContext());
        var execution = repository.createStepExecution("tasklet", job);
        var step = new StepBuilder("tasklet", repository).tasklet((contribution, chunkContext) -> {
            var em = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
            em.persist(new VisibilityProbe(1L, "fail"));
            return org.springframework.batch.infrastructure.repeat.RepeatStatus.FINISHED;
        }, transactionManager).build();
        step.execute(execution);
        assertThat(jdbc.queryForObject("select count(*) from visibility_probe", Integer.class)).isZero();
        var failed = repository.getStepExecution(execution.getId());
        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(failed.getCommitCount()).isZero();
        assertThat(failed.getRollbackCount()).isEqualTo(1);
    }

    @Test
    public void restartUsesPersistedPartitionNamesBeforeTheManagerStepFinishes() throws Exception {
        var job = repository.createJobExecution(repository.createJobInstance("restart", new JobParameters()), new JobParameters(),
                new ExecutionContext());
        job.getExecutionContext().put("BusinessDate", LocalDate.of(2026, 9, 1));
        job.getExecutionContext().put("IS_CATCH_UP", false);
        repository.updateExecutionContext(job);
        StepExecution manager = repository.createStepExecution("manager", job);
        var originalPartitioner = partitioner(manager);
        var splitter = new COBStepExecutionSplitter(repository, "worker", originalPartitioner);
        var workers = splitter.split(manager, 6);
        assertThat(workers).hasSize(3);
        // Only database state survives a manager crash: deliberately discard the original manager context.
        var persisted = repository.getStepExecution(manager.getId());
        assertThat(persisted.getExecutionContext().getInt("partitionCount")).isEqualTo(3);
        for (var worker : workers) {
            worker.setStatus(worker.getStepName().endsWith("partition_2") ? BatchStatus.FAILED : BatchStatus.COMPLETED);
            repository.update(worker);
        }
        job.setStatus(BatchStatus.FAILED);
        repository.update(job);
        var restartedJob = repository.createJobExecution(job.getJobInstance(), new JobParameters(), job.getExecutionContext());
        var restartedManager = repository.createStepExecution("manager", restartedJob);
        restartedManager.setExecutionContext(persisted.getExecutionContext());
        var restarted = new COBStepExecutionSplitter(repository, "worker", partitioner(restartedManager)).split(restartedManager, 1);
        assertThat(restarted).singleElement().satisfies(worker -> {
            assertThat(worker.getStepName()).isEqualTo("worker:partition_2");
            assertThat(worker.getExecutionContext().get("loanCobParameter"))
                    .isEqualTo(workers.stream().filter(w -> w.getStepName().equals("worker:partition_2")).findFirst().orElseThrow()
                            .getExecutionContext().get("loanCobParameter"));
        });
    }

    private LoanCOBPartitioner partitioner(StepExecution manager) {
        var properties = mock(PropertyService.class);
        var service = mock(COBBusinessStepService.class);
        var ids = mock(RetrieveIdService.class);
        when(properties.getPartitionSize(LoanCOBConstant.JOB_NAME)).thenReturn(4);
        when(service.getCOBBusinessSteps(LoanCOBBusinessStep.class, LoanCOBConstant.LOAN_COB_JOB_NAME))
                .thenReturn(Set.of(new BusinessStepNameAndOrder("step", 1L)));
        when(ids.retrieveLoanCOBPartitions(anyLong(), any(), anyBoolean(), anyInt()))
                .thenReturn(List.of(new COBPartition(1L, 4L, 0L, 4L), new COBPartition(5L, 8L, 1L, 4L), new COBPartition(9L, 12L, 2L, 4L)));
        return new LoanCOBPartitioner(properties, service, ids, mock(JobOperator.class), manager, 1L);
    }

    private int countFromIndependentConnection() {
        try (Connection connection = pool.getConnection();
                var statement = connection.createStatement();
                var rows = statement.executeQuery("select count(*) from visibility_probe")) {
            rows.next();
            return rows.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
