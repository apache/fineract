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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanNearBreachChangeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanNearBreachEvaluationService.NearBreachParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanNearBreachRederivationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate LATEST_COB_DATE = LocalDate.of(2026, 5, 31);
    private static final NearBreachParameters PARAMETERS = new NearBreachParameters(BigDecimal.valueOf(33), 3,
            WorkingCapitalLoanPeriodFrequencyType.DAYS, 0);

    @Mock
    private WorkingCapitalLoanNearBreachEvaluationService evaluationService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    private WorkingCapitalLoanNearBreachRederivation underTest;
    private WorkingCapitalLoan loan;
    private FineractPlatformTenant originalTenant;

    @BeforeEach
    void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BUSINESS_DATE, TODAY)));
        underTest = new WorkingCapitalLoanNearBreachRederivation(evaluationService, businessEventNotifierService);
        loan = new WorkingCapitalLoan();
        loan.setId(1L);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
    }

    @Test
    void rederiveOpenPeriod_ofAClosedPeriod_resolvesNothing() {
        underTest.rederiveOpenPeriod(loan, period(1L, 1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 31), true));

        verify(evaluationService, never()).resolveParametersWithBreachEvaluationEnabled(any());
        verify(evaluationService, never()).rederiveNearBreach(anyList(), any(), any());
    }

    @Test
    void rederiveOpenPeriod_changingTheValue_raisesTheEvent() {
        final WorkingCapitalLoanBreachSchedule open = period(1L, 1, LocalDate.of(2026, 5, 21), TODAY, false);
        when(evaluationService.resolveParametersWithBreachEvaluationEnabled(loan)).thenReturn(Optional.of(PARAMETERS));
        when(evaluationService.rederiveNearBreach(List.of(open), PARAMETERS, LATEST_COB_DATE)).thenReturn(true);

        underTest.rederiveOpenPeriod(loan, open);

        verify(businessEventNotifierService).notifyPostBusinessEvent(any(WorkingCapitalLoanNearBreachChangeBusinessEvent.class));
    }

    @Test
    void rederiveOpenPeriod_leavingTheValueUnchanged_raisesNoEvent() {
        final WorkingCapitalLoanBreachSchedule open = period(1L, 1, LocalDate.of(2026, 5, 21), TODAY, false);
        when(evaluationService.resolveParametersWithBreachEvaluationEnabled(loan)).thenReturn(Optional.of(PARAMETERS));
        when(evaluationService.rederiveNearBreach(List.of(open), PARAMETERS, LATEST_COB_DATE)).thenReturn(false);

        underTest.rederiveOpenPeriod(loan, open);

        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void rederiveOpenPeriod_withoutParameters_rederivesNothing() {
        final WorkingCapitalLoanBreachSchedule open = period(1L, 1, LocalDate.of(2026, 5, 21), TODAY, false);
        when(evaluationService.resolveParametersWithBreachEvaluationEnabled(loan)).thenReturn(Optional.empty());

        underTest.rederiveOpenPeriod(loan, open);

        verify(evaluationService, never()).rederiveNearBreach(anyList(), any(), any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void rederiveSince_rederivesOnlyTheStalePeriodsAndRaisesTheEventOnChange() {
        final WorkingCapitalLoanBreachSchedule settled = period(1L, 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), true);
        final WorkingCapitalLoanBreachSchedule open = period(2L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 6, 10), false);
        final WorkingCapitalLoanNearBreachBaseline baseline = WorkingCapitalLoanNearBreachBaseline.of(List.of(settled, open), TODAY);
        when(evaluationService.resolveParameters(loan)).thenReturn(Optional.of(PARAMETERS));
        when(evaluationService.rederiveNearBreach(List.of(open), PARAMETERS, LATEST_COB_DATE)).thenAnswer(invocation -> {
            open.setNearBreach(true);
            return true;
        });

        underTest.rederiveSince(loan, List.of(settled, open), baseline, false);

        verify(evaluationService).rederiveNearBreach(List.of(open), PARAMETERS, LATEST_COB_DATE);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(WorkingCapitalLoanNearBreachChangeBusinessEvent.class));
    }

    @Test
    void rederiveSince_withClosedValuesRederived_includesTheSettledPeriod_andRaisesNoEventWhenNothingChanged() {
        final WorkingCapitalLoanBreachSchedule settled = period(1L, 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), true);
        final WorkingCapitalLoanBreachSchedule open = period(2L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 6, 10), false);
        final WorkingCapitalLoanNearBreachBaseline baseline = WorkingCapitalLoanNearBreachBaseline.of(List.of(settled, open), TODAY);
        when(evaluationService.resolveParameters(loan)).thenReturn(Optional.of(PARAMETERS));

        underTest.rederiveSince(loan, List.of(settled, open), baseline, true);

        verify(evaluationService).rederiveNearBreach(List.of(settled, open), PARAMETERS, LATEST_COB_DATE);
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void rederiveSince_withoutParameters_stillCarriesTheValueOverToARegeneratedPeriod_andRederivesNothing() {
        final WorkingCapitalLoanBreachSchedule deleted = period(1L, 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), true);
        final WorkingCapitalLoanNearBreachBaseline baseline = WorkingCapitalLoanNearBreachBaseline.of(List.of(deleted), TODAY);
        final WorkingCapitalLoanBreachSchedule regenerated = period(9L, 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), null);
        when(evaluationService.resolveParameters(loan)).thenReturn(Optional.empty());

        underTest.rederiveSince(loan, List.of(regenerated), baseline, true);

        assertEquals(Boolean.TRUE, regenerated.getNearBreach());
        verify(evaluationService, never()).rederiveNearBreach(anyList(), any(), any());
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    private WorkingCapitalLoanBreachSchedule period(final Long id, final int periodNumber, final LocalDate fromDate, final LocalDate toDate,
            final Boolean nearBreach) {
        final WorkingCapitalLoanBreachSchedule period = new WorkingCapitalLoanBreachSchedule();
        period.setId(id);
        period.setLoan(loan);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setMinPaymentAmount(BigDecimal.valueOf(100));
        period.setPaidAmount(BigDecimal.ZERO);
        period.setNearBreach(nearBreach);
        return period;
    }
}
