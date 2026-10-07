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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanNearBreachEvaluationService.NearBreachParameters;
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
    private static final NearBreachParameters PARAMETERS = new NearBreachParameters(BigDecimal.valueOf(33), 3,
            WorkingCapitalLoanPeriodFrequencyType.DAYS, 0);

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;

    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;

    @Mock
    private WorkingCapitalLoanNearBreachActionRepository nearBreachActionRepository;

    private WorkingCapitalLoanNearBreachEvaluationServiceImpl underTest;

    private FineractPlatformTenant originalTenant;

    @BeforeEach
    public void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1))));
        MoneyHelper.initializeTenantRoundingMode("default", RoundingMode.HALF_UP.ordinal());
        underTest = new WorkingCapitalLoanNearBreachEvaluationServiceImpl(breachScheduleRepository, breachActionRepository,
                nearBreachActionRepository);
        lenient().when(breachActionRepository.isBreachDisabled(anyLong())).thenReturn(false);
        lenient().when(nearBreachActionRepository.findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(anyLong(), any()))
                .thenReturn(Optional.empty());
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

        assertFalse(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 2)));
        assertNull(period.getNearBreach());
        verify(breachScheduleRepository, never()).saveAll(any());

        assertTrue(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 3)));
        assertEquals(Boolean.TRUE, period.getNearBreach());
        verify(breachScheduleRepository).saveAll(List.of(period));

        assertFalse(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 4)));
        verify(breachScheduleRepository, times(1)).saveAll(any());
    }

    @Test
    public void shiftsEvaluationDateByBreachGraceDays() {
        final WorkingCapitalLoan loan = loanWithNearBreach(3);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12));
        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any()))
                .thenReturn(Optional.of(period));

        assertFalse(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 5)));
        assertNull(period.getNearBreach());

        assertTrue(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 6)));
        assertEquals(Boolean.TRUE, period.getNearBreach());
        verify(breachScheduleRepository).saveAll(List.of(period));
    }

    @Test
    public void cobEvaluationWithoutAPeriodCoveringTheDateChangesNothing() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        when(breachScheduleRepository.findByLoanIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        assertFalse(underTest.evaluateNearBreachOnCob(loan, LocalDate.of(2026, 1, 6)));
        verify(breachScheduleRepository, never()).saveAll(any());
    }

    @Test
    public void resolvesNullBeforeTheFirstCheckpointAndPointInTimeAfterIt() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        period.setPaidAmount(BigDecimal.valueOf(140));

        assertEquals(Optional.empty(), resolve(period, LocalDate.of(2026, 1, 2)));
        assertEquals(Optional.of(false), resolve(period, LocalDate.of(2026, 1, 3)));
        assertEquals(Optional.of(true), resolve(period, LocalDate.of(2026, 1, 6)));
        assertEquals(Optional.of(true), resolve(period, LocalDate.of(2026, 1, 9)));
    }

    @Test
    public void resolvesTheCloseOutValueWhenNoCheckpointFallsBeforeThePeriodEnd() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        assertEquals(Optional.empty(), resolve(period, LocalDate.of(2026, 1, 2)));
        assertEquals(Optional.of(false), resolve(period, LocalDate.of(2026, 1, 3)));
    }

    @Test
    public void resolvesNullWithoutDemandOrWithoutACheckpointInsideThePeriod() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule withoutDemand = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        withoutDemand.setMinPaymentAmount(BigDecimal.ZERO);
        final WorkingCapitalLoanBreachSchedule tooShort = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));

        assertEquals(Optional.empty(), resolve(withoutDemand, LocalDate.of(2026, 1, 9)));
        assertEquals(Optional.empty(), resolve(tooShort, LocalDate.of(2026, 1, 2)));
    }

    @Test
    public void rederivationClearsAndReraisesAsOfTheEffectiveDate() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        period.setNearBreach(true);
        period.setPaidAmount(BigDecimal.valueOf(132));

        assertTrue(underTest.rederiveNearBreach(List.of(period), PARAMETERS, LocalDate.of(2026, 1, 3)));
        assertEquals(Boolean.FALSE, period.getNearBreach());

        period.setPaidAmount(BigDecimal.valueOf(131));
        assertTrue(underTest.rederiveNearBreach(List.of(period), PARAMETERS, LocalDate.of(2026, 1, 3)));
        assertEquals(Boolean.TRUE, period.getNearBreach());
        verify(breachScheduleRepository, times(2)).saveAll(List.of(period));
    }

    @Test
    public void rederivationKeepsTheValueWhenThereIsNothingToJudgeAgainst() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule withoutDemand = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        withoutDemand.setMinPaymentAmount(BigDecimal.ZERO);
        withoutDemand.setNearBreach(true);
        final WorkingCapitalLoanBreachSchedule withoutCheckpoint = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
        withoutCheckpoint.setNearBreach(false);
        final WorkingCapitalLoanBreachSchedule noneElapsed = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        noneElapsed.setNearBreach(true);

        assertFalse(underTest.rederiveNearBreach(List.of(withoutDemand, withoutCheckpoint), PARAMETERS, LocalDate.of(2026, 1, 9)));
        assertFalse(underTest.rederiveNearBreach(List.of(noneElapsed), PARAMETERS, LocalDate.of(2026, 1, 2)));

        assertEquals(Boolean.TRUE, withoutDemand.getNearBreach());
        assertEquals(Boolean.FALSE, withoutCheckpoint.getNearBreach());
        assertEquals(Boolean.TRUE, noneElapsed.getNearBreach());
        verify(breachScheduleRepository, never()).saveAll(any());
    }

    @Test
    public void rederivationLeavingTheValueUnchangedReportsNoChange() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        final WorkingCapitalLoanBreachSchedule period = period(loan, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 9));
        period.setNearBreach(true);

        assertFalse(underTest.rederiveNearBreach(List.of(period), PARAMETERS, LocalDate.of(2026, 1, 3)));
        assertEquals(Boolean.TRUE, period.getNearBreach());
        verify(breachScheduleRepository, never()).saveAll(any());
    }

    @Test
    public void resolvesTheProductParametersOfAnActiveLoan() {
        final WorkingCapitalLoan loan = loanWithNearBreach(2);

        assertEquals(Optional.of(new NearBreachParameters(BigDecimal.valueOf(33), 3, WorkingCapitalLoanPeriodFrequencyType.DAYS, 2)),
                underTest.resolveParameters(loan));
        verify(breachActionRepository, times(1)).isBreachDisabled(LOAN_ID);
    }

    @Test
    public void resolvesNoParametersWithoutANearBreachConfiguration() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        loan.getLoanProductRelatedDetails().setNearBreach(null);

        assertEquals(Optional.empty(), underTest.resolveParameters(loan));
        verify(breachActionRepository, never()).isBreachDisabled(anyLong());
    }

    @Test
    public void resolvesNoParametersWhileBreachEvaluationIsDisabled() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        when(breachActionRepository.isBreachDisabled(LOAN_ID)).thenReturn(true);

        assertEquals(Optional.empty(), underTest.resolveParameters(loan));
    }

    @Test
    public void resolvesParametersForACallerThatEstablishedBreachEvaluationIsEnabledWithoutCheckingItAgain() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);

        assertEquals(Optional.of(PARAMETERS), underTest.resolveParametersWithBreachEvaluationEnabled(loan));
        verify(breachActionRepository, never()).isBreachDisabled(anyLong());
    }

    @Test
    public void resolvesNoParametersForALoanThatIsNotActive() {
        final WorkingCapitalLoan loan = loanWithNearBreach(0);
        loan.setLoanStatus(LoanStatus.CLOSED_OBLIGATIONS_MET);

        assertEquals(Optional.empty(), underTest.resolveParameters(loan));
        assertEquals(Optional.empty(), underTest.resolveParametersWithBreachEvaluationEnabled(loan));
        verify(nearBreachActionRepository, never()).findTopByWorkingCapitalLoanIdAndActionOrderByIdDesc(anyLong(), any());
    }

    private Optional<Boolean> resolve(final WorkingCapitalLoanBreachSchedule period, final LocalDate effectiveDate) {
        return underTest.resolveNearBreachValue(period, PARAMETERS, effectiveDate);
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
        loan.setLoanStatus(LoanStatus.ACTIVE);
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
