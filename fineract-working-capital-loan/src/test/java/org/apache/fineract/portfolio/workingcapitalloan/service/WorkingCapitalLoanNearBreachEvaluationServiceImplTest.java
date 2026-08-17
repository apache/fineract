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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloannearbreach.domain.WorkingCapitalNearBreach;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WorkingCapitalLoanNearBreachEvaluationServiceImplTest {

    private static final Long LOAN_ID = 1L;

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;

    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;

    private WorkingCapitalLoanNearBreachEvaluationServiceImpl underTest;

    private FineractPlatformTenant originalTenant;

    @BeforeEach
    public void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1))));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        underTest = new WorkingCapitalLoanNearBreachEvaluationServiceImpl(breachScheduleRepository, breachActionRepository);
        when(breachActionRepository.isBreachDisabledAsOf(anyLong(), any())).thenReturn(false);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
        MoneyHelper.clearCacheForTenant("default");
    }

    @Test
    public void evaluatesAtTheEndOfTheFirstFrequencyWindowWithoutGraceDays() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any()))
                .thenReturn(Optional.of(period));

        underTest.evaluateNearBreach(loan, null, LocalDate.of(2026, 1, 2));
        assertNull(period.getNearBreach());
        verify(breachScheduleRepository, never()).saveAndFlush(any());

        underTest.evaluateNearBreach(loan, null, LocalDate.of(2026, 1, 3));
        assertTrue(period.getNearBreach());
        verify(breachScheduleRepository).saveAndFlush(period);
    }

    @Test
    public void shiftsEvaluationDateByBreachGraceDays() {
        final WorkingCapitalLoan loan = loanWithNearBreach(3);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12));
        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any()))
                .thenReturn(Optional.of(period));

        underTest.evaluateNearBreach(loan, null, LocalDate.of(2026, 1, 5));
        assertNull(period.getNearBreach());

        underTest.evaluateNearBreach(loan, null, LocalDate.of(2026, 1, 6));
        assertTrue(period.getNearBreach());
        verify(breachScheduleRepository).saveAndFlush(period);
    }

    private WorkingCapitalLoan loanWithNearBreach(final int breachGraceDays) {
        final WorkingCapitalNearBreach nearBreach = new WorkingCapitalNearBreach("near breach", 3,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, BigDecimal.valueOf(33));
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setCurrency(new MonetaryCurrency("EUR", 2, null));
        details.setNearBreach(nearBreach);
        details.setBreachGraceDays(breachGraceDays);

        final WorkingCapitalLoan loan = new WorkingCapitalLoan();
        loan.setId(LOAN_ID);
        loan.setLoanProductRelatedDetails(details);
        return loan;
    }

    private WorkingCapitalLoanBreachSchedule period(final WorkingCapitalLoan loan, final LocalDate fromDate, final LocalDate toDate) {
        final WorkingCapitalLoanBreachSchedule period = new WorkingCapitalLoanBreachSchedule();
        period.setLoan(loan);
        period.setPeriodNumber(1);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setMinPaymentAmount(BigDecimal.valueOf(400));
        period.setPaidAmount(BigDecimal.ZERO);
        period.setOutstandingAmount(BigDecimal.valueOf(400));
        return period;
    }
}
