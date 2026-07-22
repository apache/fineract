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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.data.TransactionDateAndAmountHolder;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanDelinquencyRangeScheduleMapper;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanDelinquencyRangeScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
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
 * Verifies that the reschedule/backdated-repayment reprocess replays transactions without classifying delinquency on
 * the synthetic per-period dates (transactions are remapped to the period toDate, which can lie in the future).
 * Classification must happen exactly once, at the end, with the real business date - otherwise an active tag gets
 * lifted with the (possibly future) period toDate instead of the business date.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanDelinquencyReprocessClassificationTest {

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
    private WorkingCapitalLoanTransactionRepository transactionRepository;
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
    void reprocessReplaysTransactionsWithoutClassifyingOnSyntheticDates() {
        // A cured past period and the current period, whose toDate lies in the future: the replay remaps the
        // backdated transaction to the past toDate and the recent one to the FUTURE toDate.
        final WorkingCapitalLoanDelinquencyRangeSchedule pastPeriod = period(1, today.minusDays(45), today.minusDays(16));
        final WorkingCapitalLoanDelinquencyRangeSchedule currentPeriod = period(2, today.minusDays(15), today.plusDays(14));
        when(rangeScheduleRepository.findByLoanIdOrderByPeriodNumberAsc(LOAN_ID)).thenReturn(List.of(pastPeriod, currentPeriod));
        when(transactionRepository.fetchTransactionDateAndAmount(LOAN_ID, LoanTransactionType.getRepaymentLikeTransactionTypes()))
                .thenReturn(List.of(new TransactionDateAndAmountHolder(today.minusDays(20), new BigDecimal("3")),
                        new TransactionDateAndAmountHolder(today.minusDays(1), BigDecimal.ONE)));

        rangeScheduleService.reprocessDelinquencySchedule(loan);

        // The replay must not classify on the synthetic per-period dates (would lift tags with a wrong/future date).
        verify(classificationService, never()).instantClassifyDelinquency(any(), any());
        // Classification happens once, at the end, with the real business date.
        verify(classificationService).classifyDelinquency(loan, today);
    }

    private WorkingCapitalLoanDelinquencyRangeSchedule period(final int periodNumber, final LocalDate fromDate, final LocalDate toDate) {
        final WorkingCapitalLoanDelinquencyRangeSchedule period = new WorkingCapitalLoanDelinquencyRangeSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setExpectedAmount(new BigDecimal("3"));
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(new BigDecimal("3"));
        period.setMinPaymentCriteriaMet(null);
        return period;
    }

}
