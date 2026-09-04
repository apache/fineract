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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Real schedule and reset services over an in-memory schedule repository, so deletes and regenerations take effect. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanBreachResetFlagDerivationTest {

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

    private WorkingCapitalLoanBreachScheduleServiceImpl scheduleService;
    private WorkingCapitalLoanBreachResetServiceImpl resetService;
    private WorkingCapitalLoan loan;
    private WorkingCapitalLoanBalance balance;
    private FineractPlatformTenant originalTenant;
    private final List<WorkingCapitalLoanBreachSchedule> schedule = new ArrayList<>();

    @BeforeEach
    void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        final WorkingCapitalLoanActiveBreachResetResolver resolver = new WorkingCapitalLoanActiveBreachResetResolver(
                breachActionRepository);
        scheduleService = new WorkingCapitalLoanBreachScheduleServiceImpl(repository, mapper, loanRepository, breachActionRepository,
                transactionRepository, balanceRepository, businessEventNotifierService, resolver);
        resetService = new WorkingCapitalLoanBreachResetServiceImpl(scheduleService, resolver);
        loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
        balance = WorkingCapitalLoanBalance.createFor(loan);
        balance.setPrincipal(BigDecimal.valueOf(10_000));
        lenient().when(breachActionRepository.isBreachDisabledAsOf(anyLong(), any())).thenReturn(false);
        when(breachActionRepository.findByWorkingCapitalLoanIdAndActionOrderByIdDesc(anyLong(), any())).thenReturn(List.of());
        when(balanceRepository.findByWcLoan_Id(LOAN_ID)).thenReturn(Optional.of(balance));
        when(repository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenAnswer(inv -> sorted());
        when(repository.findTopByLoanIdOrderByPeriodNumberDesc(LOAN_ID))
                .thenAnswer(inv -> sorted().isEmpty() ? Optional.empty() : Optional.of(sorted().getLast()));
        when(repository.saveAllAndFlush(any())).thenAnswer(inv -> {
            final List<WorkingCapitalLoanBreachSchedule> saved = inv.getArgument(0);
            schedule.addAll(saved);
            return saved;
        });
        lenient().doAnswer(inv -> {
            final Iterable<WorkingCapitalLoanBreachSchedule> gone = inv.getArgument(0);
            gone.forEach(schedule::remove);
            return null;
        }).when(repository).deleteAll(any());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
        MoneyHelper.clearCacheForTenant("default");
    }

    private List<WorkingCapitalLoanBreachSchedule> sorted() {
        return new ArrayList<>(
                schedule.stream().sorted(Comparator.comparingInt(WorkingCapitalLoanBreachSchedule::getPeriodNumber)).toList());
    }

    private void businessDate(final LocalDate date) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, date)));
    }

    private void givenBreachConfig(final int frequency) {
        final WorkingCapitalBreach breachConfig = new WorkingCapitalBreach();
        breachConfig.setBreachFrequency(frequency);
        breachConfig.setBreachFrequencyType(WorkingCapitalLoanPeriodFrequencyType.DAYS);
        breachConfig.setBreachAmountCalculationType(WorkingCapitalBreachAmountCalculationType.FLAT);
        breachConfig.setBreachAmount(BigDecimal.valueOf(100));
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setBreach(breachConfig);
        loan.setLoanProductRelatedDetails(details);
    }

    private WorkingCapitalLoanBreachSchedule period(final int number, final LocalDate from, final LocalDate to, final boolean reset) {
        final WorkingCapitalLoanBreachSchedule p = new WorkingCapitalLoanBreachSchedule();
        p.setLoan(loan);
        p.setPeriodNumber(number);
        p.setFromDate(from);
        p.setToDate(to);
        p.setNumberOfDays((int) (to.toEpochDay() - from.toEpochDay() + 1));
        p.setBaseMinPaymentAmount(BigDecimal.valueOf(100));
        p.setMinPaymentAmount(BigDecimal.valueOf(100));
        p.setPaidAmount(BigDecimal.ZERO);
        p.setOutstandingAmount(BigDecimal.valueOf(100));
        p.setReset(reset);
        schedule.add(p);
        return p;
    }

    private WorkingCapitalLoanBreachAction action(final long id, final WorkingCapitalLoanBreachActionType type, final LocalDate start) {
        final WorkingCapitalLoanBreachAction a = new WorkingCapitalLoanBreachAction();
        a.setId(id);
        a.setAction(type);
        a.setStartDate(start);
        return a;
    }

    private WorkingCapitalLoanBreachAction restartReset(final long id, final LocalDate date) {
        final WorkingCapitalLoanBreachAction r = action(id, WorkingCapitalLoanBreachActionType.RESET, date);
        r.setRestartPeriodFromResetDate(true);
        return r;
    }

    private WorkingCapitalLoanBreachAction pause(final long id, final LocalDate start, final LocalDate end) {
        final WorkingCapitalLoanBreachAction p = action(id, WorkingCapitalLoanBreachActionType.PAUSE, start);
        p.setEndDate(end);
        return p;
    }

    private void givenActions(final WorkingCapitalLoanBreachAction... actions) {
        when(breachActionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(actions));
    }

    private WorkingCapitalLoanBreachSchedule periodContaining(final LocalDate date) {
        return sorted().stream().filter(p -> !p.getFromDate().isAfter(date) && !p.getToDate().isBefore(date)).findFirst().orElseThrow();
    }

    private String dump() {
        final StringBuilder sb = new StringBuilder();
        sorted().forEach(p -> sb.append(String.format("P%d [%s..%s] days=%d reset=%s outstanding=%s%n", p.getPeriodNumber(),
                p.getFromDate(), p.getToDate(), p.getNumberOfDays(), p.isReset(), p.getOutstandingAmount())));
        sb.append("pastDue=").append(balance.getBreachPastDueAmount());
        return sb.toString();
    }

    /** The pause recalculation still re-dates every period, so the flag must follow the reset date to its new row. */
    @Test
    void pauseAfterRestartReset_keepsTheFlagOnThePeriodHoldingTheResetDate() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 7, 1));
        final LocalDate resetDate = LocalDate.of(2026, 4, 15);
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), false);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 14), false);
        period(3, resetDate, LocalDate.of(2026, 6, 13), true);
        givenActions(restartReset(1L, resetDate), pause(2L, LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 29)));

        scheduleService.recalculatePeriodsForPauses(loan);

        final String state = dump();
        final WorkingCapitalLoanBreachSchedule holdingReset = periodContaining(resetDate);
        assertAll(state, //
                () -> assertEquals(2, holdingReset.getPeriodNumber(), "the re-dated period 2 holds the reset date"),
                () -> assertTrue(holdingReset.isReset(), "period holding the reset date is flagged"),
                () -> assertEquals(1, sorted().stream().filter(WorkingCapitalLoanBreachSchedule::isReset).count(), "one flag"),
                () -> assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()),
                        "past due anchored on the flagged period 2, ended unpaid on 05-10"));
    }

    /**
     * Reset A on 06-10, business date back to 05-05, reset B, forward to 06-15, undo B: A lies beyond the restored
     * period.
     */
    @Test
    void undoAfterBackwardsBusinessDateMove_flagsThePeriodHoldingTheStillActiveReset() {
        givenBreachConfig(30);
        final WorkingCapitalLoanBreachAction resetA = restartReset(1L, LocalDate.of(2026, 6, 10));
        final WorkingCapitalLoanBreachAction resetB = restartReset(2L, LocalDate.of(2026, 5, 5));
        final WorkingCapitalLoanBreachAction undoB = action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 6, 15));
        period(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), false);
        period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 4), false);
        period(3, LocalDate.of(2026, 5, 5), LocalDate.of(2026, 6, 3), true);
        givenActions(resetA, resetB, undoB);
        businessDate(LocalDate.of(2026, 6, 15));

        resetService.undoResetBreach(loan, undoB, List.of(resetA, resetB));

        final String state = dump();
        final WorkingCapitalLoanBreachSchedule holdingA = periodContaining(resetA.getStartDate());
        assertAll(state, //
                () -> assertTrue(holdingA.isReset(), "period holding reset A (06-10) is flagged"),
                () -> assertFalse(periodContaining(LocalDate.of(2026, 5, 15)).isReset(), "restored P2 is not flagged"),
                () -> assertEquals(1, sorted().stream().filter(WorkingCapitalLoanBreachSchedule::isReset).count(), "one flag"),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(balance.getBreachPastDueAmount()),
                        "past due anchored on the open period holding A, so zero"));
    }

    @Test
    void undoOfStackedRestartResets_flagsTheEarlierReset() {
        givenBreachConfig(30);
        final WorkingCapitalLoanBreachAction resetA = restartReset(1L, LocalDate.of(2026, 5, 20));
        final WorkingCapitalLoanBreachAction resetB = restartReset(2L, LocalDate.of(2026, 6, 10));
        final WorkingCapitalLoanBreachAction undoB = action(3L, WorkingCapitalLoanBreachActionType.UNDO_RESET, LocalDate.of(2026, 6, 15));
        period(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), false);
        period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 19), false);
        period(3, LocalDate.of(2026, 5, 20), LocalDate.of(2026, 6, 9), true);
        period(4, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 7, 9), true);
        givenActions(resetA, resetB, undoB);
        businessDate(LocalDate.of(2026, 6, 15));

        resetService.undoResetBreach(loan, undoB, List.of(resetA, resetB));

        final String state = dump();
        assertAll(state, //
                () -> assertTrue(periodContaining(LocalDate.of(2026, 5, 20)).isReset(), "P3 holding A flagged"),
                () -> assertEquals(1, sorted().stream().filter(WorkingCapitalLoanBreachSchedule::isReset).count(), "one flag"));
    }

    private WorkingCapitalLoanBreachAction plainReset(final long id, final LocalDate date) {
        return action(id, WorkingCapitalLoanBreachActionType.RESET, date);
    }

    private WorkingCapitalLoanBreachAction undo(final long id, final LocalDate date) {
        return action(id, WorkingCapitalLoanBreachActionType.UNDO_RESET, date);
    }

    private List<Boolean> flags() {
        return sorted().stream().map(WorkingCapitalLoanBreachSchedule::isReset).toList();
    }

    @Test
    void plainReset_flagsTheActionDatePeriodOnlyAndKeepsItsValues() {
        givenBreachConfig(30);
        businessDate(LocalDate.of(2026, 4, 20));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), false);
        final WorkingCapitalLoanBreachSchedule actionDatePeriod = period(2, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), false);
        actionDatePeriod.setPaidAmount(BigDecimal.valueOf(40));
        actionDatePeriod.setOutstandingAmount(BigDecimal.valueOf(60));
        actionDatePeriod.setBreach(Boolean.TRUE);
        period(3, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), false);
        final WorkingCapitalLoanBreachAction reset = plainReset(1L, LocalDate.of(2026, 4, 15));
        givenActions(reset);

        resetService.resetBreach(loan, reset);

        assertAll(dump(), //
                () -> assertEquals(List.of(false, true, false), flags()),
                () -> assertEquals(0, BigDecimal.valueOf(40).compareTo(actionDatePeriod.getPaidAmount())),
                () -> assertEquals(0, BigDecimal.valueOf(60).compareTo(actionDatePeriod.getOutstandingAmount())),
                () -> assertEquals(Boolean.TRUE, actionDatePeriod.getBreach()));
    }

    @Test
    void stackedPlainResets_keepTheEarlierActiveResetFlagged() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 4, 15));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), false);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 30), false);
        final WorkingCapitalLoanBreachAction latest = plainReset(2L, LocalDate.of(2026, 4, 15));
        givenActions(plainReset(1L, LocalDate.of(2026, 2, 20)), latest);

        resetService.resetBreach(loan, latest);

        assertEquals(List.of(true, true), flags(), dump());
    }

    @Test
    void plainReset_anEarlierUndoneResetDoesNotFlagItsPeriod() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 4, 15));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), true);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 30), false);
        final WorkingCapitalLoanBreachAction latest = plainReset(3L, LocalDate.of(2026, 4, 15));
        givenActions(plainReset(1L, LocalDate.of(2026, 2, 20)), undo(2L, LocalDate.of(2026, 2, 25)), latest);

        resetService.resetBreach(loan, latest);

        assertEquals(List.of(false, true), flags(), dump());
    }

    @Test
    void plainReset_datedAfterTheLastPeriod_flagsTheLastPeriodUntilTheContainingPeriodIsGenerated() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 5, 5));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), false);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 30), false);
        final WorkingCapitalLoanBreachAction reset = plainReset(1L, LocalDate.of(2026, 5, 5));
        givenActions(reset);

        resetService.resetBreach(loan, reset);

        assertAll(dump(), //
                () -> assertEquals(List.of(false, true), flags()),
                () -> assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount())));

        scheduleService.generateNextPeriodIfNeeded(loan, LocalDate.of(2026, 5, 5));
        scheduleService.recalculatePastDueAmount(loan);

        assertAll(dump(), //
                () -> assertEquals(List.of(false, false, true), flags()),
                () -> assertEquals(LocalDate.of(2026, 5, 1), periodContaining(reset.getStartDate()).getFromDate()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(balance.getBreachPastDueAmount())));
    }

    @Test
    void rescheduleAfterABackwardsBusinessDateMove_movesTheFlagWithTheResetDate() {
        givenBreachConfig(30);
        businessDate(LocalDate.of(2026, 5, 5));
        period(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), false);
        final WorkingCapitalLoanBreachSchedule current = period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 30), true);
        final WorkingCapitalLoanBreachSchedule future = period(3, LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 29), false);
        final WorkingCapitalLoanBreachAction reset = plainReset(1L, LocalDate.of(2026, 5, 20));
        final WorkingCapitalLoanBreachAction reschedule = action(2L, WorkingCapitalLoanBreachActionType.RESCHEDULE,
                LocalDate.of(2026, 5, 5));
        reschedule.setFrequency(10);
        reschedule.setFrequencyType(WorkingCapitalLoanPeriodFrequencyType.DAYS);
        givenActions(reset, reschedule);
        when(breachActionRepository.findByWorkingCapitalLoanIdAndActionOrderByIdDesc(LOAN_ID,
                WorkingCapitalLoanBreachActionType.RESCHEDULE)).thenReturn(List.of(reschedule));
        when(repository.findCurrentOpenPeriod(LOAN_ID, LocalDate.of(2026, 5, 5))).thenReturn(Optional.of(current));
        when(repository.findFuturePeriodsOrderByPeriodNumberAsc(LOAN_ID, LocalDate.of(2026, 5, 5))).thenReturn(List.of(future));

        scheduleService.rescheduleMinimumPayment(loan, reschedule);

        assertAll(dump(), //
                () -> assertEquals(LocalDate.of(2026, 5, 10), current.getToDate()),
                () -> assertEquals(LocalDate.of(2026, 5, 20), future.getToDate()), () -> assertEquals(List.of(false, false, true), flags()),
                () -> assertEquals(0, BigDecimal.ZERO.compareTo(balance.getBreachPastDueAmount())));
    }

    @Test
    void undoOfAPlainReset_liftsTheFlagOfTheResetPeriodNotOfTheUndoDatePeriod() {
        givenBreachConfig(30);
        businessDate(LocalDate.of(2026, 5, 20));
        period(1, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), true);
        period(2, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), false);
        final WorkingCapitalLoanBreachAction reset = plainReset(1L, LocalDate.of(2026, 4, 15));
        final WorkingCapitalLoanBreachAction undo = undo(2L, LocalDate.of(2026, 5, 20));
        givenActions(reset, undo);

        resetService.undoResetBreach(loan, undo, List.of(reset));

        assertAll(dump(), //
                () -> assertEquals(List.of(false, false), flags()),
                () -> assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount()),
                        "past due covers the ended period 1 again once its reset is undone"));
    }

    @Test
    void undoOfStackedPlainResets_popsOnlyTheLatestAndKeepsTheEarlierFlag() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 4, 15));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), true);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 30), true);
        final WorkingCapitalLoanBreachAction earlier = plainReset(1L, LocalDate.of(2026, 2, 20));
        final WorkingCapitalLoanBreachAction latest = plainReset(2L, LocalDate.of(2026, 4, 15));
        final WorkingCapitalLoanBreachAction undo = undo(3L, LocalDate.of(2026, 4, 15));
        givenActions(earlier, latest, undo);

        resetService.undoResetBreach(loan, undo, List.of(earlier, latest));

        assertEquals(List.of(true, false), flags(), dump());
    }

    @Test
    void undoWithoutAnActiveReset_clearsEveryFlagAndRecalculatesThePastDueAmount() {
        givenBreachConfig(60);
        businessDate(LocalDate.of(2026, 4, 20));
        period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), true);
        period(2, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 4, 30), false);
        final WorkingCapitalLoanBreachAction undo = undo(1L, LocalDate.of(2026, 4, 20));
        givenActions(undo);

        resetService.undoResetBreach(loan, undo, List.of());

        assertAll(dump(), //
                () -> assertEquals(List.of(false, false), flags()),
                () -> assertEquals(0, BigDecimal.valueOf(100).compareTo(balance.getBreachPastDueAmount())));
    }
}
