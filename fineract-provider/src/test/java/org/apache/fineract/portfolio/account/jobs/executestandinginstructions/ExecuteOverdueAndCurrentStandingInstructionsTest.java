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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class ExecuteOverdueAndCurrentStandingInstructionsTest {

    private static final Long INSTRUCTION_ID = 1L;
    private static final Long FROM_SAVINGS_ACCOUNT_ID = 11L;
    private static final Long TO_LOAN_ACCOUNT_ID = 22L;
    private static final BigDecimal INSTALLMENT_DUE = BigDecimal.valueOf(40000);
    private static final BigDecimal JUST_UNDER_THE_INSTALLMENT = BigDecimal.valueOf(39900);
    private static final BigDecimal MORE_THAN_THE_INSTALLMENT = BigDecimal.valueOf(50000);

    private final LocalDate currentDate = LocalDate.now(Clock.systemUTC());
    private final LocalDate previousDate = currentDate.minusDays(2);

    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService = mock(
            StandingInstructionReadPlatformService.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final DatabaseSpecificSQLGenerator sqlGenerator = mock(DatabaseSpecificSQLGenerator.class);
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService = mock(
            AccountTransfersWritePlatformService.class);
    private final SavingsAccountAssembler savingsAccountAssembler = mock(SavingsAccountAssembler.class);

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Africa/Kampala", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, currentDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testAcceptPreviousDateAsDue() {
        boolean isDueForTransfer = tasklet().isDueForTransfer(new StandingInstructionDuesData(previousDate, BigDecimal.ONE));
        assertThat(isDueForTransfer).isTrue().describedAs("Earlier instructions are accepted as due");
    }

    @Test
    public void testAcceptCurrentDateAsDue() {
        boolean isDueForTransfer = tasklet().isDueForTransfer(new StandingInstructionDuesData(currentDate, BigDecimal.ONE));
        assertThat(isDueForTransfer).isTrue().describedAs("Current day instructions are accepted as due");
    }

    /**
     * Without the opt-in, an instruction that cannot be covered in full still attempts the whole amount, is rejected
     * outright, and collects nothing. This is the behaviour FINERACT-2400 leaves untouched by default.
     */
    @Test
    public void insufficientBalanceAttemptsFullAmountAndCollectsNothing() {
        givenAnInstructionDueToday(false);
        doThrow(new InsufficientAccountBalanceException("transactionAmount", JUST_UNDER_THE_INSTALLMENT, null, INSTALLMENT_DUE))
                .when(accountTransfersWritePlatformService).transferFunds(any(AccountTransferDTO.class));

        assertThrows(JobExecutionException.class, this::runTheJob);

        assertThat(transferredAmount()).isEqualByComparingTo(INSTALLMENT_DUE)
                .describedAs("the whole installment is attempted even though only part of it is available");
        assertThat(historyRow()).contains("'failed'");
        verifyNoInteractions(savingsAccountAssembler);
    }

    @Test
    public void partialTransferIsLimitedToTheAvailableBalance() throws Exception {
        givenAnInstructionDueToday(true);
        givenAWithdrawableBalanceOf(JUST_UNDER_THE_INSTALLMENT);

        runTheJob();

        assertThat(transferredAmount()).isEqualByComparingTo(JUST_UNDER_THE_INSTALLMENT)
                .describedAs("only what the savings account can release is transferred");
        assertThat(historyRow()).contains("'partial'").describedAs("a partly covered installment is distinguishable from a full one");
    }

    @Test
    public void partialTransferIsSkippedWhenNothingIsAvailable() throws Exception {
        givenAnInstructionDueToday(true);
        givenAWithdrawableBalanceOf(BigDecimal.ZERO);

        runTheJob();

        verifyNoInteractions(accountTransfersWritePlatformService);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    public void fullAmountIsTransferredWhenTheBalanceCoversIt() throws Exception {
        givenAnInstructionDueToday(true);
        givenAWithdrawableBalanceOf(MORE_THAN_THE_INSTALLMENT);

        runTheJob();

        assertThat(transferredAmount()).isEqualByComparingTo(INSTALLMENT_DUE);
        assertThat(historyRow()).contains("'success'").describedAs("opting in does not turn a fully covered run into a partial one");
    }

    private void runTheJob() throws Exception {
        tasklet().execute(mock(StepContribution.class), mock(ChunkContext.class));
    }

    private ExecuteStandingInstructionsTasklet tasklet() {
        return new ExecuteStandingInstructionsTasklet(standingInstructionReadPlatformService, jdbcTemplate, sqlGenerator,
                accountTransfersWritePlatformService, savingsAccountAssembler);
    }

    private void givenAnInstructionDueToday(final boolean allowPartialTransfer) {
        when(standingInstructionReadPlatformService.retrieveAll(StandingInstructionStatus.ACTIVE.getValue()))
                .thenReturn(List.of(duesBasedLoanRepayment(allowPartialTransfer)));
        when(standingInstructionReadPlatformService.retriveLoanDuesData(TO_LOAN_ACCOUNT_ID))
                .thenReturn(new StandingInstructionDuesData(currentDate, INSTALLMENT_DUE));
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenAWithdrawableBalanceOf(final BigDecimal withdrawableBalance) {
        final SavingsAccount fromSavingsAccount = mock(SavingsAccount.class);
        when(fromSavingsAccount.getWithdrawableBalance()).thenReturn(withdrawableBalance);
        when(savingsAccountAssembler.assembleFrom(eq(FROM_SAVINGS_ACCOUNT_ID), eq(false))).thenReturn(fromSavingsAccount);
    }

    private BigDecimal transferredAmount() {
        ArgumentCaptor<AccountTransferDTO> transfer = ArgumentCaptor.forClass(AccountTransferDTO.class);
        verify(accountTransfersWritePlatformService).transferFunds(transfer.capture());
        return transfer.getValue().getTransactionAmount();
    }

    private String historyRow() {
        ArgumentCaptor<String> insert = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(insert.capture());
        return insert.getValue();
    }

    /** A dues-based standing instruction repaying a loan from a savings account, active and in date. */
    private StandingInstructionData duesBasedLoanRepayment(final boolean allowPartialTransfer) {
        final PortfolioAccountData fromAccount = new PortfolioAccountData(FROM_SAVINGS_ACCOUNT_ID, "000000011", null, null, null, null,
                null, null, null, null, null, null);
        final PortfolioAccountData toAccount = new PortfolioAccountData(TO_LOAN_ACCOUNT_ID, "000000022", null, null, null, null, null, null,
                null, null, null, null);
        final BigDecimal noFixedAmount = null;

        return StandingInstructionData.instance(INSTRUCTION_ID, 1L, "Loan repayment from savings", null, null, null, null,
                enumOf(PortfolioAccountType.SAVINGS.getValue()), fromAccount, enumOf(PortfolioAccountType.LOAN.getValue()), toAccount,
                enumOf(AccountTransferType.LOAN_REPAYMENT.getValue()), null, enumOf(StandingInstructionType.DUES.getValue()),
                enumOf(StandingInstructionStatus.ACTIVE.getValue()), noFixedAmount, currentDate.minusMonths(1), null,
                enumOf(AccountTransferRecurrenceType.AS_PER_DUES.getValue()), null, null, null, allowPartialTransfer);
    }

    private EnumOptionData enumOf(final Integer value) {
        return new EnumOptionData(value.longValue(), null, null);
    }
}
