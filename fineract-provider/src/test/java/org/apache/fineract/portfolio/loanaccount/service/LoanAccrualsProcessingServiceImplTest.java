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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ChargeAccrualDateConfig;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidByRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
        // reprocessing skips unsaved loans, so the mock must look persisted
        when(loan.getId()).thenReturn(1L);
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
        when(loan.getLoanTransactions()).thenReturn(new ArrayList<>());

        Office office = mock(Office.class);
        when(loan.getOffice()).thenReturn(office);
        when(externalIdFactory.create()).thenReturn(ExternalId.empty());
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("accrualAdjustmentDedupeCases")
    @DisplayName("Reprocess creates one accrual adjustment per same-date, same-amount post-due-date accrual not yet adjusted")
    void reprocessExistingAccruals_ShouldAdjustOnlyUnadjustedSameDateSameAmountAccruals(final String scenario, final boolean progressive,
            final int accrualCount, final int existingAdjustmentCount, final int expectedNewAdjustmentCount) {
        LocalDate lastDueDate = LocalDate.of(2025, 9, 10);
        LocalDate accrualDate = LocalDate.of(2025, 12, 12);

        LoanRepaymentScheduleInstallment lastInstallment = mock(LoanRepaymentScheduleInstallment.class);
        when(lastInstallment.getDueDate()).thenReturn(lastDueDate);
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(lastInstallment);
        when(loan.getId()).thenReturn(1L);
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loan.isProgressiveSchedule()).thenReturn(progressive);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of());

        List<LoanTransaction> accruals = IntStream.range(0, accrualCount).mapToObj(i -> postDueDateFeeAccrual(accrualDate, BigDecimal.TEN))
                .toList();
        List<LoanTransaction> loanTransactions = new ArrayList<>(accruals);
        IntStream.range(0, existingAdjustmentCount)
                .forEach(i -> loanTransactions.add(existingAccrualAdjustment(accrualDate, BigDecimal.TEN)));
        when(loan.getLoanTransactions()).thenReturn(loanTransactions);
        doAnswer(invocation -> loanTransactions.add(invocation.getArgument(0))).when(loan).addLoanTransaction(any(LoanTransaction.class));

        when(loanTransactionRepository.findNonReversedByLoanAndTypes(loan,
                Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT))).thenReturn(accruals);
        when(loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, Set.of(LoanTransactionType.ACCRUAL), lastDueDate))
                .thenReturn(accruals);
        Office office = mock(Office.class);
        when(loan.getOffice()).thenReturn(office);
        when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        ArgumentCaptor<LoanTransaction> savedAdjustments = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionRepository, times(expectedNewAdjustmentCount)).saveAndFlush(savedAdjustments.capture());
        assertThat(savedAdjustments.getAllValues()).allSatisfy(adjustment -> {
            assertThat(adjustment.getTypeOf()).isEqualTo(LoanTransactionType.ACCRUAL_ADJUSTMENT);
            assertThat(adjustment.getTransactionDate()).isEqualTo(accrualDate);
            assertThat(adjustment.getAmount()).isEqualByComparingTo("10");
            assertThat(adjustment.getFeeChargesPortion()).isEqualByComparingTo("10");
        });
        verify(journalEntryPoster, times(expectedNewAdjustmentCount)).postJournalEntriesForLoanTransaction(any(LoanTransaction.class),
                eq(false), eq(false));
        verify(businessEventNotifierService, times(expectedNewAdjustmentCount))
                .notifyPostBusinessEvent(any(LoanAccrualAdjustmentTransactionBusinessEvent.class));
        accruals.forEach(accrual -> verify(accrual, never()).reverse());
    }

    @Test
    @DisplayName("Reprocess adjusts the unadjusted penalty accrual when a same-day fee accrual of equal amount is already adjusted")
    void reprocessExistingAccruals_ShouldAdjustPenaltyAccrual_WhenSameDayEqualFeeAccrualIsAlreadyAdjusted() {
        LocalDate lastDueDate = LocalDate.of(2025, 9, 10);
        LocalDate accrualDate = LocalDate.of(2025, 12, 12);
        LoanRepaymentScheduleInstallment lastInstallment = mock(LoanRepaymentScheduleInstallment.class);
        when(lastInstallment.getDueDate()).thenReturn(lastDueDate);
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(lastInstallment);
        when(loan.getId()).thenReturn(1L);
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(List.of());

        List<LoanTransaction> accruals = List.of(postDueDateAccrual(accrualDate, BigDecimal.TEN, BigDecimal.ZERO),
                postDueDateAccrual(accrualDate, BigDecimal.ZERO, BigDecimal.TEN));
        List<LoanTransaction> loanTransactions = new ArrayList<>(accruals);
        loanTransactions.add(existingAccrualAdjustment(accrualDate, BigDecimal.TEN, BigDecimal.ZERO));
        when(loan.getLoanTransactions()).thenReturn(loanTransactions);
        doAnswer(invocation -> loanTransactions.add(invocation.getArgument(0))).when(loan).addLoanTransaction(any(LoanTransaction.class));
        when(loanTransactionRepository.findNonReversedByLoanAndTypes(loan,
                Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT))).thenReturn(accruals);
        when(loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, Set.of(LoanTransactionType.ACCRUAL), lastDueDate))
                .thenReturn(accruals);
        when(loan.getOffice()).thenReturn(mock(Office.class));
        when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        ArgumentCaptor<LoanTransaction> savedAdjustments = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionRepository, times(1)).saveAndFlush(savedAdjustments.capture());
        LoanTransaction adjustment = savedAdjustments.getValue();
        assertThat(adjustment.getTypeOf()).isEqualTo(LoanTransactionType.ACCRUAL_ADJUSTMENT);
        assertThat(adjustment.getTransactionDate()).isEqualTo(accrualDate);
        assertThat(adjustment.getAmount()).isEqualByComparingTo("10");
        assertThat(adjustment.getPenaltyChargesPortion()).isEqualByComparingTo("10");
        assertThat(adjustment.getFeeChargesPortion()).isEqualByComparingTo("0");
        assertThat(adjustment.getInterestPortion()).isEqualByComparingTo("0");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("jobLoanSelectionCases")
    @DisplayName("Accrual jobs widen the loan selection to future charges for progressive loans only when Real-Time is on")
    void addAccruals_ShouldWidenLoanSelectionForProgressiveLoansOnly_WhenRealTimeIsOn(final String scenario,
            final ChargeAccrualDateConfig chargeAccrualDate, final boolean realTime, final boolean expectedFutureCharges,
            final boolean expectedProgressiveFutureCharges) throws Exception {
        LocalDate tillDate = LocalDate.of(2025, 1, 31);
        when(configurationDomainService.getAccrualDateConfigForCharge()).thenReturn(chargeAccrualDate);
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(realTime);

        accrualsProcessingService.addPeriodicAccruals(tillDate);
        accrualsProcessingService.addAccruals(tillDate);

        verify(loanRepositoryWrapper).findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, expectedFutureCharges,
                expectedProgressiveFutureCharges);
        verify(loanRepositoryWrapper).findLoansForAddAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, expectedFutureCharges,
                expectedProgressiveFutureCharges);
    }

    @Test
    @DisplayName("COB accrual on a Real-Time progressive loan keeps installment fee and penalty accrued equal to the paid-by sums")
    void addPeriodicAccruals_ShouldKeepInstallmentChargeAccruedEqualToPaidBySums_WhenRealTimeOnProgressiveLoan() {
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        LocalDate tillDate = LocalDate.of(2024, 2, 15);
        LoanRepaymentScheduleInstallment first = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31), BigDecimal.valueOf(500), BigDecimal.valueOf(4), BigDecimal.ZERO, BigDecimal.valueOf(5), false,
                null, null);
        LoanRepaymentScheduleInstallment second = new LoanRepaymentScheduleInstallment(loan, 2, LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 2, 29), BigDecimal.valueOf(500), BigDecimal.valueOf(2), BigDecimal.TEN, BigDecimal.ZERO, false, null,
                null);
        first.setPenaltyAccrued(BigDecimal.valueOf(5));
        second.setFeeAccrued(BigDecimal.TEN);
        givenInterestBearingRealTimeProgressiveLoan(currency, List.of(first, second));
        LoanCharge fee = realTimeAccruedCharge(1L, false, LocalDate.of(2024, 2, 10), LocalDate.of(2024, 2, 20), BigDecimal.TEN, currency);
        LoanCharge penalty = realTimeAccruedCharge(2L, true, LocalDate.of(2024, 1, 20), LocalDate.of(2024, 1, 20), BigDecimal.valueOf(5),
                currency);
        List<LoanCharge> loanCharges = List.of(fee, penalty);
        when(loan.getLoanCharges(any())).thenAnswer(invocation -> {
            Predicate<LoanCharge> predicate = invocation.getArgument(0);
            return loanCharges.stream().filter(predicate).toList();
        });

        accrualsProcessingService.addPeriodicAccruals(tillDate, loan);

        assertThat(first.getInterestAccrued(currency).getAmount()).isEqualByComparingTo("4");
        assertThat(first.getFeeAccrued(currency).getAmount()).isEqualByComparingTo("0");
        assertThat(first.getPenaltyAccrued(currency).getAmount()).isEqualByComparingTo("5");
        assertThat(second.getInterestAccrued(currency).getAmount()).isEqualByComparingTo("1");
        assertThat(second.getFeeAccrued(currency).getAmount()).isEqualByComparingTo("10");
        assertThat(second.getPenaltyAccrued(currency).getAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Real-time accrual saves, journals and publishes the transaction created for the charge, in that order")
    void processRealTimeChargeAccruals_ShouldSaveJournalAndPublishCreatedAccrualInOrder() {
        LoanCharge loanCharge = givenRealTimeApplicableCharge();
        LoanTransaction created = mock(LoanTransaction.class);
        LoanTransaction saved = mock(LoanTransaction.class);
        when(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null)).thenReturn(created);
        when(loanTransactionRepository.saveAndFlush(created)).thenReturn(saved);

        accrualsProcessingService.processRealTimeChargeAccruals(loan, List.of(loanCharge));

        InOrder inOrder = inOrder(loanChargeService, loanTransactionRepository, journalEntryPoster, businessEventNotifierService);
        inOrder.verify(loanChargeService).createChargeAppliedTransaction(loan, loanCharge, null);
        inOrder.verify(loanTransactionRepository).saveAndFlush(created);
        inOrder.verify(journalEntryPoster).postJournalEntriesForLoanTransaction(saved, false, false);
        ArgumentCaptor<LoanAccrualTransactionCreatedBusinessEvent> event = ArgumentCaptor
                .forClass(LoanAccrualTransactionCreatedBusinessEvent.class);
        inOrder.verify(businessEventNotifierService).notifyPostBusinessEvent(event.capture());
        assertThat(event.getValue().get()).isSameAs(saved);
    }

    @Test
    @DisplayName("Real-time accrual evaluates loan applicability once and posts only the charges without an existing accrual")
    void processRealTimeChargeAccruals_ShouldEvaluateLoanOnceAndPostOnlyUnaccruedCharges() {
        MonetaryCurrency currency = givenRealTimeApplicableLoan();
        LoanCharge firstPenalty = accruableCharge(currency);
        LoanCharge accruedPenalty = accruableCharge(currency);
        LoanCharge secondPenalty = accruableCharge(currency);
        when(loanTransactionRepository.findChargeAccrualAmount(accruedPenalty)).thenReturn(BigDecimal.TEN);
        LoanTransaction firstAccrual = mock(LoanTransaction.class);
        LoanTransaction secondAccrual = mock(LoanTransaction.class);
        when(loanChargeService.createChargeAppliedTransaction(loan, firstPenalty, null)).thenReturn(firstAccrual);
        when(loanChargeService.createChargeAppliedTransaction(loan, secondPenalty, null)).thenReturn(secondAccrual);
        when(loanTransactionRepository.saveAndFlush(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accrualsProcessingService.processRealTimeChargeAccruals(loan, List.of(firstPenalty, accruedPenalty, secondPenalty));

        verify(configurationDomainService, times(1)).isPlChargeAccrualRealTime();
        verify(loanChargeService, never()).createChargeAppliedTransaction(loan, accruedPenalty, null);
        ArgumentCaptor<LoanTransaction> saved = ArgumentCaptor.forClass(LoanTransaction.class);
        verify(loanTransactionRepository, times(2)).saveAndFlush(saved.capture());
        assertThat(saved.getAllValues()).containsExactly(firstAccrual, secondAccrual);
        verify(journalEntryPoster).postJournalEntriesForLoanTransaction(firstAccrual, false, false);
        verify(journalEntryPoster).postJournalEntriesForLoanTransaction(secondAccrual, false, false);
    }

    @Test
    @DisplayName("Real-time accrual does nothing and reads no configuration when no charge was applied")
    void processRealTimeChargeAccruals_ShouldDoNothing_WhenNoChargeWasApplied() {
        accrualsProcessingService.processRealTimeChargeAccruals(loan, List.of());

        verifyNoInteractions(configurationDomainService, loanChargeService, loanTransactionRepository, journalEntryPoster,
                businessEventNotifierService);
    }

    @Test
    @DisplayName("Periodic accrual job reads the charge accrual configurations once per run, not once per loan")
    void addPeriodicAccruals_ShouldReadChargeAccrualConfigurationsOncePerRun() throws Exception {
        LocalDate tillDate = LocalDate.of(2025, 1, 31);
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(true);
        Loan firstLoan = mock(Loan.class);
        Loan secondLoan = mock(Loan.class);
        when(firstLoan.isClosed()).thenReturn(true);
        when(secondLoan.isClosed()).thenReturn(true);
        when(loanRepositoryWrapper.findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, false, true))
                .thenReturn(List.of(firstLoan, secondLoan));

        accrualsProcessingService.addPeriodicAccruals(tillDate);

        verify(loanRepositoryWrapper).findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, false, true);
        verify(configurationDomainService, times(1)).getAccrualDateConfigForCharge();
        verify(configurationDomainService, times(1)).isPlChargeAccrualRealTime();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "cumulative", "eod", "alreadyAccrued", "npa", "nonPeriodic", "chargedOff", "inactiveLoan", "inactiveCharge",
            "disbursementCharge", "installmentFee" })
    @DisplayName("Real-time accrual posts nothing when the loan or the charge is not eligible")
    void processRealTimeChargeAccruals_ShouldBeNoOp_WhenNotApplicable(final String scenario) {
        LoanCharge loanCharge = givenRealTimeApplicableCharge();
        switch (scenario) {
            case "cumulative" -> when(loan.isProgressiveSchedule()).thenReturn(false);
            case "eod" -> when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(false);
            case "alreadyAccrued" -> when(loanTransactionRepository.findChargeAccrualAmount(loanCharge)).thenReturn(BigDecimal.TEN);
            case "npa" -> when(loan.isNpa()).thenReturn(true);
            case "nonPeriodic" -> when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(false);
            case "chargedOff" -> when(loan.isChargedOff()).thenReturn(true);
            case "inactiveLoan" -> when(loanStatus.isActive()).thenReturn(false);
            case "inactiveCharge" -> when(loanCharge.isActive()).thenReturn(false);
            case "disbursementCharge" -> when(loanCharge.isDueAtDisbursement()).thenReturn(true);
            case "installmentFee" -> when(loanCharge.isInstalmentFee()).thenReturn(true);
            default -> throw new IllegalArgumentException(scenario);
        }

        accrualsProcessingService.processRealTimeChargeAccruals(loan, List.of(loanCharge));

        verify(loanChargeService, never()).createChargeAppliedTransaction(any(), any(), any());
        verify(loanTransactionRepository, never()).saveAndFlush(any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    @DisplayName("Reprocess keeps a real-time fee accrual on the installment its paid-by mapping points to")
    void reprocessExistingAccruals_ShouldDistributeRealTimeFeeAccrualToItsPaidByInstallment_OnProgressiveLoan() {
        LoanRepaymentScheduleInstallment first = installmentWithFeeCharged(1, LocalDate.of(2024, 1, 31));
        LoanRepaymentScheduleInstallment second = installmentWithFeeCharged(2, LocalDate.of(2024, 2, 29));
        LoanRepaymentScheduleInstallment third = installmentWithFeeCharged(3, LocalDate.of(2024, 3, 31));
        givenRealTimeProgressiveLoanWithAccruals(List.of(first, second, third), chargeAccrualTransaction(LoanTransactionType.ACCRUAL,
                BigDecimal.TEN, BigDecimal.ZERO, paidBy(charge(false), 3, BigDecimal.TEN)));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        assertThat(capturedFeeAccrued(third)).isEqualByComparingTo("10");
        assertThat(capturedFeeAccrued(first)).isEqualByComparingTo("0");
        assertThat(capturedFeeAccrued(second)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Reprocess places a real-time penalty accrual without installment number on the installment that carries the penalty")
    void reprocessExistingAccruals_ShouldKeepRealTimePenaltyAccrualWithoutInstallmentNumber_OnProgressiveLoan() {
        LoanRepaymentScheduleInstallment first = installment(1, LocalDate.of(2024, 1, 31), BigDecimal.ZERO, BigDecimal.ZERO);
        LoanRepaymentScheduleInstallment second = installment(2, LocalDate.of(2024, 3, 1), BigDecimal.ZERO, BigDecimal.valueOf(5));
        LoanRepaymentScheduleInstallment third = installment(3, LocalDate.of(2024, 3, 31), BigDecimal.ZERO, BigDecimal.ZERO);
        givenRealTimeProgressiveLoanWithAccruals(List.of(first, second, third), chargeAccrualTransaction(LoanTransactionType.ACCRUAL,
                BigDecimal.ZERO, BigDecimal.valueOf(5), paidBy(charge(true), null, BigDecimal.valueOf(5))));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        assertThat(capturedPenaltyAccrued(second)).isEqualByComparingTo("5");
        assertThat(capturedPenaltyAccrued(first)).isEqualByComparingTo("0");
        assertThat(capturedPenaltyAccrued(third)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Reprocess reduces a real-time fee accrual by an accrual adjustment that has no paid-by mapping")
    void reprocessExistingAccruals_ShouldReduceRealTimeFeeAccrualByAdjustmentWithoutPaidBy_OnProgressiveLoan() {
        LoanRepaymentScheduleInstallment only = installmentWithFeeCharged(1, LocalDate.of(2024, 1, 31));
        givenRealTimeProgressiveLoanWithAccruals(List.of(only),
                chargeAccrualTransaction(LoanTransactionType.ACCRUAL, BigDecimal.TEN, BigDecimal.ZERO,
                        paidBy(charge(false), 1, BigDecimal.TEN)),
                chargeAccrualTransaction(LoanTransactionType.ACCRUAL_ADJUSTMENT, BigDecimal.TEN, BigDecimal.ZERO));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        assertThat(capturedFeeAccrued(only)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Reprocess caps a real-time fee accrual at the installment fee charged and moves the excess to the next installment")
    void reprocessExistingAccruals_ShouldCapRealTimeFeeAccrualAtInstallmentFeeCharged_OnProgressiveLoan() {
        LoanRepaymentScheduleInstallment first = installmentWithFeeCharged(1, LocalDate.of(2024, 1, 31));
        LoanRepaymentScheduleInstallment second = installmentWithFeeCharged(2, LocalDate.of(2024, 2, 29));
        givenRealTimeProgressiveLoanWithAccruals(List.of(first, second), chargeAccrualTransaction(LoanTransactionType.ACCRUAL,
                BigDecimal.valueOf(15), BigDecimal.ZERO, paidBy(charge(false), 1, BigDecimal.valueOf(15))));

        accrualsProcessingService.reprocessExistingAccruals(loan, true);

        assertThat(capturedFeeAccrued(first)).isEqualByComparingTo("10");
        assertThat(capturedFeeAccrued(second)).isEqualByComparingTo("5");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("chargeSubmittedAroundDisbursementCases")
    @DisplayName("COB matches a charge submitted before disbursement on its due date under Real-Time, otherwise on its submitted date")
    void addPeriodicAccruals_ShouldMatchChargeSubmittedBeforeDisbursementOnDueDateOnly_UnderRealTime(final String scenario,
            final LocalDate submittedOnDate, final boolean realTimeChargeAccrual, final ChargeAccrualDateConfig chargeAccrualDate,
            final LocalDate tillDate, final String expectedFeeAccrued) {
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        LoanRepaymentScheduleInstallment first = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31), BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null);
        LoanRepaymentScheduleInstallment second = new LoanRepaymentScheduleInstallment(loan, 2, LocalDate.of(2024, 1, 31),
                LocalDate.of(2024, 2, 29), BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO, false, null, null);
        givenInterestBearingRealTimeProgressiveLoan(currency, List.of(first, second));
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(realTimeChargeAccrual);
        when(configurationDomainService.getAccrualDateConfigForCharge()).thenReturn(chargeAccrualDate);
        LoanCharge fee = realTimeAccruedCharge(1L, false, submittedOnDate, LocalDate.of(2024, 2, 10), BigDecimal.TEN, currency);
        when(loanTransactionRepository.findChargeAccrualAmount(fee)).thenReturn(BigDecimal.ZERO);
        when(fee.isDueInPeriod(any(), any(), anyBoolean())).thenCallRealMethod();
        when(loanChargeService.fetchLoanChargesById(loan, 1L)).thenReturn(fee);
        when(loan.getLoanCharges(any())).thenAnswer(invocation -> {
            Predicate<LoanCharge> predicate = invocation.getArgument(0);
            return Stream.of(fee).filter(predicate).toList();
        });

        accrualsProcessingService.addPeriodicAccruals(tillDate, loan);

        assertThat(second.getFeeAccrued(currency).getAmount()).isEqualByComparingTo(expectedFeeAccrued);
    }

    private void givenRealTimeProgressiveLoanWithAccruals(final List<LoanRepaymentScheduleInstallment> installments,
            final LoanTransaction... accruals) {
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(true);
        when(loan.getId()).thenReturn(1L);
        when(loan.isChargedOff()).thenReturn(false);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.getRepaymentScheduleInstallments()).thenReturn(installments);
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(installments.getLast());
        when(loanTransactionRepository.findNonReversedByLoanAndTypes(loan,
                Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT))).thenReturn(List.of(accruals));
    }

    private void givenInterestBearingRealTimeProgressiveLoan(final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments) {
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(true);
        when(configurationDomainService.getAccrualDateConfigForCharge()).thenReturn(ChargeAccrualDateConfig.DUE_DATE);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.isOpen()).thenReturn(true);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loan.isInterestBearing()).thenReturn(true);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.getDisbursementDate()).thenReturn(installments.getFirst().getFromDate());
        when(loan.getOffice()).thenReturn(mock(Office.class));
        when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        LoanProductRelatedDetail productDetail = mock(LoanProductRelatedDetail.class);
        when(productDetail.getCurrency()).thenReturn(currency);
        when(loan.getLoanProductRelatedDetail()).thenReturn(productDetail);
        LoanScheduleGenerator scheduleGenerator = mock(LoanScheduleGenerator.class);
        when(loanScheduleFactory.create(any(), any())).thenReturn(scheduleGenerator);
        when(scheduleGenerator.getPeriodInterestTillDate(any(), any())).thenReturn(Money.of(currency, BigDecimal.ONE));
        when(loan.getRepaymentScheduleInstallments()).thenReturn(installments);
        when(loan.getLastLoanRepaymentScheduleInstallment()).thenReturn(installments.getLast());
        when(loan.getRepaymentScheduleInstallments(any())).thenAnswer(invocation -> {
            Predicate<LoanRepaymentScheduleInstallment> predicate = invocation.getArgument(0);
            return installments.stream().filter(predicate).toList();
        });
        when(loan.getRepaymentScheduleInstallment(any())).thenAnswer(invocation -> {
            Predicate<LoanRepaymentScheduleInstallment> predicate = invocation.getArgument(0);
            return installments.stream().filter(predicate).findFirst().orElse(null);
        });
        when(loan.fetchRepaymentScheduleInstallment(any())).thenAnswer(invocation -> installments.stream()
                .filter(installment -> installment.getInstallmentNumber().equals(invocation.getArgument(0))).findFirst().orElse(null));
        when(loanTransactionRepository.findTotalInterestAccruedAmount(loan)).thenReturn(BigDecimal.ZERO);
        when(loanTransactionRepository.findTotalUnrecognizedIncomeFromInterestWaiverByLoanAndDate(any(), any())).thenReturn(BigDecimal.ZERO);
        when(loanTransactionRepository.findChargeUnrecognizedWaivedAmount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(loanTransactionRepository.save(any(LoanTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanTransactionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private LoanCharge realTimeAccruedCharge(final Long id, final boolean penalty, final LocalDate submittedOnDate, final LocalDate dueDate,
            final BigDecimal amount, final MonetaryCurrency currency) {
        LoanCharge loanCharge = mock(LoanCharge.class);
        when(loanCharge.getId()).thenReturn(id);
        when(loanCharge.getLoan()).thenReturn(loan);
        when(loanCharge.isActive()).thenReturn(true);
        when(loanCharge.isPenaltyCharge()).thenReturn(penalty);
        when(loanCharge.getSubmittedOnDate()).thenReturn(submittedOnDate);
        when(loanCharge.getDueLocalDate()).thenReturn(dueDate);
        when(loanCharge.getDueDate()).thenReturn(dueDate);
        when(loanCharge.getAmount(currency)).thenReturn(Money.of(currency, amount));
        when(loanCharge.getAmountWaived(currency)).thenReturn(Money.zero(currency));
        when(loanTransactionRepository.findChargeAccrualAmount(loanCharge)).thenReturn(amount);
        return loanCharge;
    }

    private static BigDecimal capturedFeeAccrued(final LoanRepaymentScheduleInstallment installment) {
        ArgumentCaptor<BigDecimal> feeAccrued = ArgumentCaptor.forClass(BigDecimal.class);
        verify(installment).setFeeAccrued(feeAccrued.capture());
        return feeAccrued.getValue();
    }

    private static BigDecimal capturedPenaltyAccrued(final LoanRepaymentScheduleInstallment installment) {
        ArgumentCaptor<BigDecimal> penaltyAccrued = ArgumentCaptor.forClass(BigDecimal.class);
        verify(installment).setPenaltyAccrued(penaltyAccrued.capture());
        return penaltyAccrued.getValue();
    }

    private static LoanCharge charge(final boolean penalty) {
        LoanCharge loanCharge = mock(LoanCharge.class);
        when(loanCharge.isPenaltyCharge()).thenReturn(penalty);
        return loanCharge;
    }

    private static LoanChargePaidBy paidBy(final LoanCharge loanCharge, final Integer installmentNumber, final BigDecimal amount) {
        LoanChargePaidBy paidBy = mock(LoanChargePaidBy.class);
        when(paidBy.getInstallmentNumber()).thenReturn(installmentNumber);
        when(paidBy.getAmount()).thenReturn(amount);
        when(paidBy.getLoanCharge()).thenReturn(loanCharge);
        return paidBy;
    }

    private static LoanTransaction chargeAccrualTransaction(final LoanTransactionType type, final BigDecimal fee, final BigDecimal penalty,
            final LoanChargePaidBy... paidBys) {
        LoanTransaction transaction = mock(LoanTransaction.class);
        when(transaction.getTypeOf()).thenReturn(type);
        when(transaction.getTransactionDate()).thenReturn(LocalDate.of(2024, 1, 10));
        when(transaction.getInterestPortion()).thenReturn(BigDecimal.ZERO);
        when(transaction.getFeeChargesPortion()).thenReturn(fee);
        when(transaction.getPenaltyChargesPortion()).thenReturn(penalty);
        when(transaction.getLoanChargesPaid()).thenReturn(Set.of(paidBys));
        return transaction;
    }

    private LoanCharge givenRealTimeApplicableCharge() {
        return accruableCharge(givenRealTimeApplicableLoan());
    }

    private MonetaryCurrency givenRealTimeApplicableLoan() {
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, null);
        when(loan.getCurrency()).thenReturn(currency);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loanStatus.isActive()).thenReturn(true);
        when(loan.isNpa()).thenReturn(false);
        when(loan.isChargedOff()).thenReturn(false);
        when(configurationDomainService.isPlChargeAccrualRealTime()).thenReturn(true);
        return currency;
    }

    private LoanCharge accruableCharge(final MonetaryCurrency currency) {
        LoanCharge loanCharge = mock(LoanCharge.class);
        when(loanCharge.isActive()).thenReturn(true);
        when(loanCharge.isDueAtDisbursement()).thenReturn(false);
        when(loanCharge.isInstalmentFee()).thenReturn(false);
        when(loanCharge.getAmount(currency)).thenReturn(Money.of(currency, BigDecimal.TEN));
        when(loanTransactionRepository.findChargeAccrualAmount(loanCharge)).thenReturn(BigDecimal.ZERO);
        return loanCharge;
    }

    private static LoanRepaymentScheduleInstallment installmentWithFeeCharged(final int installmentNumber, final LocalDate dueDate) {
        return installment(installmentNumber, dueDate, BigDecimal.TEN, BigDecimal.ZERO);
    }

    private static LoanRepaymentScheduleInstallment installment(final int installmentNumber, final LocalDate dueDate,
            final BigDecimal feeCharged, final BigDecimal penaltyCharged) {
        LoanRepaymentScheduleInstallment installment = mock(LoanRepaymentScheduleInstallment.class);
        when(installment.getInstallmentNumber()).thenReturn(installmentNumber);
        when(installment.getDueDate()).thenReturn(dueDate);
        when(installment.getFeeChargesCharged()).thenReturn(feeCharged);
        when(installment.getPenaltyCharges()).thenReturn(penaltyCharged);
        return installment;
    }

    private static LoanTransaction postDueDateFeeAccrual(LocalDate transactionDate, BigDecimal fee) {
        return postDueDateAccrual(transactionDate, fee, BigDecimal.ZERO);
    }

    private static LoanTransaction postDueDateAccrual(final LocalDate transactionDate, final BigDecimal fee, final BigDecimal penalty) {
        LoanTransaction accrual = mock(LoanTransaction.class);
        when(accrual.isReversed()).thenReturn(false);
        when(accrual.isNotReversed()).thenReturn(true);
        when(accrual.isAccrual()).thenReturn(true);
        when(accrual.getTransactionDate()).thenReturn(transactionDate);
        when(accrual.getTypeOf()).thenReturn(LoanTransactionType.ACCRUAL);
        when(accrual.getAmount()).thenReturn(fee.add(penalty));
        when(accrual.getInterestPortion()).thenReturn(BigDecimal.ZERO);
        when(accrual.getFeeChargesPortion()).thenReturn(fee);
        when(accrual.getPenaltyChargesPortion()).thenReturn(penalty);
        return accrual;
    }

    private static LoanTransaction existingAccrualAdjustment(final LocalDate transactionDate, final BigDecimal amount) {
        return existingAccrualAdjustment(transactionDate, amount, BigDecimal.ZERO);
    }

    private static LoanTransaction existingAccrualAdjustment(final LocalDate transactionDate, final BigDecimal fee,
            final BigDecimal penalty) {
        LoanTransaction adjustment = mock(LoanTransaction.class);
        when(adjustment.isNotReversed()).thenReturn(true);
        when(adjustment.getTransactionDate()).thenReturn(transactionDate);
        when(adjustment.getTypeOf()).thenReturn(LoanTransactionType.ACCRUAL_ADJUSTMENT);
        when(adjustment.getAmount()).thenReturn(fee.add(penalty));
        when(adjustment.getInterestPortion()).thenReturn(BigDecimal.ZERO);
        when(adjustment.getFeeChargesPortion()).thenReturn(fee);
        when(adjustment.getPenaltyChargesPortion()).thenReturn(penalty);
        return adjustment;
    }

    private static Stream<Arguments> accrualAdjustmentDedupeCases() {
        return Stream.of(Arguments.of("progressive, 2 accruals, none adjusted -> 2 new", true, 2, 0, 2),
                Arguments.of("progressive, 2 accruals, 1 adjusted -> 1 new", true, 2, 1, 1),
                Arguments.of("progressive, 2 accruals, both adjusted -> none", true, 2, 2, 0),
                Arguments.of("progressive, 1 accrual, already adjusted -> none", true, 1, 1, 0),
                Arguments.of("cumulative, 1 accrual, none adjusted -> 1 new", false, 1, 0, 1),
                Arguments.of("cumulative, 1 accrual, already adjusted -> none", false, 1, 1, 0),
                Arguments.of("cumulative, 2 accruals, 1 adjusted -> 1 new", false, 2, 1, 1));
    }

    private static Stream<Arguments> jobLoanSelectionCases() {
        return Stream.of(Arguments.of("due-date, EOD -> no widening", ChargeAccrualDateConfig.DUE_DATE, false, false, false),
                Arguments.of("due-date, Real-Time -> progressive loans only", ChargeAccrualDateConfig.DUE_DATE, true, false, true),
                Arguments.of("submitted-date, EOD -> all loans", ChargeAccrualDateConfig.SUBMITTED_DATE, false, true, false),
                Arguments.of("submitted-date, Real-Time -> all loans", ChargeAccrualDateConfig.SUBMITTED_DATE, true, true, true));
    }

    private static Stream<Arguments> chargeSubmittedAroundDisbursementCases() {
        LocalDate beforeDisbursement = LocalDate.of(2023, 12, 20);
        LocalDate disbursement = LocalDate.of(2024, 1, 1);
        LocalDate beforeDueDate = LocalDate.of(2024, 1, 20);
        LocalDate afterDueDate = LocalDate.of(2024, 2, 15);
        return Stream.of(
                Arguments.of("real-time, submitted before disbursement, before due date -> not accrued", beforeDisbursement, true,
                        ChargeAccrualDateConfig.DUE_DATE, beforeDueDate, "0"),
                Arguments.of("real-time, submitted before disbursement, after due date -> accrued", beforeDisbursement, true,
                        ChargeAccrualDateConfig.DUE_DATE, afterDueDate, "10"),
                Arguments.of("real-time, submitted on disbursement date, before due date -> accrued", disbursement, true,
                        ChargeAccrualDateConfig.DUE_DATE, beforeDueDate, "10"),
                Arguments.of("eod submitted-date, submitted before disbursement, after due date -> not accrued", beforeDisbursement, false,
                        ChargeAccrualDateConfig.SUBMITTED_DATE, afterDueDate, "0"));
    }

    private static Stream<Arguments> loanStatusTestCases() {
        return Stream.of(Arguments.of(true, false), // Loan is closed
                Arguments.of(false, true) // Loan is overpaid
        );
    }
}
