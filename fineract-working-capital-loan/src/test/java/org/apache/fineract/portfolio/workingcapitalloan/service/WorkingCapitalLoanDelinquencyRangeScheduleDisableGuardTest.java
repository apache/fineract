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
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Verifies that the delinquency data (delinquentAmount/delinquentDays) on range schedule periods is frozen while a
 * Delinquency Disable is in effect: repayment and repayment-undo bookkeeping still runs, but no delinquency values are
 * written until the disable is reversed.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyRangeScheduleDisableGuardTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleRepository rangeScheduleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyActionRepository actionRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyRangeScheduleMapper mapper;
    @Mock
    private DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;
    @Mock
    private WorkingCapitalLoanDelinquencyClassificationService classificationService;
    @Mock
    private WorkingCapitalLoan loan;

    @InjectMocks
    private WorkingCapitalLoanDelinquencyRangeScheduleServiceImpl rangeScheduleService;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        final HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        today = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, today);
        businessDates.put(BusinessDateType.COB_DATE, today.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        when(loan.getId()).thenReturn(LOAN_ID);
        when(rangeScheduleRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void repaymentUndoWhileDisabledDoesNotWriteDelinquencyData() {
        when(classificationService.isDelinquencyDisabled(loan, today)).thenReturn(true);
        final WorkingCapitalLoanDelinquencyRangeSchedule pastPeriod = pastOpenPeriod();
        when(rangeScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, today, today))
                .thenReturn(Optional.empty());
        when(rangeScheduleRepository.findByLoanIdAndToDateIsBeforeOrderByPeriodNumberAsc(LOAN_ID, today)).thenReturn(List.of(pastPeriod));

        rangeScheduleService.applyRepaymentUndo(loan, today, new BigDecimal("100"));

        assertThat(pastPeriod.getMinPaymentCriteriaMet()).isFalse();
        assertThat(pastPeriod.getDelinquentAmount()).isNull();
        assertThat(pastPeriod.getDelinquentDays()).isNull();
    }

    @Test
    void repaymentUndoWhileEnabledWritesDelinquencyData() {
        when(classificationService.isDelinquencyDisabled(loan, today)).thenReturn(false);
        final WorkingCapitalLoanDelinquencyRangeSchedule pastPeriod = pastOpenPeriod();
        when(rangeScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, today, today))
                .thenReturn(Optional.empty());
        when(rangeScheduleRepository.findByLoanIdAndToDateIsBeforeOrderByPeriodNumberAsc(LOAN_ID, today)).thenReturn(List.of(pastPeriod));

        rangeScheduleService.applyRepaymentUndo(loan, today, new BigDecimal("100"));

        assertThat(pastPeriod.getMinPaymentCriteriaMet()).isFalse();
        assertThat(pastPeriod.getDelinquentAmount()).isEqualByComparingTo(pastPeriod.getOutstandingAmount());
        assertThat(pastPeriod.getDelinquentDays()).isEqualTo(DateUtils.getDifferenceInDays(pastPeriod.getToDate(), today));
    }

    @Test
    void repaymentWhileDisabledDoesNotResetDelinquencyData() {
        when(classificationService.isDelinquencyDisabled(loan, today)).thenReturn(true);
        final WorkingCapitalLoanDelinquencyRangeSchedule pastPeriod = pastOpenPeriod();
        pastPeriod.setMinPaymentCriteriaMet(false);
        pastPeriod.setDelinquentAmount(new BigDecimal("100"));
        pastPeriod.setDelinquentDays(5L);
        when(rangeScheduleRepository.findPastOpenPeriodsForRepayment(LOAN_ID, today)).thenReturn(List.of(pastPeriod));
        when(rangeScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, today, today))
                .thenReturn(Optional.empty());

        rangeScheduleService.applyRepayment(loan, today, new BigDecimal("100"));

        assertThat(pastPeriod.getMinPaymentCriteriaMet()).isTrue();
        assertThat(pastPeriod.getDelinquentAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(pastPeriod.getDelinquentDays()).isEqualTo(5L);
    }

    @Test
    void repaymentUndoUnpaysNewestPastPeriodFirst() {
        when(classificationService.isDelinquencyDisabled(loan, today)).thenReturn(false);
        // Two fully-paid past periods; the repository returns them ordered by period number ascending (oldest first).
        final WorkingCapitalLoanDelinquencyRangeSchedule olderPeriod = fullyPaidPastPeriod(1, today.minusDays(66), today.minusDays(36));
        final WorkingCapitalLoanDelinquencyRangeSchedule newerPeriod = fullyPaidPastPeriod(2, today.minusDays(35), today.minusDays(6));
        when(rangeScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(LOAN_ID, today, today))
                .thenReturn(Optional.empty());
        when(rangeScheduleRepository.findByLoanIdAndToDateIsBeforeOrderByPeriodNumberAsc(LOAN_ID, today))
                .thenReturn(List.of(olderPeriod, newerPeriod));

        // The undo only covers a single period's payment.
        rangeScheduleService.applyRepaymentUndo(loan, today, new BigDecimal("100"));

        // It must reverse the most recent payment first: the newer period is unpaid, the older one is left untouched.
        assertThat(newerPeriod.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newerPeriod.getOutstandingAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(newerPeriod.getMinPaymentCriteriaMet()).isFalse();
        assertThat(olderPeriod.getPaidAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(olderPeriod.getOutstandingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(olderPeriod.getMinPaymentCriteriaMet()).isTrue();
    }

    private WorkingCapitalLoanDelinquencyRangeSchedule fullyPaidPastPeriod(final int periodNumber, final LocalDate fromDate,
            final LocalDate toDate) {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setExpectedAmount(new BigDecimal("100"));
        period.setPaidAmount(new BigDecimal("100"));
        period.setOutstandingAmount(BigDecimal.ZERO);
        period.setMinPaymentCriteriaMet(true);
        return period;
    }

    private WorkingCapitalLoanDelinquencyRangeSchedule pastOpenPeriod() {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(1);
        period.setFromDate(today.minusDays(35));
        period.setToDate(today.minusDays(6));
        period.setExpectedAmount(new BigDecimal("100"));
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(new BigDecimal("100"));
        period.setMinPaymentCriteriaMet(null);
        return period;
    }

}
