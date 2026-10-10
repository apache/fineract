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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanNearBreachBaselineTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 6, 1);

    @Test
    void theOpenPeriodIsStaleWhetherItMovedOrNot() {
        final WorkingCapitalLoanBreachSchedule open = period(1L, 1, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 3), 0, true);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(open);

        assertTrue(baseline.isStale(open, BUSINESS_DATE, false));

        open.setToDate(LocalDate.of(2026, 6, 5));
        assertTrue(baseline.isStale(open, BUSINESS_DATE, false));
    }

    @Test
    void aClosedPeriodThatDidNotMoveIsStaleOnlyWhenTheClosedValuesAreRederived() {
        final WorkingCapitalLoanBreachSchedule closed = period(1L, 1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 50, true);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(closed);

        assertFalse(baseline.isStale(closed, BUSINESS_DATE, false));
        assertTrue(baseline.isStale(closed, BUSINESS_DATE, true));
    }

    @Test
    void aClosedPeriodWhoseBoundsDemandOrPaidAmountMovedIsStale() {
        final WorkingCapitalLoanBreachSchedule redated = period(1L, 1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 50, true);
        final WorkingCapitalLoanBreachSchedule redemanded = period(2L, 2, LocalDate.of(2026, 5, 21), LocalDate.of(2026, 5, 30), 50, true);
        final WorkingCapitalLoanBreachSchedule repaid = period(3L, 3, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10), 50, false);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(redated, redemanded, repaid);

        redated.setToDate(LocalDate.of(2026, 5, 22));
        redemanded.setMinPaymentAmount(BigDecimal.valueOf(80));
        repaid.setPaidAmount(BigDecimal.valueOf(70));

        assertTrue(baseline.isStale(redated, BUSINESS_DATE, false));
        assertTrue(baseline.isStale(redemanded, BUSINESS_DATE, false));
        assertTrue(baseline.isStale(repaid, BUSINESS_DATE, false));
    }

    @Test
    void aClosedPeriodMissingFromTheBaselineIsStaleWhenItCarriesAValue() {
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf();

        assertTrue(baseline.isStale(period(5L, 5, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 0, true), BUSINESS_DATE, false));
        assertFalse(baseline.isStale(period(6L, 6, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 0, null), BUSINESS_DATE, true));
    }

    @Test
    void aClosedPeriodNeverEvaluatedStaysUnevaluatedUnlessItIsTheBaselineOpenPeriodClosingOut() {
        final WorkingCapitalLoanBreachSchedule neverEvaluated = period(1L, 1, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), 0, null);
        final WorkingCapitalLoanBreachSchedule closingOut = period(2L, 2, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 5), 0, null);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(neverEvaluated, closingOut);

        closingOut.setToDate(LocalDate.of(2026, 5, 31));

        assertFalse(baseline.isStale(neverEvaluated, BUSINESS_DATE, true));
        assertTrue(baseline.isStale(closingOut, BUSINESS_DATE, false));
    }

    @Test
    void aPeriodRegeneratedWithTheSameNumberDatesDemandAndPaidAmountTakesOverTheValue() {
        final WorkingCapitalLoanBreachSchedule deleted = period(1L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 30, true);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(deleted);
        final WorkingCapitalLoanBreachSchedule regenerated = period(99L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 30, null);

        baseline.carryOverToRegenerated(regenerated);

        assertEquals(Boolean.TRUE, regenerated.getNearBreach());
        assertFalse(baseline.hasChanged(List.of(regenerated), BUSINESS_DATE));
    }

    @Test
    void aRegeneratedPeriodWithOtherDatesDoesNotTakeOverTheValue() {
        final WorkingCapitalLoanBreachSchedule deleted = period(1L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 30, true);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(deleted);
        final WorkingCapitalLoanBreachSchedule regenerated = period(99L, 2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 18), 30, null);

        baseline.carryOverToRegenerated(regenerated);

        assertNull(regenerated.getNearBreach());
        assertTrue(baseline.hasChanged(List.of(regenerated), BUSINESS_DATE));
    }

    @Test
    void aValueGoingFromNullToAValueIsAChange() {
        final WorkingCapitalLoanBreachSchedule open = period(1L, 1, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 3), 0, null);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(open);

        open.setNearBreach(false);

        assertTrue(baseline.hasChanged(List.of(open), BUSINESS_DATE));
    }

    @Test
    void anUnchangedValueIsNoChange() {
        final WorkingCapitalLoanBreachSchedule closed = period(1L, 1, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 20), 0, true);
        final WorkingCapitalLoanBreachSchedule open = period(2L, 2, LocalDate.of(2026, 5, 21), LocalDate.of(2026, 6, 3), 0, false);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(closed, open);

        open.setPaidAmount(BigDecimal.valueOf(10));

        assertFalse(baseline.hasChanged(List.of(closed, open), BUSINESS_DATE));
    }

    @Test
    void aDeletedBaselineOpenPeriodIsComparedWithThePeriodOpenNow() {
        final WorkingCapitalLoanBreachSchedule split = period(1L, 1, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 28), 0, true);
        final WorkingCapitalLoanBreachSchedule deletedOpen = period(2L, 2, LocalDate.of(2026, 5, 29), LocalDate.of(2026, 6, 7), 0, false);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(split, deletedOpen);

        split.setToDate(LocalDate.of(2026, 6, 3));
        assertTrue(baseline.hasChanged(List.of(split), BUSINESS_DATE));

        split.setNearBreach(false);
        assertTrue(baseline.hasChanged(List.of(split), BUSINESS_DATE));

    }

    @Test
    void aDeletedBaselineOpenPeriodWhoseValueThePeriodOpenNowKeepsIsNoChange() {
        final WorkingCapitalLoanBreachSchedule split = period(1L, 1, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 28), 0, true);
        final WorkingCapitalLoanBreachSchedule deletedOpen = period(2L, 2, LocalDate.of(2026, 5, 29), LocalDate.of(2026, 6, 7), 0, true);
        final WorkingCapitalLoanNearBreachBaseline baseline = baselineOf(split, deletedOpen);

        split.setToDate(LocalDate.of(2026, 6, 3));

        assertFalse(baseline.hasChanged(List.of(split), BUSINESS_DATE));
    }

    private static WorkingCapitalLoanNearBreachBaseline baselineOf(final WorkingCapitalLoanBreachSchedule... periods) {
        return WorkingCapitalLoanNearBreachBaseline.of(List.of(periods), BUSINESS_DATE);
    }

    private static WorkingCapitalLoanBreachSchedule period(final Long id, final int periodNumber, final LocalDate fromDate,
            final LocalDate toDate, final int paidAmount, final Boolean nearBreach) {
        final WorkingCapitalLoanBreachSchedule period = new WorkingCapitalLoanBreachSchedule();
        period.setId(id);
        period.setPeriodNumber(periodNumber);
        period.setFromDate(fromDate);
        period.setToDate(toDate);
        period.setMinPaymentAmount(BigDecimal.valueOf(100));
        period.setPaidAmount(BigDecimal.valueOf(paidAmount));
        period.setNearBreach(nearBreach);
        return period;
    }
}
