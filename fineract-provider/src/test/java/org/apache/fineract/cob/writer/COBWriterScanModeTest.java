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
package org.apache.fineract.cob.writer;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.listener.ChunkProcessingLoanItemListener;
import org.apache.fineract.cob.writer.probe.CobProbeAccount;
import org.apache.fineract.cob.writer.probe.CobProbeAccountRepository;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.persistence.ExtendedDataSourceTransactionManager;
import org.apache.fineract.infrastructure.core.persistence.ExtendedJpaTransactionManager;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSource;
import org.apache.fineract.infrastructure.jobs.ScheduledJobRunnerConfig;
import org.apache.fineract.infrastructure.springbatch.StepRetryPolicyFactory;
import org.eclipse.persistence.config.PersistenceUnitProperties;
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
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the COB item writer and lock listener in a real fault-tolerant Spring Batch 6 step, over EclipseLink and
 * PostgreSQL, configured the way the COB worker steps are: no processor, no retries, every exception skippable.
 * <p>
 * Pins the behaviour COB depends on: when one account of a chunk fails, the chunk is rolled back and every account is
 * written again in a transaction of its own, so only the failing account is skipped and stays locked with its error,
 * while every other account of the chunk is closed exactly once.
 */
@Testcontainers(disabledWithoutDocker = true)
class COBWriterScanModeTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 10, 6);
    private static final LocalDate PREVIOUS_BUSINESS_DATE = BUSINESS_DATE.minusDays(1);
    private static final LockOwner LOCK_OWNER = LockOwner.LOAN_COB_CHUNK_PROCESSING;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private HikariDataSource pool;
    private LocalContainerEntityManagerFactoryBean factory;
    private AnnotationConfigApplicationContext context;
    private ExtendedJpaTransactionManager transactionManager;
    private ExtendedDataSourceTransactionManager jdbcTransactionManager;
    private JobRepository jobRepository;
    private JdbcTemplate jdbc;
    private CobProbeAccountRepository accounts;
    private ProbeBusinessSteps businessSteps;
    private ProbeLockingService lockingService;

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
        jdbc.execute("create table cob_probe_account (id bigint primary key, version bigint not null, runs int not null, "
                + "last_closed_business_date date)");
        jdbc.execute("create table cob_probe_lock (loan_id bigint primary key, lock_owner varchar(50) not null, error varchar(500), "
                + "stacktrace text)");

        factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPersistenceUnitName("cob-scan-" + UUID.randomUUID());
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(CobProbeAccount.class.getPackageName());
        factory.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        // The persistence settings JPAConfig gives the application, without which a reload could be served from a
        // cache.
        factory.setJpaPropertyMap(Map.of(PersistenceUnitProperties.WEAVING, "false", "eclipselink.logging.level", "SEVERE",
                PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false", PersistenceUnitProperties.PERSISTENCE_CONTEXT_CLOSE_ON_COMMIT,
                "true"));
        factory.afterPropertiesSet();
        transactionManager = new ExtendedJpaTransactionManager(false);
        transactionManager.setEntityManagerFactory(factory.getObject());
        // As JpaTransactionConfig and JdbcTransactionConfig configure them.
        transactionManager.setValidateExistingTransaction(true);
        transactionManager.afterPropertiesSet();
        jdbcTransactionManager = new ExtendedDataSourceTransactionManager(false);
        jdbcTransactionManager.setDataSource(dataSource);
        jdbcTransactionManager.setValidateExistingTransaction(true);

        context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("dataSource", dataSource);
        context.getBeanFactory().registerSingleton("transactionManager", transactionManager);
        context.getBeanFactory().registerSingleton("jdbcTransactionManager", jdbcTransactionManager);
        context.register(ScheduledJobRunnerConfig.class);
        context.refresh();
        jobRepository = context.getBean(JobRepository.class);

        accounts = new JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(factory.getObject()))
                .getRepository(CobProbeAccountRepository.class);
        businessSteps = new ProbeBusinessSteps(new TransactionTemplate(transactionManager));
        lockingService = new ProbeLockingService(jdbc, new CopyOnWriteArrayList<>());
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
    void aFailingAccountIsSkippedAndTheRestOfItsChunkIsClosedExactlyOnce() throws Exception {
        lockedAccounts(1L, 2L, 3L);
        businessSteps.beforeRun = account -> {
            if (account.getId() == 2L) {
                throw new BusinessStepException("business step failed for account 2");
            }
        };

        StepExecution result = runWorkerStep(List.of(1L, 2L, 3L), 3);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(result.getWriteSkipCount()).isEqualTo(1);
        assertThat(result.getProcessSkipCount()).isZero();
        // Accounts 1 and 3 ran their steps in the rolled-back chunk transaction as well; only one run may survive.
        assertThat(account(1L)).containsEntry("runs", 1).containsEntry("last_closed_business_date", Date.valueOf(BUSINESS_DATE));
        assertThat(account(3L)).containsEntry("runs", 1).containsEntry("last_closed_business_date", Date.valueOf(BUSINESS_DATE));
        assertThat(account(2L)).containsEntry("runs", 0).containsEntry("last_closed_business_date", Date.valueOf(PREVIOUS_BUSINESS_DATE));
        assertThat(lockedAccountIds()).containsExactly(2L);
        // An error makes a lock overrulable by inline COB. Accounts 1 and 3 were still to be written in scan mode, so
        // an
        // error on their locks would have let inline COB close them for this date before the step closed them again.
        assertThat(lockingService.errorsRecordedFor()).containsExactly(2L);
        Map<String, Object> lock = jdbc.queryForMap("select error, stacktrace from cob_probe_lock where loan_id = 2");
        assertThat(lock.get("error")).isEqualTo("Loan (id: 2) writing is failed");
        assertThat((String) lock.get("stacktrace")).contains("business step failed for account 2");
    }

    @Test
    void anOptimisticLockConflictRaisedAtFlushIsRecoveredByWritingTheAccountAgain() throws Exception {
        lockedAccounts(1L, 2L);
        AtomicBoolean concurrentUpdateDone = new AtomicBoolean();
        businessSteps.beforeRun = account -> {
            if (account.getId() == 1L && concurrentUpdateDone.compareAndSet(false, true)) {
                updateVersionConcurrently(1L);
            }
        };

        StepExecution result = runWorkerStep(List.of(1L, 2L), 2);

        // The conflict surfaced inside the write thanks to the flush, so scan mode took over and account 1 succeeded
        // on its own against freshly read state. Without the flush it would surface at commit and fail the step.
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(result.getWriteSkipCount()).isZero();
        assertThat(result.getRollbackCount()).isPositive();
        assertThat(account(1L)).containsEntry("runs", 1).containsEntry("version", 2L);
        assertThat(account(2L)).containsEntry("runs", 1);
        assertThat(lockedAccountIds()).isEmpty();
    }

    @Test
    void theErrorIsSavedEvenWhenTheFailedTransactionHeldTheLockRow() throws Exception {
        // A failed attempt that had written to the account's lock row holds a row lock on it until it rolls back; an
        // error
        // saved while it is still open would wait behind it. Fail fast instead of hanging if that ever comes back.
        jdbc.execute("ALTER DATABASE " + POSTGRES.getDatabaseName() + " SET lock_timeout = '3s'");
        pool.getHikariPoolMXBean().softEvictConnections();
        lockedAccounts(1L, 2L);
        businessSteps.beforeRun = account -> {
            if (account.getId() == 2L) {
                jdbc.update("update cob_probe_lock set error = 'in flight' where loan_id = 2");
                throw new BusinessStepException("business step failed for account 2");
            }
        };

        StepExecution result = runWorkerStep(List.of(1L, 2L), 2);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(lockedAccountIds()).containsExactly(2L);
        assertThat(jdbc.queryForObject("select error from cob_probe_lock where loan_id = 2", String.class))
                .isEqualTo("Loan (id: 2) writing is failed");
    }

    @Test
    void anAccountThatCannotBeLoadedIsSkippedAloneInsteadOfSilentlyLosingItsChunk() throws Exception {
        // Reading hands out ids only; the account is loaded in the write, where its failure enters scan mode.
        lockedAccounts(1L, 2L, 3L);
        jdbc.execute("delete from cob_probe_account where id = 2");

        StepExecution result = runWorkerStep(List.of(1L, 2L, 3L), 3);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(result.getWriteSkipCount()).isEqualTo(1);
        assertThat(account(1L)).containsEntry("runs", 1).containsEntry("last_closed_business_date", Date.valueOf(BUSINESS_DATE));
        assertThat(account(3L)).containsEntry("runs", 1).containsEntry("last_closed_business_date", Date.valueOf(BUSINESS_DATE));
        assertThat(lockedAccountIds()).containsExactly(2L);
        assertThat(jdbc.queryForObject("select stacktrace from cob_probe_lock where loan_id = 2", String.class)).contains("does not exist");
    }

    @Test
    void theAccountThatExceedsTheSkipLimitIsLeftWithItsOwnError() throws Exception {
        lockedAccounts(1L, 2L, 3L);
        businessSteps.beforeRun = account -> {
            if (account.getId() != 1L) {
                throw new BusinessStepException("business step failed for account " + account.getId());
            }
        };

        StepExecution result = runWorkerStep(List.of(1L, 2L, 3L), 3, 1);

        assertThat(result.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(account(1L)).containsEntry("runs", 1).containsEntry("last_closed_business_date", Date.valueOf(BUSINESS_DATE));
        assertThat(lockedAccountIds()).containsExactly(2L, 3L);
        // Account 2 is skipped; account 3 then exceeds the skip limit, which Batch reports to no item listener.
        for (Long id : List.of(2L, 3L)) {
            Map<String, Object> lock = jdbc.queryForMap("select error, stacktrace from cob_probe_lock where loan_id = ?", id);
            assertThat(lock.get("error")).isEqualTo("Loan (id: " + id + ") writing is failed");
            assertThat((String) lock.get("stacktrace")).startsWith(BusinessStepException.class.getName())
                    .contains("business step failed for account " + id);
        }
    }

    private StepExecution runWorkerStep(List<Long> ids, int chunkSize) throws Exception {
        return runWorkerStep(ids, chunkSize, 101);
    }

    private StepExecution runWorkerStep(List<Long> ids, int chunkSize, int skipLimit) throws Exception {
        Queue<Long> remaining = new ArrayDeque<>(ids);
        ItemReader<Long> reader = remaining::poll;
        ExecutionContext jobContext = new ExecutionContext();
        jobContext.put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, BUSINESS_DATE);
        JobExecution job = jobRepository.createJobExecution(
                jobRepository.createJobInstance("cob-" + UUID.randomUUID(), new JobParameters()), new JobParameters(), jobContext);
        StepExecution execution = jobRepository.createStepExecution("worker", job);
        Step step = new StepBuilder("worker", jobRepository).<Long, Long>chunk(chunkSize).transactionManager(transactionManager) //
                .reader(reader) //
                .writer(new ProbeWriter(businessSteps, lockingService, accounts)) //
                .faultTolerant() //
                .retryPolicy(StepRetryPolicyFactory.noRetryPolicy()) //
                .skip(Exception.class) //
                .skipLimit(skipLimit) //
                .listener(new ChunkProcessingLoanItemListener(lockingService, requiresNewTransactionTemplate())) //
                .build();
        step.execute(execution);
        return jobRepository.getStepExecution(execution.getId());
    }

    private void lockedAccounts(Long... ids) {
        for (Long id : ids) {
            jdbc.update("insert into cob_probe_account (id, version, runs, last_closed_business_date) values (?, 0, 0, ?)", id,
                    PREVIOUS_BUSINESS_DATE);
            jdbc.update("insert into cob_probe_lock (loan_id, lock_owner) values (?, ?)", id, LOCK_OWNER.name());
        }
    }

    private Map<String, Object> account(Long id) {
        return jdbc.queryForMap("select runs, version, last_closed_business_date from cob_probe_account where id = ?", id);
    }

    private List<Long> lockedAccountIds() {
        return jdbc.queryForList("select loan_id from cob_probe_lock order by loan_id", Long.class);
    }

    /** Commits on a connection of its own, as another node updating the account would. */
    private void updateVersionConcurrently(Long id) {
        try (Connection connection = pool.getConnection(); Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.executeUpdate("update cob_probe_account set version = version + 1 where id = " + id);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private TransactionTemplate requiresNewTransactionTemplate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    /** Business steps that join the chunk transaction the way {@code COBBusinessStepServiceImpl.run} does. */
    private static final class ProbeBusinessSteps implements COBBusinessStepService {

        private final TransactionTemplate participating;
        private Consumer<CobProbeAccount> beforeRun = ProbeBusinessSteps::runNormally;

        private ProbeBusinessSteps(TransactionTemplate participating) {
            this.participating = participating;
        }

        @Override
        public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom<Long>> S run(TreeMap<Long, String> executionMap, S item) {
            return participating.execute(status -> {
                CobProbeAccount account = (CobProbeAccount) item;
                beforeRun.accept(account);
                account.recordRun();
                return item;
            });
        }

        private static void runNormally(CobProbeAccount account) {
            // No extra behaviour before the business step.
        }

        @Override
        public <T extends COBBusinessStep<S>, S extends AbstractPersistableCustom<Long>> Set<BusinessStepNameAndOrder> getCOBBusinessSteps(
                Class<T> businessStepClass, String cobJobName) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ProbeWriter extends AbstractCOBItemWriter<CobProbeAccount> {

        private ProbeWriter(COBBusinessStepService businessSteps, LockingService lockingService, CobProbeAccountRepository accounts) {
            super(businessSteps, lockingService, accounts);
        }

        @Override
        public void beforeStep(StepExecution stepExecution) {
            ExecutionContext stepContext = new ExecutionContext();
            stepContext.put(COBConstant.BUSINESS_STEPS, Set.of(new BusinessStepNameAndOrder("probeStep", 1L)));
            setExecutionContext(stepContext);
            setBusinessDate(stepExecution);
        }

        @Override
        protected void setLastRun(CobProbeAccount processedItem) {
            processedItem.setLastClosedBusinessDate(getBusinessDate());
        }

        @Override
        protected LockOwner getLockOwner() {
            return LOCK_OWNER;
        }
    }

    private record ProbeLockingService(JdbcTemplate jdbc, List<Long> errorsRecordedFor) implements LockingService {

        @Override
        public void deleteByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
            loanIds.forEach(id -> jdbc.update("delete from cob_probe_lock where loan_id = ? and lock_owner = ?", id, lockOwner.name()));
        }

        @Override
        public void updateLockError(Long loanId, LockOwner lockOwner, String error, String stacktrace) {
            errorsRecordedFor.add(loanId);
            jdbc.update("update cob_probe_lock set error = ?, stacktrace = ? where loan_id = ? and lock_owner = ?", error, stacktrace,
                    loanId, lockOwner.name());
        }

        @Override
        public void upgradeLock(List<Long> accountsToLock, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Long> findLockIdsByLoanIdIn(List<Long> loanIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Long> findLockIdsByLoanIdInAndLockOwner(List<Long> loanIds, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void applyLock(List<Long> loanIds, LockOwner lockOwner) {
            throw new UnsupportedOperationException();
        }
    }
}
