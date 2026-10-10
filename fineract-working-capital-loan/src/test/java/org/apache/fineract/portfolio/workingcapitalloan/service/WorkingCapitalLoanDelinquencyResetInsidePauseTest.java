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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucket;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRule;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * A reset that starts a new period while a pause is active cuts the current period on the day before the reset and
 * starts the next one on the reset date itself. The restarted period must not move past the pause (that would leave the
 * days in between belonging to no period, so a repayment dated there would be lost), and a later resume must not pull
 * its start back before the reset either.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyResetInsidePauseTest {

    private static final Long LOAN_ID = 1L;
    private static final LocalDate RESET_DATE = LocalDate.of(2026, 1, 12);
    private static final BigDecimal EXPECTED = new BigDecimal("100.00");

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryRepository tagHistoryRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleMapper mapper;
    @Mock
    private DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyClassificationService classificationService;
    @Mock
    private WorkingCapitalLoanTransactionRepository transactionRepository;
    @Mock
    private WorkingCapitalLoan loan;
    @Mock
    private WorkingCapitalLoanProductRelatedDetails productRelatedDetails;
    @Mock
    private DelinquencyBucket bucket;

    @InjectMocks
    private WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl rangeScheduleService;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, RESET_DATE.plusDays(1));
        businessDates.put(BusinessDateType.COB_DATE, RESET_DATE);
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());

        when(loan.getId()).thenReturn(LOAN_ID);
        when(loan.getApprovedPrincipal()).thenReturn(new BigDecimal("1000"));
        when(loan.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        when(loan.getLoanProductRelatedDetails()).thenReturn(productRelatedDetails);
        when(productRelatedDetails.getDelinquencyBucket()).thenReturn(bucket);
        when(bucket.getId()).thenReturn(7L);
        final DelinquencyMinimumPaymentPeriodAndRule rule = new DelinquencyMinimumPaymentPeriodAndRule();
        rule.setFrequency(6);
        rule.setFrequencyType(DelinquencyFrequencyType.DAYS);
        rule.setMinimumPayment(EXPECTED);
        rule.setMinimumPaymentType(DelinquencyMinimumPaymentType.FLAT);
        when(minimumPaymentPeriodAndRuleRepository.findByBucketId(7L)).thenReturn(Optional.of(rule));
        when(actionRepository.findByWorkingCapitalLoanIdAndActionOrderByIdDesc(LOAN_ID, DelinquencyAction.RESCHEDULE))
                .thenReturn(List.of());
        when(rangeScheduleRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actionRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        MoneyHelper.clearCacheForTenant("default");
        ThreadLocalContextUtil.reset();
    }

    @Test
    void resetPeriods_startNewPeriodInsideActivePause_startsTheNewPeriodOnTheResetDate() {
        // 6-day periods, the pause 11-13 Jan already extended period 2 from 07-12 to 07-15 Jan
        final WorkingCapitalLoanDelinquencyRangeSchedule period1 = period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6));
        final WorkingCapitalLoanDelinquencyRangeSchedule period2 = period(2, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 15));
        final WorkingCapitalLoanDelinquencyAction reset = reset(RESET_DATE);
        when(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(new ArrayList<>(List.of(period1, period2)));
        when(rangeScheduleRepository.findTopByLoanIdOrderByPeriodNumberDesc(LOAN_ID)).thenReturn(Optional.of(period2));
        when(actionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID))
                .thenReturn(List.of(pause(LocalDate.of(2026, 1, 11), LocalDate.of(2026, 1, 13)), reset));

        rangeScheduleService.resetPeriods(loan, reset);

        assertThat(period2.getToDate()).isEqualTo(RESET_DATE.minusDays(1));
        assertThat(period2.getReset()).isTrue();
        assertThat(period1.getReset()).isTrue();
        final ArgumentCaptor<WorkingCapitalLoanDelinquencyRangeSchedule> saved = ArgumentCaptor
                .forClass(WorkingCapitalLoanDelinquencyRangeSchedule.class);
        verify(rangeScheduleRepository).saveAndFlush(saved.capture());
        final WorkingCapitalLoanDelinquencyRangeSchedule restarted = saved.getValue();
        assertThat(restarted.getPeriodNumber()).isEqualTo(3);
        // starts on the reset date, 6 days extended by the pause days 12 and 13 Jan only
        assertThat(restarted.getFromDate()).isEqualTo(RESET_DATE);
        assertThat(restarted.getToDate()).isEqualTo(LocalDate.of(2026, 1, 19));
        assertThat(restarted.getReset()).isFalse();
        assertThat(restarted.getExpectedAmount()).isEqualByComparingTo(EXPECTED);
    }

    @Test
    void resumeActivePause_afterResetInsideThePause_leavesTheCutPeriodAloneAndKeepsTheRestartedStart() {
        // pause 11-15 Jan, reset 12 Jan: period 2 was cut to 07-11 and period 3 restarted on 12 Jan with 4 pause days
        final WorkingCapitalLoanDelinquencyRangeSchedule period1 = period(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 6));
        final WorkingCapitalLoanDelinquencyRangeSchedule period2 = period(2, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 11));
        period1.reset();
        period2.reset();
        final WorkingCapitalLoanDelinquencyRangeSchedule period3 = period(3, RESET_DATE, LocalDate.of(2026, 1, 21));
        final WorkingCapitalLoanDelinquencyAction pause = pause(LocalDate.of(2026, 1, 11), LocalDate.of(2026, 1, 15));
        final WorkingCapitalLoanDelinquencyAction resume = new WorkingCapitalLoanDelinquencyAction();
        resume.setAction(DelinquencyAction.RESUME);
        resume.setStartDate(LocalDate.of(2026, 1, 13));
        when(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(period1, period2, period3));
        when(actionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(pause, reset(RESET_DATE), resume));

        rangeScheduleService.resumeActivePause(loan, pause, resume);

        // the resume drops the pause days 14 and 15 Jan: only the restarted period shortens, from its end
        assertThat(period1.getFromDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(period1.getToDate()).isEqualTo(LocalDate.of(2026, 1, 6));
        assertThat(period2.getFromDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(period2.getToDate()).isEqualTo(RESET_DATE.minusDays(1));
        assertThat(period3.getFromDate()).isEqualTo(RESET_DATE);
        assertThat(period3.getToDate()).isEqualTo(LocalDate.of(2026, 1, 19));
    }

    @Test
    void resumeActivePause_afterTheResetInsideThePauseWasUndone_keepsTheRestartedStart() {
        // pause 11-15 Jan, reset 12 Jan undone on 13 Jan: the undo cleared the flags and the reprocess evaluated period
        // 2, but the cut stays, so period 3 still begins on the reset date
        final WorkingCapitalLoanDelinquencyRangeSchedule period2 = period(2, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 11));
        period2.setMinPaymentCriteriaMet(false);
        final WorkingCapitalLoanDelinquencyRangeSchedule period3 = period(3, RESET_DATE, LocalDate.of(2026, 1, 21));
        final WorkingCapitalLoanDelinquencyAction pause = pause(LocalDate.of(2026, 1, 11), LocalDate.of(2026, 1, 15));
        final WorkingCapitalLoanDelinquencyAction undoneReset = reset(RESET_DATE);
        undoneReset.setEndDate(LocalDate.of(2026, 1, 13));
        final WorkingCapitalLoanDelinquencyAction undo = new WorkingCapitalLoanDelinquencyAction();
        undo.setAction(DelinquencyAction.UNDO_RESET);
        undo.setStartDate(LocalDate.of(2026, 1, 13));
        final WorkingCapitalLoanDelinquencyAction resume = new WorkingCapitalLoanDelinquencyAction();
        resume.setAction(DelinquencyAction.RESUME);
        resume.setStartDate(LocalDate.of(2026, 1, 13));
        when(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(period2, period3));
        when(actionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(pause, undoneReset, undo, resume));

        rangeScheduleService.resumeActivePause(loan, pause, resume);

        assertThat(period2.getFromDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(period2.getToDate()).isEqualTo(RESET_DATE.minusDays(1));
        assertThat(period3.getFromDate()).isEqualTo(RESET_DATE);
        assertThat(period3.getToDate()).isEqualTo(LocalDate.of(2026, 1, 19));
    }

    @Test
    void resumeActivePause_withoutReset_stillMovesThePeriodsBackPastThePause() {
        // pause 11-15 Jan extended period 2 from 07-12 to 07-17 and pushed period 3 to 18-23 Jan
        final WorkingCapitalLoanDelinquencyRangeSchedule period2 = period(2, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 17));
        final WorkingCapitalLoanDelinquencyRangeSchedule period3 = period(3, LocalDate.of(2026, 1, 18), LocalDate.of(2026, 1, 23));
        final WorkingCapitalLoanDelinquencyAction pause = pause(LocalDate.of(2026, 1, 11), LocalDate.of(2026, 1, 15));
        final WorkingCapitalLoanDelinquencyAction resume = new WorkingCapitalLoanDelinquencyAction();
        resume.setAction(DelinquencyAction.RESUME);
        resume.setStartDate(LocalDate.of(2026, 1, 13));
        when(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(period2, period3));
        when(actionRepository.findByWorkingCapitalLoanIdOrderById(LOAN_ID)).thenReturn(List.of(pause, resume));

        rangeScheduleService.resumeActivePause(loan, pause, resume);

        assertThat(period2.getFromDate()).isEqualTo(LocalDate.of(2026, 1, 7));
        assertThat(period2.getToDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(period3.getFromDate()).isEqualTo(LocalDate.of(2026, 1, 16));
        assertThat(period3.getToDate()).isEqualTo(LocalDate.of(2026, 1, 21));
    }

    private WorkingCapitalLoanDelinquencyRangeSchedule period(final int periodNumber, final LocalDate fromDate, final LocalDate toDate) {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setBaseExpectedAmount(EXPECTED);
        period.setExpectedAmount(EXPECTED);
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(EXPECTED);
        period.setMinPaymentCriteriaMet(null);
        return period;
    }

    private static WorkingCapitalLoanDelinquencyAction pause(final LocalDate startDate, final LocalDate endDate) {
        final WorkingCapitalLoanDelinquencyAction pause = new WorkingCapitalLoanDelinquencyAction();
        pause.setAction(DelinquencyAction.PAUSE);
        pause.setStartDate(startDate);
        pause.setEndDate(endDate);
        return pause;
    }

    private static WorkingCapitalLoanDelinquencyAction reset(final LocalDate resetDate) {
        final WorkingCapitalLoanDelinquencyAction reset = new WorkingCapitalLoanDelinquencyAction();
        reset.setAction(DelinquencyAction.RESET);
        reset.setStartDate(resetDate);
        reset.setStartNewPeriod(true);
        return reset;
    }
}
