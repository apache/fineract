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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanBreachScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalBreachAmountCalculationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanBreachScheduleServiceImplTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository repository;

    @Mock
    private WorkingCapitalLoanBreachScheduleMapper mapper;

    @Mock
    private WorkingCapitalLoanRepository loanRepository;

    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;

    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;

    @Mock
    private WorkingCapitalLoanBalanceRepository balanceRepository;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    private WorkingCapitalLoanBreachScheduleServiceImpl underTest;

    private WorkingCapitalLoan loan;

    private WorkingCapitalLoanBalance balance;

    private FineractPlatformTenant originalTenant;

    @BeforeEach
    void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, LocalDate.of(2026, 6, 1))));
        underTest = new WorkingCapitalLoanBreachScheduleServiceImpl(repository, mapper, loanRepository, breachActionRepository,
                transactionRepository, balanceRepository, businessEventNotifierService,
                new WorkingCapitalLoanActiveBreachResetResolver(breachActionRepository));
        loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
        balance = WorkingCapitalLoanBalance.createFor(loan);
        lenient().when(breachActionRepository.isBreachDisabledAsOf(anyLong(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
        MoneyHelper.clearCacheForTenant("default");
    }

    private WorkingCapitalLoanBreachSchedule period(final int periodNumber, final LocalDate fromDate, final LocalDate toDate,
            final BigDecimal minPaymentAmount, final BigDecimal paidAmount, final BigDecimal outstandingAmount) {
        final WorkingCapitalLoanBreachSchedule period = new WorkingCapitalLoanBreachSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setMinPaymentAmount(minPaymentAmount);
        period.setPaidAmount(paidAmount);
        period.setOutstandingAmount(outstandingAmount);
        return period;
    }

    @Test
    void recalculatePastDueAmount_sumsOutstandingAmountAcrossEndedPeriods() {
        final List<WorkingCapitalLoanBreachSchedule> periods = List.of(
                period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), BigDecimal.valueOf(100), BigDecimal.valueOf(100),
                        BigDecimal.ZERO),
                period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), BigDecimal.valueOf(100), BigDecimal.valueOf(40),
                        BigDecimal.valueOf(60)),
                period(3, LocalDate.of(2026, 5, 21), LocalDate.of(2026, 5, 31), BigDecimal.valueOf(100), BigDecimal.ZERO,
                        BigDecimal.valueOf(100)));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(periods);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.recalculatePastDueAmount(loan);

        assertEquals(0, BigDecimal.valueOf(160).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void recalculatePastDueAmount_settledPeriodsContributeZeroBalance() {
        final List<WorkingCapitalLoanBreachSchedule> periods = List.of(
                period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), BigDecimal.valueOf(100), BigDecimal.valueOf(100),
                        BigDecimal.ZERO),
                period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), BigDecimal.valueOf(100), BigDecimal.valueOf(100),
                        BigDecimal.ZERO));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(periods);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.recalculatePastDueAmount(loan);

        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void recalculatePastDueAmount_excludesCurrentlyOpenPeriod() {
        final List<WorkingCapitalLoanBreachSchedule> periods = List.of(
                period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), BigDecimal.valueOf(100), BigDecimal.ZERO,
                        BigDecimal.valueOf(100)),
                period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 6, 20), BigDecimal.valueOf(50), BigDecimal.ZERO,
                        BigDecimal.valueOf(50)));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(periods);
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.recalculatePastDueAmount(loan);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void recalculatePastDueAmount_startsFromLatestResetPeriod() {
        final WorkingCapitalLoanBreachSchedule priorPeriod = period(1, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 30),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        final WorkingCapitalLoanBreachSchedule resetPeriod = period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        resetPeriod.setReset(true);
        final WorkingCapitalLoanBreachSchedule normalPeriod = period(3, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(priorPeriod, resetPeriod, normalPeriod));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.recalculatePastDueAmount(loan);

        assertEquals(0, BigDecimal.valueOf(200).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void recalculatePastDueAmount_noBalance_doesNothing() {
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.empty());

        underTest.recalculatePastDueAmount(loan);

        verify(repository, never()).findByLoanIdOrderByPeriodNumberAsc(anyLong());
        verify(balanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void recalculatePastDueAmount_skipsWhenBreachEvaluationDisabled() {
        when(breachActionRepository.isBreachDisabledAsOf(LOAN_ID, LocalDate.of(2026, 6, 1))).thenReturn(true);

        underTest.recalculatePastDueAmount(loan);

        verify(balanceRepository, never()).findByWcLoan_Id(anyLong());
        verify(repository, never()).findByLoanIdOrderByPeriodNumberAsc(anyLong());
        verify(balanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void applyRepayment_recalculatesPastDueAmountAfterUpdatingPeriod() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        final WorkingCapitalLoanBreachSchedule currentPeriod = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        when(repository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, transactionDate, transactionDate))
                .thenReturn(Optional.of(currentPeriod));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(currentPeriod));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.applyRepayment(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        assertEquals(0, BigDecimal.valueOf(40).compareTo(currentPeriod.getOutstandingAmount()));
        assertEquals(0, BigDecimal.valueOf(40).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void applyRepayment_updatesResetPeriod() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        final WorkingCapitalLoanBreachSchedule resetPeriod = period(1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        resetPeriod.setReset(true);
        when(repository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, transactionDate, transactionDate))
                .thenReturn(Optional.of(resetPeriod));

        underTest.applyRepayment(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        assertEquals(0, BigDecimal.valueOf(60).compareTo(resetPeriod.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(40).compareTo(resetPeriod.getOutstandingAmount()));
    }

    @Test
    void applyRepayment_skipsWhenBreachEvaluationDisabled() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        when(breachActionRepository.isBreachDisabledAsOf(LOAN_ID, LocalDate.of(2026, 6, 1))).thenReturn(true);

        underTest.applyRepayment(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        verify(repository, never()).findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any());
        verify(balanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void applyRepaymentUndo_recalculatesPastDueAmountAfterUpdatingPeriod() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        final WorkingCapitalLoanBreachSchedule currentPeriod = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.valueOf(60), BigDecimal.valueOf(40));
        when(repository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, transactionDate, transactionDate))
                .thenReturn(Optional.of(currentPeriod));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(currentPeriod));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.applyRepaymentUndo(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        assertEquals(0, BigDecimal.ZERO.compareTo(currentPeriod.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(currentPeriod.getOutstandingAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void applyRepaymentUndo_reflipsBreachForAlreadyEndedPeriod() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        final WorkingCapitalLoanBreachSchedule endedPeriod = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.ZERO);
        endedPeriod.setBreach(false);
        when(repository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, transactionDate, transactionDate))
                .thenReturn(Optional.of(endedPeriod));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(endedPeriod));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.applyRepaymentUndo(LOAN_ID, transactionDate, BigDecimal.valueOf(100));

        assertEquals(0, BigDecimal.valueOf(100).compareTo(endedPeriod.getOutstandingAmount()));
        assertEquals(Boolean.TRUE, endedPeriod.getBreach());
    }

    @Test
    void applyRepaymentUndo_updatesResetPeriod() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        final WorkingCapitalLoanBreachSchedule resetPeriod = period(1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.valueOf(60), BigDecimal.valueOf(40));
        resetPeriod.setReset(true);
        when(repository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, transactionDate, transactionDate))
                .thenReturn(Optional.of(resetPeriod));

        underTest.applyRepaymentUndo(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        assertEquals(0, BigDecimal.ZERO.compareTo(resetPeriod.getPaidAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(resetPeriod.getOutstandingAmount()));
        assertEquals(Boolean.TRUE, resetPeriod.getBreach());
    }

    @Test
    void applyRepaymentUndo_skipsWhenBreachEvaluationDisabled() {
        final LocalDate transactionDate = LocalDate.of(2026, 5, 15);
        when(breachActionRepository.isBreachDisabledAsOf(LOAN_ID, LocalDate.of(2026, 6, 1))).thenReturn(true);

        underTest.applyRepaymentUndo(LOAN_ID, transactionDate, BigDecimal.valueOf(60));

        verify(repository, never()).findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any());
        verify(balanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void recalculatePeriodsForPauses_recalculatesPastDueAmount() {
        final WorkingCapitalBreach breachConfig = new WorkingCapitalBreach();
        breachConfig.setBreachFrequency(7);
        breachConfig.setBreachFrequencyType(WorkingCapitalLoanPeriodFrequencyType.DAYS);
        breachConfig.setBreachAmountCalculationType(WorkingCapitalBreachAmountCalculationType.FLAT);
        breachConfig.setBreachAmount(BigDecimal.valueOf(100));
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setBreach(breachConfig);
        loan.setLoanProductRelatedDetails(details);

        final WorkingCapitalLoanBreachSchedule onlyPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(onlyPeriod));
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of());
        when(breachActionRepository.findByWorkingCapitalLoanIdAndActionOrderByIdDesc(anyLong(), any())).thenReturn(List.of());
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.recalculatePeriodsForPauses(loan);

        assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void reprocessBreachSchedule_bucketsTransactionsIntoTheirOwnPeriod() {
        final WorkingCapitalLoanBreachSchedule period1 = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        final WorkingCapitalLoanBreachSchedule period2 = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));

        final TransactionDateAndAmountHolder txn = new TransactionDateAndAmountHolder(LocalDate.of(2026, 5, 5), BigDecimal.valueOf(100));

        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(period1, period2));
        when(transactionRepository.fetchTransactionDateAndAmount(anyLong(), any())).thenReturn(List.of(txn));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));

        underTest.reprocessBreachSchedule(loan);

        assertEquals(0, BigDecimal.ZERO.compareTo(period1.getOutstandingAmount()));
        assertEquals(Boolean.FALSE, period1.getBreach());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(period2.getOutstandingAmount()));
        assertEquals(Boolean.TRUE, period2.getBreach());

        assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()));
    }

    @Test
    void reprocessBreachSchedule_recalculatesResetPeriods() {
        final WorkingCapitalLoanBreachSchedule resetPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));
        resetPeriod.setReset(true);
        final WorkingCapitalLoanBreachSchedule normalPeriod = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20),
                BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.valueOf(100));

        final TransactionDateAndAmountHolder txn = new TransactionDateAndAmountHolder(LocalDate.of(2026, 5, 5), BigDecimal.valueOf(100));

        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(resetPeriod, normalPeriod));
        when(transactionRepository.fetchTransactionDateAndAmount(anyLong(), any())).thenReturn(List.of(txn));
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));
        givenActions(restartReset(1L, LocalDate.of(2026, 5, 1)));

        underTest.reprocessBreachSchedule(loan);

        assertTrue(resetPeriod.isReset());
        assertFalse(normalPeriod.isReset());
        assertEquals(0, BigDecimal.ZERO.compareTo(resetPeriod.getOutstandingAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(resetPeriod.getPaidAmount()));
        assertEquals(Boolean.FALSE, resetPeriod.getBreach());

        assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()));
    }

    private void givenBreachConfig(final int frequency, final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        final WorkingCapitalBreach breachConfig = new WorkingCapitalBreach();
        breachConfig.setBreachFrequency(frequency);
        breachConfig.setBreachFrequencyType(frequencyType);
        breachConfig.setBreachAmountCalculationType(WorkingCapitalBreachAmountCalculationType.FLAT);
        breachConfig.setBreachAmount(BigDecimal.valueOf(100));
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setBreach(breachConfig);
        loan.setLoanProductRelatedDetails(details);
    }

    private void givenReschedules(final WorkingCapitalLoanBreachAction... reschedulesLatestFirst) {
        when(breachActionRepository.findByWorkingCapitalLoanIdAndActionOrderByIdDesc(LOAN_ID, WorkingCapitalLoanBreachActionType.RESCHEDULE))
                .thenReturn(List.of(reschedulesLatestFirst));
    }

    private void givenActions(final WorkingCapitalLoanBreachAction... actions) {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(actions));
    }

    private WorkingCapitalLoanBreachSchedule period(final int periodNumber, final LocalDate fromDate, final LocalDate toDate,
            final int numberOfDays) {
        final WorkingCapitalLoanBreachSchedule period = period(periodNumber, fromDate, toDate, BigDecimal.valueOf(100), BigDecimal.ZERO,
                BigDecimal.valueOf(100));
        period.setNumberOfDays(numberOfDays);
        return period;
    }

    private WorkingCapitalLoanBreachAction pause(final LocalDate startDate, final LocalDate endDate) {
        final WorkingCapitalLoanBreachAction pause = new WorkingCapitalLoanBreachAction();
        pause.setAction(WorkingCapitalLoanBreachActionType.PAUSE);
        pause.setStartDate(startDate);
        pause.setEndDate(endDate);
        return pause;
    }

    private WorkingCapitalLoanBreachAction restartReset(final long id, final LocalDate resetDate) {
        final WorkingCapitalLoanBreachAction reset = new WorkingCapitalLoanBreachAction();
        reset.setId(id);
        reset.setAction(WorkingCapitalLoanBreachActionType.RESET);
        reset.setStartDate(resetDate);
        reset.setRestartPeriodFromResetDate(true);
        return reset;
    }

    private WorkingCapitalLoanBreachAction reschedule(final long id, final int frequency,
            final WorkingCapitalLoanPeriodFrequencyType frequencyType) {
        final WorkingCapitalLoanBreachAction reschedule = new WorkingCapitalLoanBreachAction();
        reschedule.setId(id);
        reschedule.setAction(WorkingCapitalLoanBreachActionType.RESCHEDULE);
        reschedule.setFrequency(frequency);
        reschedule.setFrequencyType(frequencyType);
        return reschedule;
    }

    private void assertBounds(final WorkingCapitalLoanBreachSchedule period, final LocalDate fromDate, final LocalDate toDate,
            final int numberOfDays) {
        assertEquals(fromDate, period.getFromDate());
        assertEquals(toDate, period.getToDate());
        assertEquals(numberOfDays, period.getNumberOfDays().intValue());
    }

    @Test
    void splitPeriodAtReset_endsThePeriodHoldingTheResetDateTheDayBeforeAndOpensANewOne() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules();
        givenActions();
        final WorkingCapitalLoanBreachSchedule lastPeriod = period(1, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 10), 17);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(lastPeriod));
        when(repository.findTopByLoanIdOrderByPeriodNumberDesc(LOAN_ID)).thenReturn(Optional.of(lastPeriod));

        underTest.splitPeriodAtReset(loan, LocalDate.of(2026, 6, 1));

        assertBounds(lastPeriod, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 31), 7);
        final ArgumentCaptor<List<WorkingCapitalLoanBreachSchedule>> generated = ArgumentCaptor.captor();
        verify(repository).saveAllAndFlush(generated.capture());
        assertEquals(1, generated.getValue().size());
        final WorkingCapitalLoanBreachSchedule newPeriod = generated.getValue().getFirst();
        assertEquals(2, newPeriod.getPeriodNumber().intValue());
        assertBounds(newPeriod, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 7), 7);
        verify(repository, never()).deleteAll(any());
    }

    @Test
    void splitPeriodAtReset_resetDateStartingThePeriod_leavesItsBoundsUntouched() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        final WorkingCapitalLoanBreachSchedule lastPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 10);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(lastPeriod));
        when(repository.findTopByLoanIdOrderByPeriodNumberDesc(LOAN_ID)).thenReturn(Optional.of(lastPeriod));

        underTest.splitPeriodAtReset(loan, LocalDate.of(2026, 5, 1));

        assertBounds(lastPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 10);
        verify(repository, never()).saveAllAndFlush(any());
        verify(repository, never()).deleteAll(any());
    }

    @Test
    void splitPeriodAtReset_resetDateInAnEarlierPeriod_cutsThatPeriodAndReplacesTheLaterOnes() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules();
        givenActions();
        final WorkingCapitalLoanBreachSchedule period1 = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 10);
        final WorkingCapitalLoanBreachSchedule period2 = period(2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 10);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(period1, period2));
        when(repository.findTopByLoanIdOrderByPeriodNumberDesc(LOAN_ID)).thenReturn(Optional.of(period1));

        underTest.splitPeriodAtReset(loan, LocalDate.of(2026, 5, 5));

        assertBounds(period1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), 4);
        verify(repository).deleteAll(List.of(period2));
        verify(repository).flush();
        final ArgumentCaptor<List<WorkingCapitalLoanBreachSchedule>> generated = ArgumentCaptor.captor();
        verify(repository).saveAllAndFlush(generated.capture());
        assertEquals(1, generated.getValue().size());
        assertBounds(generated.getValue().getFirst(), LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 11), 7);
    }

    @Test
    void restoreSplitPeriod_restoresTheSplitPeriodAndDeletesEverythingAfterIt() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules();
        givenActions();
        final WorkingCapitalLoanBreachSchedule splitPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), 4);
        final WorkingCapitalLoanBreachSchedule openedPeriod = period(2, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 11), 7);
        final WorkingCapitalLoanBreachSchedule nextPeriod = period(3, LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 18), 7);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(splitPeriod, openedPeriod, nextPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 5)));

        assertBounds(splitPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7);
        verify(repository).deleteAll(List.of(openedPeriod, nextPeriod));
        verify(repository).flush();
        verify(repository, never()).saveAllAndFlush(any());
        verify(transactionRepository, never()).fetchTransactionDateAndAmount(anyLong(), any());
    }

    @Test
    void restoreSplitPeriod_restoresThePauseExtendedBoundary() {
        givenBreachConfig(60, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules();
        givenActions(pause(LocalDate.of(2026, 3, 20), LocalDate.of(2026, 3, 29)));
        final WorkingCapitalLoanBreachSchedule splitPeriod = period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 14), 44);
        final WorkingCapitalLoanBreachSchedule openedPeriod = period(3, LocalDate.of(2026, 4, 15), LocalDate.of(2026, 6, 13), 60);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(splitPeriod, openedPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 4, 15)));

        assertBounds(splitPeriod, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 5, 10), 70);
        verify(repository).deleteAll(List.of(openedPeriod));
    }

    @Test
    void restoreSplitPeriod_givesTheFirstPeriodItsGraceDaysBack() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        loan.getLoanProductRelatedDetails().setBreachGraceDays(3);
        givenReschedules();
        givenActions();
        final WorkingCapitalLoanBreachSchedule firstPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), 4);
        final WorkingCapitalLoanBreachSchedule openedPeriod = period(2, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 11), 7);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(firstPeriod, openedPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 5)));

        assertBounds(firstPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 10);
    }

    @Test
    void restoreSplitPeriod_usesTheParametersInForceAtTheReset() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules(reschedule(20L, 30, WorkingCapitalLoanPeriodFrequencyType.DAYS),
                reschedule(5L, 10, WorkingCapitalLoanPeriodFrequencyType.DAYS));
        givenActions();
        final WorkingCapitalLoanBreachSchedule splitPeriod = period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), 4);
        final WorkingCapitalLoanBreachSchedule openedPeriod = period(3, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 6, 3), 30);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(splitPeriod, openedPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 5)));

        assertBounds(splitPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 10);
    }

    @Test
    void restoreSplitPeriod_resetOnAPeriodStartDate_leavesTheScheduleUntouched() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenReschedules();
        givenActions();
        final WorkingCapitalLoanBreachSchedule previousPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7);
        final WorkingCapitalLoanBreachSchedule currentPeriod = period(2, LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), 7);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(previousPeriod, currentPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 8)));

        assertBounds(previousPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7);
        assertBounds(currentPeriod, LocalDate.of(2026, 5, 8), LocalDate.of(2026, 5, 14), 7);
        verify(repository, never()).deleteAll(any());
    }

    @Test
    void restoreSplitPeriod_resetOnTheFirstDayOfTheSchedule_leavesTheScheduleUntouched() {
        givenBreachConfig(7, WorkingCapitalLoanPeriodFrequencyType.DAYS);
        final WorkingCapitalLoanBreachSchedule firstPeriod = period(1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7);
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(firstPeriod));

        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 1)));

        assertBounds(firstPeriod, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 7);
        verify(repository, never()).deleteAll(any());
    }

    @Test
    void restoreSplitPeriod_noBreachConfiguration_leavesTheScheduleUntouched() {
        underTest.restoreSplitPeriod(loan, restartReset(10L, LocalDate.of(2026, 5, 5)));

        verify(repository, never()).findByLoanIdOrderByPeriodNumberAsc(anyLong());
        verify(repository, never()).deleteAll(any());
    }
}
