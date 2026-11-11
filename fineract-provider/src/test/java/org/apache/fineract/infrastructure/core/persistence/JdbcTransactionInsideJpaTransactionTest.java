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
package org.apache.fineract.infrastructure.core.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.listener.CobWorkerStepListener;
import org.apache.fineract.cob.loan.ApplyLoanLockTasklet;
import org.apache.fineract.cob.service.RetrieveIdService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.persistence.visibility.VisibilityProbe;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.apache.fineract.infrastructure.jobs.ScheduledJobRunnerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.support.IteratorItemReader;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Pins how {@link ExtendedDataSourceTransactionManager} behaves inside a transaction of
 * {@link ExtendedJpaTransactionManager} on the same PostgreSQL {@code DataSource}.
 * <p>
 * A plain {@code DataSourceTransactionManager} does not recognise the JPA transaction: it runs "its own" transaction on
 * the JPA transaction's connection and commits it, or rolls it back with {@code UnexpectedRollbackException} when the
 * JPA transaction is rollback-only. These tests pin that it now joins, suspends or refuses, as the propagation asks.
 */
@Testcontainers(disabledWithoutDocker = true)
class JdbcTransactionInsideJpaTransactionTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource pool;
    private LocalContainerEntityManagerFactoryBean factory;
    private AnnotationConfigApplicationContext context;
    private ExtendedJpaTransactionManager jpaTransactionManager;
    private ExtendedDataSourceTransactionManager jdbcTransactionManager;
    private TransactionTemplate requiresNewJdbc;
    private JobRepository jobRepository;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        pool = new HikariDataSource();
        pool.setJdbcUrl(POSTGRES.getJdbcUrl());
        pool.setUsername(POSTGRES.getUsername());
        pool.setPassword(POSTGRES.getPassword());
        pool.setConnectionTimeout(1_000);
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
        // See BatchJpaTransactionTest: Fineract keeps the Spring Batch 5 job instance sequence name.
        jdbc.execute("ALTER SEQUENCE BATCH_JOB_INSTANCE_SEQ RENAME TO BATCH_JOB_SEQ");
        jdbc.execute("create table visibility_probe (id bigint primary key, name varchar(255))");
        jdbc.execute("create table probe_lock (loan_id bigint primary key, lock_owner varchar(50) not null)");

        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("jdbc-in-jpa-" + UUID.randomUUID());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(VisibilityProbe.class.getPackageName());
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("eclipselink.weaving", "false", "eclipselink.logging.level", "SEVERE"));
        factory.afterPropertiesSet();
        jpaTransactionManager = new ExtendedJpaTransactionManager(false);
        jpaTransactionManager.setEntityManagerFactory(factory.getObject());
        // As JpaTransactionConfig and JdbcTransactionConfig configure them.
        jpaTransactionManager.setValidateExistingTransaction(true);
        jpaTransactionManager.afterPropertiesSet();
        jdbcTransactionManager = new ExtendedDataSourceTransactionManager(false);
        jdbcTransactionManager.setDataSource(dataSource);
        jdbcTransactionManager.setValidateExistingTransaction(true);
        requiresNewJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("dataSource", dataSource);
        context.getBeanFactory().registerSingleton("transactionManager", jpaTransactionManager);
        context.getBeanFactory().registerSingleton("jdbcTransactionManager", jdbcTransactionManager);
        context.register(ScheduledJobRunnerConfig.class);
        context.refresh();
        jobRepository = context.getBean(JobRepository.class);
    }

    @AfterEach
    void tearDown() {
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
    void requiresNewOutsideAnyTransactionCommits() {
        requiresNewJdbc.executeWithoutResult(status -> insertProbe(1L, "jdbc"));

        assertThat(probeNames()).containsExactly("jdbc");
    }

    @Test
    void requiresNewInsideAJpaTransactionSuspendsItAndCommitsIndependently() {
        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            requiresNewJdbc.executeWithoutResult(inner -> insertProbe(2L, "jdbc"));
            assertThat(jdbcOnTheJpaConnectionSees(1L)).as("the JPA transaction must be intact after the inner one").isTrue();
            throw new IllegalStateException("jpa rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(probeNames()).containsExactly("jdbc");
    }

    @Test
    void requiresNewInsideARollbackOnlyJpaTransactionStillCommits() {
        // Work that must survive the enclosing transaction, such as an error record, saved once it is already doomed.
        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            markRollbackOnlyLikeAFailedBusinessStep();
            requiresNewJdbc.executeWithoutResult(inner -> insertProbe(2L, "error"));
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(probeNames()).containsExactly("error");
    }

    @Test
    void requiredInsideAJpaTransactionJoinsIt() {
        TransactionTemplate requiredJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_REQUIRED);

        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            requiredJdbc.executeWithoutResult(inner -> insertProbe(2L, "jdbc"));
            assertThat(probeNamesFromAnotherConnection()).as("joining must not commit the JPA work early").isEmpty();
            throw new IllegalStateException("jpa rollback");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(probeNames()).isEmpty();

        jpa().executeWithoutResult(outer -> {
            persistProbe(3L, "jpa");
            requiredJdbc.executeWithoutResult(inner -> insertProbe(4L, "jdbc"));
        });
        assertThat(probeNames()).containsExactly("jpa", "jdbc");
    }

    @Test
    void aFailingJdbcParticipantMarksTheJpaTransactionRollbackOnly() {
        TransactionTemplate requiredJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_REQUIRED);

        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            try {
                requiredJdbc.executeWithoutResult(inner -> {
                    insertProbe(2L, "jdbc");
                    throw new IllegalStateException("participant failed");
                });
            } catch (IllegalStateException expected) {
                // The caller carries on, as callers of a failed @Transactional participant sometimes do.
            }
            assertThat(outer.isRollbackOnly()).isTrue();
        })).isInstanceOf(UnexpectedRollbackException.class);

        assertThat(probeNames()).isEmpty();
    }

    @Test
    void notSupportedInsideAJpaTransactionRunsOutsideIt() {
        TransactionTemplate notSupportedJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        assertThatThrownBy(() -> jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            notSupportedJdbc.executeWithoutResult(inner -> insertProbe(2L, "auto-committed"));
            throw new IllegalStateException("jpa rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(probeNames()).containsExactly("auto-committed");
    }

    @Test
    void nestedInsideAJpaTransactionIsRefusedWithoutHarmingIt() {
        TransactionTemplate nestedJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_NESTED);

        jpa().executeWithoutResult(outer -> {
            persistProbe(1L, "jpa");
            assertThatThrownBy(() -> nestedJdbc.executeWithoutResult(inner -> insertProbe(2L, "never")))
                    .isInstanceOf(IllegalTransactionStateException.class);
        });

        assertThat(probeNames()).containsExactly("jpa");
    }

    @Test
    void aWriteJoiningAReadOnlyJpaTransactionIsRefused() {
        TransactionTemplate readOnlyJpa = jpa();
        readOnlyJpa.setReadOnly(true);
        TransactionTemplate requiredJdbc = jdbcTemplate(TransactionDefinition.PROPAGATION_REQUIRED);

        readOnlyJpa.executeWithoutResult(
                outer -> assertThatThrownBy(() -> requiredJdbc.executeWithoutResult(inner -> insertProbe(1L, "never")))
                        .isInstanceOf(IllegalTransactionStateException.class));

        assertThat(probeNames()).isEmpty();
    }

    @Test
    void insideAJdbcTransactionRequiresNewStillSuspendsIt() {
        assertThatThrownBy(() -> new TransactionTemplate(jdbcTransactionManager).executeWithoutResult(outer -> {
            insertProbe(1L, "outer");
            requiresNewJdbc.executeWithoutResult(inner -> insertProbe(2L, "inner"));
            throw new IllegalStateException("outer rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(probeNames()).containsExactly("inner");
    }

    @Test
    void theCobWorkerStepListenerAppliesTheLocksThroughTheJdbcTemplate() throws Exception {
        // The real COB path: CobWorkerStepListener.beforeStep -> ApplyLoanLockTasklet ->
        // requiresNewTransactionJdbcTemplate,
        // in a chunk-oriented step on the JPA transaction manager, as LoanCOBWorkerConfiguration builds it.
        CobWorkerStepListener listener = new CobWorkerStepListener((contribution, chunkContext) -> RepeatStatus.FINISHED,
                applyLoanLockTasklet(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        Step step = new StepBuilder("worker", jobRepository).<Long, Long>chunk(10).transactionManager(jpaTransactionManager)
                .reader(new IteratorItemReader<>(List.<Long>of())).writer(JdbcTransactionInsideJpaTransactionTest::writeNothing)
                .listener(listener).build();

        StepExecution execution = workerStepExecution();
        step.execute(execution);

        assertThat(jobRepository.getStepExecution(execution.getId()).getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jdbc.queryForList("select loan_id from probe_lock order by loan_id", Long.class)).containsExactly(1L, 2L);
    }

    @Test
    void theLockTaskletAppliesItsLocksIndependentlyEvenInsideAJpaTransaction() throws Exception {
        // Inside a JPA tasklet transaction the JDBC REQUIRES_NEW used to run on - and commit - the JPA connection.
        Step step = new StepBuilder("worker", jobRepository).tasklet(applyLoanLockTasklet(), jpaTransactionManager).build();

        StepExecution execution = workerStepExecution();
        step.execute(execution);

        assertThat(jobRepository.getStepExecution(execution.getId()).getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jdbc.queryForList("select loan_id from probe_lock order by loan_id", Long.class)).containsExactly(1L, 2L);
    }

    private TransactionTemplate jpa() {
        return new TransactionTemplate(jpaTransactionManager);
    }

    private TransactionTemplate jdbcTemplate(int propagation) {
        TransactionTemplate template = new TransactionTemplate(jdbcTransactionManager);
        template.setPropagationBehavior(propagation);
        return template;
    }

    private void markRollbackOnlyLikeAFailedBusinessStep() {
        try {
            jpa().executeWithoutResult(participating -> {
                throw new IllegalStateException("business step failed");
            });
        } catch (IllegalStateException expected) {
            // The participating transaction has marked the enclosing one rollback-only, as @Transactional does.
        }
    }

    private boolean jdbcOnTheJpaConnectionSees(Long id) {
        return jdbc.queryForObject("select count(*) from visibility_probe where id = ?", Integer.class, id) == 1;
    }

    private List<String> probeNamesFromAnotherConnection() {
        List<String> names = new ArrayList<>();
        try (Connection connection = pool.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select name from visibility_probe order by id")) {
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return names;
    }

    private ApplyLoanLockTasklet applyLoanLockTasklet() {
        FineractProperties properties = new FineractProperties();
        FineractProperties.FineractQueryProperties query = new FineractProperties.FineractQueryProperties();
        query.setInClauseParameterSizeLimit(1000);
        properties.setQuery(query);
        RetrieveIdService retrieveIdService = mock(RetrieveIdService.class);
        when(retrieveIdService.retrieveAllNonClosedLoansByLastClosedBusinessDateAndMinAndMaxLoanId(any(), anyBoolean()))
                .thenReturn(List.of(1L, 2L));
        return new ApplyLoanLockTasklet(properties, new ProbeLockingService(jdbc), retrieveIdService, requiresNewJdbc);
    }

    private StepExecution workerStepExecution() throws Exception {
        JobExecution job = jobRepository.createJobExecution(
                jobRepository.createJobInstance("cob-" + UUID.randomUUID(), new JobParameters()), new JobParameters(),
                new ExecutionContext());
        StepExecution execution = jobRepository.createStepExecution("worker", job);
        execution.getExecutionContext().put(COBConstant.COB_PARAMETER, new COBParameter(1L, 2L));
        return execution;
    }

    private static void writeNothing(Chunk<? extends Long> items) {
        // The step has nothing to write; only its beforeStep matters here.
    }

    private void persistProbe(Long id, String name) {
        var entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(factory.getObject());
        entityManager.persist(new VisibilityProbe(id, name));
        entityManager.flush();
    }

    private void insertProbe(Long id, String name) {
        jdbc.update("insert into visibility_probe (id, name) values (?, ?)", id, name);
    }

    private List<String> probeNames() {
        return jdbc.queryForList("select name from visibility_probe order by id", String.class);
    }

    private record ProbeLockingService(JdbcTemplate jdbc) implements LockingService {

        @Override
        public void applyLock(List<Long> loanIds, LockOwner lockOwner) {
            loanIds.forEach(id -> jdbc.update("insert into probe_lock (loan_id, lock_owner) values (?, ?)", id, lockOwner.name()));
        }

        @Override
        public List<Long> findLockIdsByLoanIdIn(List<Long> loanIds) {
            return jdbc.queryForList("select loan_id from probe_lock", Long.class).stream().filter(loanIds::contains).toList();
        }

        @Override
        public void upgradeLock(List<Long> accountsToLock, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Long> findLockIdsByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateLockError(Long loanId, LockOwner lockOwner, String error, String stacktrace) {
            throw new UnsupportedOperationException();
        }
    }
}
