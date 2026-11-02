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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the Execute Standing Instruction tasklet against PostgreSQL with the real due-instruction query, the real
 * history and last_run_date writes and real transactions. Only the transfer step is stubbed: it moves balances in a
 * minimal savings table inside the transaction it is called in, crediting before it debits, so a failure after the
 * credit shows whether the transfer is rolled back. The tasklet runs inside an outer transaction that rolls back when
 * the job fails, as the Spring Batch step transaction does.
 */
@Testcontainers(disabledWithoutDocker = true)
class ExecuteStandingInstructionsHistoryPostgresTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final int PRIORITY_HIGH = 2;
    private static final int PRIORITY_LOW = 4;

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private final AccountTransfersWritePlatformService transfers = mock(AccountTransfersWritePlatformService.class);
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate stepTransaction;
    private ExecuteStandingInstructionsTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));

        final DriverManagerDataSource dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword());
        jdbcTemplate = new JdbcTemplate(dataSource);
        createSchema();

        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        stepTransaction = new TransactionTemplate(transactionManager);
        final TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        final DatabaseSpecificSQLGenerator sqlGenerator = mock(DatabaseSpecificSQLGenerator.class);
        when(sqlGenerator.currentBusinessDate()).thenReturn("date '" + BUSINESS_DATE + "'");
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
        final StandingInstructionReadPlatformServiceImpl readService = new StandingInstructionReadPlatformServiceImpl(jdbcTemplate, null,
                null, null, null, null, sqlGenerator, null);

        tasklet = new ExecuteStandingInstructionsTasklet(readService, jdbcTemplate, sqlGenerator, transfers,
                mock(SavingsAccountAssembler.class), requiresNew);
        doAnswer(invocation -> {
            moveFunds(invocation.getArgument(0));
            return 1L;
        }).when(transfers).transferFunds(any(AccountTransferDTO.class));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void failureDescriptionWithAnApostropheIsStoredLiterallyAndTheNextInstructionStillPosts() {
        final String description = "Transfer rejected: customer's account is under review";
        final long failing = createInstruction(1, PRIORITY_LOW, "25.00", false, "500.00");
        final long funded = createInstruction(2, PRIORITY_HIGH, "30.00", false, "100.00");
        doAnswer(invocation -> {
            final AccountTransferDTO transfer = invocation.getArgument(0);
            if (transfer.getFromAccountId() == sourceOf(failing)) {
                throw new IllegalStateException(description);
            }
            moveFunds(transfer);
            return 1L;
        }).when(transfers).transferFunds(any(AccountTransferDTO.class));
        assertThat(historyCount(failing)).isZero();
        assertThat(historyCount(funded)).isZero();

        assertThrows(JobExecutionException.class, this::runJobInStepTransaction);

        assertThat(historyCount(failing)).isEqualTo(1);
        assertThat(historyColumn(failing, "status", String.class)).isEqualTo("failed");
        assertThat(historyColumn(failing, "error_log", String.class)).contains(description);
        assertThat(historyColumn(failing, "amount", BigDecimal.class)).isEqualByComparingTo("25.00");
        assertThat(lastRunDate(failing)).isNull();
        assertThat(balance(sourceOf(failing))).isEqualByComparingTo("500.00");
        assertThat(balance(destinationOf(failing))).isEqualByComparingTo("0.00");

        assertThat(historyCount(funded)).isEqualTo(1);
        assertThat(historyColumn(funded, "status", String.class)).isEqualTo("success");
        assertThat(historyColumn(funded, "amount", BigDecimal.class)).isEqualByComparingTo("30.00");
        assertThat(lastRunDate(funded)).isEqualTo(BUSINESS_DATE);
        assertThat(balance(sourceOf(funded))).isEqualByComparingTo("70.00");
        assertThat(balance(destinationOf(funded))).isEqualByComparingTo("30.00");
    }

    @Test
    void failedRowRecordsTheExactAttemptedAmount() {
        final BigDecimal amount = new BigDecimal("9000000000000.000001");
        final long instruction = createInstruction(1, PRIORITY_LOW, amount.toPlainString(), false, "100.00");
        assertThat(jdbcTemplate.queryForObject("select amount from m_account_transfer_standing_instructions where id = ?", BigDecimal.class,
                instruction)).isEqualTo(amount);
        assertThat(jdbcTemplate.queryForObject("select allow_partial_transfer from m_account_transfer_standing_instructions where id = ?",
                Boolean.class, instruction)).isFalse();
        assertThat(historyCount(instruction)).isZero();

        assertThrows(JobExecutionException.class, this::runJobInStepTransaction);

        assertThat(historyCount(instruction)).isEqualTo(1);
        assertThat(historyColumn(instruction, "status", String.class)).isEqualTo("failed");
        assertThat(historyColumn(instruction, "error_log", String.class)).contains("InsufficientAccountBalance");
        assertThat(historyColumn(instruction, "amount", BigDecimal.class)).isEqualTo(amount);
        assertThat(balance(sourceOf(instruction))).isEqualByComparingTo("100.00");
        assertThat(balance(destinationOf(instruction))).isEqualByComparingTo("0.00");
        assertThat(lastRunDate(instruction)).isNull();
    }

    @Test
    void failureDescriptionLongerThanTheErrorLogColumnStillProducesAFailedRow() {
        final String description = "Transfer rejected: " + "x".repeat(600);
        final long failing = createInstruction(1, PRIORITY_LOW, "25.00", false, "500.00");
        final long funded = createInstruction(2, PRIORITY_HIGH, "30.00", false, "100.00");
        doAnswer(invocation -> {
            final AccountTransferDTO transfer = invocation.getArgument(0);
            if (transfer.getFromAccountId() == sourceOf(failing)) {
                throw new IllegalStateException(description);
            }
            moveFunds(transfer);
            return 1L;
        }).when(transfers).transferFunds(any(AccountTransferDTO.class));
        assertThat(description).hasSizeGreaterThan(500);
        assertThat(historyCount(failing)).isZero();

        assertThrows(JobExecutionException.class, this::runJobInStepTransaction);

        assertThat(historyCount(failing)).isEqualTo(1);
        assertThat(historyColumn(failing, "status", String.class)).isEqualTo("failed");
        assertThat(historyColumn(failing, "amount", BigDecimal.class)).isEqualByComparingTo("25.00");
        final String errorLog = historyColumn(failing, "error_log", String.class);
        assertThat(errorLog).hasSize(500).contains("Transfer rejected: xxxx");
        assertThat(balance(sourceOf(failing))).isEqualByComparingTo("500.00");
        assertThat(balance(destinationOf(failing))).isEqualByComparingTo("0.00");

        assertThat(historyCount(funded)).isEqualTo(1);
        assertThat(historyColumn(funded, "status", String.class)).isEqualTo("success");
        assertThat(balance(destinationOf(funded))).isEqualByComparingTo("30.00");
    }

    /** Runs the tasklet the way Spring Batch does: in a transaction that is rolled back when the tasklet throws. */
    private void runJobInStepTransaction() throws Exception {
        final AtomicReference<Exception> failure = new AtomicReference<>();
        stepTransaction.executeWithoutResult(status -> {
            try {
                tasklet.execute(mock(StepContribution.class), mock(ChunkContext.class));
            } catch (Exception e) {
                failure.set(e);
                status.setRollbackOnly();
            }
        });
        if (failure.get() != null) {
            throw failure.get();
        }
    }

    /** The stubbed transfer step: credits the destination, then debits the source if it can cover the amount. */
    private void moveFunds(final AccountTransferDTO transfer) {
        final BigDecimal amount = transfer.getTransactionAmount();
        jdbcTemplate.update("update m_savings_account set account_balance_derived = account_balance_derived + ? where id = ?", amount,
                transfer.getToAccountId());
        final BigDecimal available = balance(transfer.getFromAccountId());
        if (available.compareTo(amount) < 0) {
            throw new InsufficientAccountBalanceException("transactionAmount", available, null, amount);
        }
        jdbcTemplate.update("update m_savings_account set account_balance_derived = account_balance_derived - ? where id = ?", amount,
                transfer.getFromAccountId());
    }

    /** Creates a monthly fixed-amount transfer due on the business date between two new savings accounts. */
    private long createInstruction(final long id, final int priority, final String amount, final boolean allowPartialTransfer,
            final String sourceBalance) {
        final long source = id * 10 + 1;
        final long destination = id * 10 + 2;
        jdbcTemplate.update("insert into m_savings_account (id, account_no, product_id, account_balance_derived) values (?, ?, 1, ?)",
                source, "S" + source, new BigDecimal(sourceBalance));
        jdbcTemplate.update("insert into m_savings_account (id, account_no, product_id, account_balance_derived) values (?, ?, 1, 0)",
                destination, "S" + destination);
        jdbcTemplate.update("""
                insert into m_account_transfer_details (id, from_office_id, to_office_id, from_client_id, to_client_id,
                    from_savings_account_id, to_savings_account_id, transfer_type)
                values (?, 1, 1, 1, 1, ?, ?, 1)
                """, id, source, destination);
        jdbcTemplate.update("""
                insert into m_account_transfer_standing_instructions (id, name, account_transfer_details_id, priority, status,
                    instruction_type, amount, valid_from, recurrence_type, recurrence_frequency, recurrence_interval,
                    recurrence_on_day, recurrence_on_month, allow_partial_transfer)
                values (?, ?, ?, ?, 1, 1, ?, ?, 1, 2, 1, ?, ?, ?)
                """, id, "Instruction " + id, id, priority, new BigDecimal(amount), BUSINESS_DATE.minusMonths(2),
                BUSINESS_DATE.getDayOfMonth(), BUSINESS_DATE.getMonthValue(), allowPartialTransfer);
        return id;
    }

    private long sourceOf(final long instructionId) {
        return instructionId * 10 + 1;
    }

    private long destinationOf(final long instructionId) {
        return instructionId * 10 + 2;
    }

    private BigDecimal balance(final long savingsAccountId) {
        return jdbcTemplate.queryForObject("select account_balance_derived from m_savings_account where id = ?", BigDecimal.class,
                savingsAccountId);
    }

    private int historyCount(final long instructionId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from m_account_transfer_standing_instructions_history where standing_instruction_id = ?", Integer.class,
                instructionId);
    }

    private <T> T historyColumn(final long instructionId, final String column, final Class<T> type) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from m_account_transfer_standing_instructions_history where standing_instruction_id = ?", type,
                instructionId);
    }

    private LocalDate lastRunDate(final long instructionId) {
        return jdbcTemplate.queryForObject("select last_run_date from m_account_transfer_standing_instructions where id = ?",
                LocalDate.class, instructionId);
    }

    /** The tables the due-instruction query joins, with the column types of the tenant schema. */
    private void createSchema() {
        jdbcTemplate.execute("""
                drop table if exists m_account_transfer_standing_instructions_history, m_account_transfer_standing_instructions,
                    m_account_transfer_details, m_savings_account, m_savings_product, m_loan, m_product_loan, m_client, m_office
                """);
        jdbcTemplate.execute("create table m_office (id bigint primary key, name varchar(50) not null)");
        jdbcTemplate.execute("create table m_client (id bigint primary key, display_name varchar(100) not null)");
        jdbcTemplate.execute("create table m_savings_product (id bigint primary key, name varchar(100) not null)");
        jdbcTemplate.execute("""
                create table m_savings_account (id bigint primary key, account_no varchar(20) not null,
                    product_id bigint references m_savings_product(id), account_balance_derived numeric(19, 6) not null)
                """);
        jdbcTemplate.execute("create table m_product_loan (id bigint primary key, name varchar(100) not null)");
        jdbcTemplate.execute("create table m_loan (id bigint primary key, account_no varchar(20) not null, product_id bigint)");
        jdbcTemplate.execute(
                """
                        create table m_account_transfer_details (id bigint primary key, from_office_id bigint not null, to_office_id bigint not null,
                            from_client_id bigint not null, to_client_id bigint not null, from_savings_account_id bigint, from_loan_account_id bigint,
                            to_savings_account_id bigint, to_loan_account_id bigint, transfer_type smallint)
                        """);
        jdbcTemplate.execute("""
                create table m_account_transfer_standing_instructions (id bigint primary key, name varchar(250) not null,
                    account_transfer_details_id bigint not null references m_account_transfer_details(id), priority smallint not null,
                    status smallint not null, instruction_type smallint not null, amount numeric(19, 6), valid_from date not null,
                    valid_till date, recurrence_type smallint not null, recurrence_frequency smallint, recurrence_interval smallint,
                    recurrence_on_day smallint, recurrence_on_month smallint, last_run_date date,
                    allow_partial_transfer boolean default false not null)
                """);
        jdbcTemplate.execute("""
                create table m_account_transfer_standing_instructions_history (id bigserial primary key,
                    standing_instruction_id bigint not null, status varchar(20) not null, execution_time timestamp(6) not null,
                    amount numeric(19, 6) not null, error_log varchar(500))
                """);
        jdbcTemplate.update("insert into m_office (id, name) values (1, 'Head Office')");
        jdbcTemplate.update("insert into m_client (id, display_name) values (1, 'Test client')");
        jdbcTemplate.update("insert into m_savings_product (id, name) values (1, 'Plain savings')");
    }
}
