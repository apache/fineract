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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.COBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.LockCannotBeAppliedException;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Pins {@link RequiresNewJdbcTransactionTemplate} against a real EclipseLink JPA transaction manager and a real JDBC
 * transaction manager on one PostgreSQL {@code DataSource}: it refuses to start inside a JPA transaction, where it
 * would otherwise run on the JPA transaction's connection, and behaves as a plain, independent {@code REQUIRES_NEW}
 * everywhere else - including in the real COB worker path that uses it to apply the account locks.
 */
@Testcontainers(disabledWithoutDocker = true)
class RequiresNewJdbcTransactionTemplateTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource pool;
    private LocalContainerEntityManagerFactoryBean factory;
    private AnnotationConfigApplicationContext context;
    private ExtendedJpaTransactionManager jpaTransactionManager;
    private ExtendedDataSourceTransactionManager jdbcTransactionManager;
    private RequiresNewJdbcTransactionTemplate requiresNewJdbc;
    private JobRepository jobRepository;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
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
        // See BatchJpaTransactionTest: Fineract keeps the Spring Batch 5 job instance sequence name.
        jdbc.execute("ALTER SEQUENCE BATCH_JOB_INSTANCE_SEQ RENAME TO BATCH_JOB_SEQ");
        jdbc.execute("create table visibility_probe (id bigint primary key, name varchar(255))");
        jdbc.execute("create table probe_lock (loan_id bigint primary key, lock_owner varchar(50) not null)");

        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("requires-new-jdbc-" + UUID.randomUUID());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(VisibilityProbe.class.getPackageName());
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("eclipselink.weaving", "false", "eclipselink.logging.level", "SEVERE"));
        factory.afterPropertiesSet();
        jpaTransactionManager = new ExtendedJpaTransactionManager(false);
        jpaTransactionManager.setEntityManagerFactory(factory.getObject());
        jpaTransactionManager.afterPropertiesSet();
        jdbcTransactionManager = new ExtendedDataSourceTransactionManager(false);
        jdbcTransactionManager.setDataSource(dataSource);
        requiresNewJdbc = new RequiresNewJdbcTransactionTemplate(jdbcTransactionManager);

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
    void outsideAnyTransactionItCommits() {
        requiresNewJdbc.executeWithoutResult(status -> insertProbe(1L, "jdbc"));

        assertThat(probeNames()).containsExactly("jdbc");
    }

    @Test
    void insideAJpaTransactionItRefusesBeforeRunningAndLeavesTheJpaTransactionIntact() {
        AtomicBoolean callbackRan = new AtomicBoolean();

        new TransactionTemplate(jpaTransactionManager).executeWithoutResult(jpa -> {
            persistProbe(1L, "jpa");
            assertThatThrownBy(() -> requiresNewJdbc.executeWithoutResult(status -> callbackRan.set(true)))
                    .isInstanceOf(IllegalTransactionStateException.class).hasMessageContaining("requiresNewTransactionTemplate");
            assertThat(jpa.isRollbackOnly()).as("the refusal must not doom the JPA transaction").isFalse();
        });

        assertThat(callbackRan).isFalse();
        assertThat(probeNames()).containsExactly("jpa");
    }

    @Test
    void insideAJpaTransactionWhoseConnectionJdbcAlreadyUsedItRefuses() {
        new TransactionTemplate(jpaTransactionManager).executeWithoutResult(jpa -> {
            // Materialises the JPA transaction's connection through DataSourceUtils, as JdbcTemplate code inside a
            // JPA transaction does all over Fineract.
            insertProbe(1L, "jdbc in jpa");
            assertThatThrownBy(() -> requiresNewJdbc.executeWithoutResult(status -> insertProbe(2L, "never")))
                    .isInstanceOf(IllegalTransactionStateException.class);
        });

        assertThat(probeNames()).containsExactly("jdbc in jpa");
    }

    @Test
    void insideAJdbcTransactionItSuspendsItAndCommitsIndependently() {
        assertThatThrownBy(() -> new TransactionTemplate(jdbcTransactionManager).executeWithoutResult(outer -> {
            insertProbe(1L, "outer");
            requiresNewJdbc.executeWithoutResult(inner -> insertProbe(2L, "inner"));
            throw new IllegalStateException("outer rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(probeNames()).containsExactly("inner");
    }

    @Test
    void whereTheJpaTransactionIsSuspendedItRunsIndependently() {
        TransactionTemplate notSupported = new TransactionTemplate(jpaTransactionManager);
        notSupported.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        assertThatThrownBy(() -> new TransactionTemplate(jpaTransactionManager).executeWithoutResult(jpa -> {
            persistProbe(1L, "jpa");
            notSupported.executeWithoutResult(suspended -> requiresNewJdbc.executeWithoutResult(status -> insertProbe(2L, "jdbc")));
            throw new IllegalStateException("jpa rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(probeNames()).containsExactly("jdbc");
    }

    @Test
    void theCobWorkerStepListenerAppliesTheLocksThroughIt() throws Exception {
        // The real COB path: CobWorkerStepListener.beforeStep -> ApplyLoanLockTasklet -> this template, in a
        // chunk-oriented step on the JPA transaction manager, as LoanCOBWorkerConfiguration builds it.
        CobWorkerStepListener listener = new CobWorkerStepListener((contribution, chunkContext) -> RepeatStatus.FINISHED,
                applyLoanLockTasklet(), (contribution, chunkContext) -> RepeatStatus.FINISHED);
        Step step = new StepBuilder("worker", jobRepository).<Long, Long>chunk(10).transactionManager(jpaTransactionManager)
                .reader(new IteratorItemReader<>(List.<Long>of())).writer(RequiresNewJdbcTransactionTemplateTest::writeNothing)
                .listener(listener).build();

        StepExecution execution = workerStepExecution();
        step.execute(execution);

        assertThat(jobRepository.getStepExecution(execution.getId()).getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jdbc.queryForList("select loan_id from probe_lock order by loan_id", Long.class)).containsExactly(1L, 2L);
    }

    @Test
    void theLockTaskletFailsLoudlyIfItIsEverRunInsideAJpaTransaction() throws Exception {
        // What the guard turns a misuse into: as a tasklet step on the JPA transaction manager, every attempt is
        // refused, the tasklet's own retries run out, and the step fails without having written a single lock.
        Step step = new StepBuilder("worker", jobRepository).tasklet(applyLoanLockTasklet(), jpaTransactionManager).build();

        StepExecution execution = workerStepExecution();
        step.execute(execution);

        StepExecution result = jobRepository.getStepExecution(execution.getId());
        assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(execution.getFailureExceptions())
                .anySatisfy(failure -> assertThat(failure).hasRootCauseInstanceOf(IllegalTransactionStateException.class)
                        .satisfies(f -> assertThat(causeChain(f)).anyMatch(LockCannotBeAppliedException.class::isInstance)));
        assertThat(jdbc.queryForObject("select count(*) from probe_lock", Integer.class)).isZero();
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

    private static List<Throwable> causeChain(Throwable failure) {
        List<Throwable> chain = new ArrayList<>();
        for (Throwable current = failure; current != null && !chain.contains(current); current = current.getCause()) {
            chain.add(current);
        }
        return chain;
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
