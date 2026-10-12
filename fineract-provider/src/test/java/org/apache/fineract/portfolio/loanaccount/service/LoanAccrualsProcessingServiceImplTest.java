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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidByRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanAccrualsProcessingServiceImplTest {

    @InjectMocks
    private LoanAccrualsProcessingServiceImpl accrualsProcessingService;

    @Mock
    private Loan loan;

    @Mock
    private LoanStatus loanStatus;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private LoanChargePaidByRepository loanChargePaidByRepository;

    @Mock
    private LoanJournalEntryPoster journalEntryPoster;

    @Mock
    private LoanBalanceService loanBalanceService;

    @Mock
    private LoanChargeService loanChargeService;

    @Mock
    private ExternalIdFactory externalIdFactory;

    @Mock
    private LoanScheduleGeneratorFactory loanScheduleFactory;

    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;

    @BeforeEach
    void setUp() {
        when(loan.isClosed()).thenReturn(false);
        when(loan.getStatus()).thenReturn(loanStatus);
        when(loanStatus.isOverpaid()).thenReturn(false);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "America/Mexico_City", null));
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(
                org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE, today,
                org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.COB_DATE, today)));
        MoneyHelper.initializeTenantRoundingMode("test", 6);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @ParameterizedTest
    @MethodSource("loanStatusTestCases")
    void addPeriodicAccruals_ShouldNotProceed_WhenLoanIsClosedOrOverpaid(final boolean isClosed, final boolean isOverpaid) {
        // Given
        final LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        when(loan.isClosed()).thenReturn(isClosed);

        when(loan.getStatus()).thenReturn(loanStatus);
        when(loanStatus.isOverpaid()).thenReturn(isOverpaid);

        // When
        accrualsProcessingService.addPeriodicAccruals(tillDate, loan);

        // Then
        verify(loan, times(1)).isClosed();

        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(journalEntryWritePlatformService);
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
        verify(loan, never()).addLoanTransaction(any());
    }

    @Test
    void calcInterestTransactionWaivedAmount_ShouldSkipMappingsWithoutTransaction() {
        // Given
        LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        LoanTransactionToRepaymentScheduleMapping nullTransactionMapping = mock(LoanTransactionToRepaymentScheduleMapping.class);
        LoanTransactionToRepaymentScheduleMapping interestWaiverMapping = mock(LoanTransactionToRepaymentScheduleMapping.class);
        LoanTransaction interestWaiverTransaction = mock(LoanTransaction.class);

        when(nullTransactionMapping.getLoanTransaction()).thenReturn(null);
        when(interestWaiverMapping.getLoanTransaction()).thenReturn(interestWaiverTransaction);
        when(interestWaiverMapping.getInterestPortion()).thenReturn(new BigDecimal("12.34"));
        when(interestWaiverTransaction.isReversed()).thenReturn(false);
        when(interestWaiverTransaction.isInterestWaiver()).thenReturn(true);
        when(interestWaiverTransaction.getTransactionDate()).thenReturn(tillDate);
        when(installment.getLoanTransactionToRepaymentScheduleMappings()).thenReturn(Set.of(nullTransactionMapping, interestWaiverMapping));

        // When
        BigDecimal result = ReflectionTestUtils.invokeMethod(accrualsProcessingService, "calcInterestTransactionWaivedAmount", installment,
                tillDate);

        // Then
        assertThat(result).isEqualByComparingTo("12.34");
    }

    @Test
    void calcChargeWaivedAmount_ShouldSkipMappingsWithoutTransaction() {
        // Given
        LocalDate tillDate = LocalDate.now(ZoneId.systemDefault());
        LoanChargePaidBy nullTransactionPaidBy = mock(LoanChargePaidBy.class);
        LoanChargePaidBy chargeWaiverPaidBy = mock(LoanChargePaidBy.class);
        LoanTransaction chargeWaiverTransaction = mock(LoanTransaction.class);

        when(nullTransactionPaidBy.getLoanTransaction()).thenReturn(null);
        when(chargeWaiverPaidBy.getLoanTransaction()).thenReturn(chargeWaiverTransaction);
        when(chargeWaiverPaidBy.getAmount()).thenReturn(new BigDecimal("12.34"));
        when(chargeWaiverTransaction.isReversed()).thenReturn(false);
        when(chargeWaiverTransaction.isWaiveCharge()).thenReturn(true);
        when(chargeWaiverTransaction.getTransactionDate()).thenReturn(tillDate);

        // When
        BigDecimal result = ReflectionTestUtils.invokeMethod(accrualsProcessingService, "calcChargeWaivedAmount",
                List.of(nullTransactionPaidBy, chargeWaiverPaidBy), tillDate);

        // Then
        assertThat(result).isEqualByComparingTo("12.34");
    }

    /**
     * Verifies that reprocessExistingAccruals does NOT reverse a post-due-date ACCRUAL transaction. Instead, it creates
     * an ACCRUAL_ADJUSTMENT transaction to cancel its effect.
     */
    @Test
    void reprocessExistingAccruals_ShouldCreateAdjustmentNotReverse_ForPostDueDateAccrual_OnCumulativeLoan() {
        // Given: a cumulative loan with periodic accrual accounting
        LocalDate lastDueDate = LocalDate.of(2025, 9, 10);
        LocalDate postDueDateAccrualDate = LocalDate.of(2025, 12, 12); // AFTER lastDueDate

        LoanRepaymentScheduleInstallment lastInstallment = mock(LoanRepaymentScheduleInstallment.class);
        when(lastInstallment.getDueDate()).thenReturn(lastDueDate);
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(lastInstallment);
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loan.isProgressiveSchedule()).thenReturn(false); // cumulative

        // Create a post-due-date ACCRUAL transaction (created by processAccrualsOnLoanClosure
        // when closedOnDate > lastDueDate)
        LoanTransaction postDueDateAccrual = mock(LoanTransaction.class);
        when(postDueDateAccrual.isReversed()).thenReturn(false);
        when(postDueDateAccrual.isNotReversed()).thenReturn(true);
        when(postDueDateAccrual.isAccrual()).thenReturn(true);
        when(postDueDateAccrual.getTransactionDate()).thenReturn(postDueDateAccrualDate);
        when(postDueDateAccrual.getTypeOf()).thenReturn(LoanTransactionType.ACCRUAL);
        when(postDueDateAccrual.getInterestPortion()).thenReturn(BigDecimal.valueOf(0.67));
        when(postDueDateAccrual.getFeeChargesPortion()).thenReturn(BigDecimal.ZERO);
        when(postDueDateAccrual.getPenaltyChargesPortion()).thenReturn(BigDecimal.ZERO);

        Set<LoanTransactionType> accrualTypes = Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT);
        Set<LoanTransactionType> accrualOnly = Set.of(LoanTransactionType.ACCRUAL);

        // retrieveListOfAccrualTransactions uses both types
        when(loanTransactionRepository.findNonReversedByLoanAndTypes(loan, accrualTypes)).thenReturn(List.of(postDueDateAccrual));
        // adjustAccrualsAfter only queries for ACCRUAL (not ACCRUAL_ADJUSTMENT — those must persist)
        when(loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, accrualOnly, lastDueDate))
                .thenReturn(List.of(postDueDateAccrual));
        // Idempotency check queries the loan's in-memory transaction list
        when(loan.getLoanTransactions()).thenReturn(new java.util.ArrayList<>());

        org.apache.fineract.organisation.office.domain.Office office = mock(org.apache.fineract.organisation.office.domain.Office.class);
        when(loan.getOffice()).thenReturn(office);
        when(externalIdFactory.create()).thenReturn(org.apache.fineract.infrastructure.core.domain.ExternalId.empty());
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        // Then: the post-due-date accrual is NOT reversed
        verify(postDueDateAccrual, never()).reverse();
        // An ACCRUAL_ADJUSTMENT is persisted, journal entries posted, and event published
        verify(loanTransactionRepository, times(1)).saveAndFlush(any(LoanTransaction.class));
        verify(journalEntryPoster, times(1)).postJournalEntriesForLoanTransaction(any(LoanTransaction.class), eq(false), eq(false));
        verify(businessEventNotifierService, times(1)).notifyPostBusinessEvent(any(LoanAccrualAdjustmentTransactionBusinessEvent.class));
    }

    private static Stream<Arguments> loanStatusTestCases() {
        return Stream.of(Arguments.of(true, false), // Loan is closed
                Arguments.of(false, true) // Loan is overpaid
        );
    }
}
