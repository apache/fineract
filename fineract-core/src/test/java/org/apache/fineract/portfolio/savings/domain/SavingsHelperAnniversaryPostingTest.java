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
package org.apache.fineract.portfolio.savings.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.junit.jupiter.api.Test;

class SavingsHelperAnniversaryPostingTest {

    private static final Integer FINANCIAL_YEAR_BEGINNING_MONTH = 1;

    private static void assertPeriod(LocalDateInterval period, LocalDate expectedStart, LocalDate expectedEnd) {
        assertThat(period.startDate()).as("period start").isEqualTo(expectedStart);
        assertThat(period.endDate()).as("period end").isEqualTo(expectedEnd);
    }

    // ── ANNIVERSARY_MONTHLY ──────────────────────────────────────────────────

    @Test
    void anniversaryMonthly_accountOpenedOn15th_periodsAlignToThe15th() {
        // Account opened Jan 15 → posts on Feb 15, Mar 15, Apr 15, …
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 3, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_MONTHLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        assertPeriod(periods.get(0), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 14));
        assertPeriod(periods.get(1), LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 14));
        // last period extends past upToDate — truncation is handled by PostingPeriod, not here
        assertPeriod(periods.get(2), LocalDate.of(2024, 3, 15), LocalDate.of(2024, 4, 14));
    }

    @Test
    void anniversaryMonthly_accountOpenedOn1st_periodsAlignToFirstOfMonth() {
        // Day-1 anniversary: behaves identically to standard monthly-from-start
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_MONTHLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        assertPeriod(periods.get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertPeriod(periods.get(1), LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)); // 2024 leap year
        assertPeriod(periods.get(2), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));
    }

    @Test
    void anniversaryMonthly_accountOpenedOn29th_februaryUsesLastDay() {
        // Feb 2025 has 28 days → posting falls on Feb 28 (last day), not Feb 29
        LocalDate start = LocalDate.of(2025, 1, 29);
        LocalDate end = LocalDate.of(2025, 4, 30);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_MONTHLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(4);
        // Jan 29 → Feb 28 posting: period Jan 29 – Feb 27
        assertPeriod(periods.get(0), LocalDate.of(2025, 1, 29), LocalDate.of(2025, 2, 27));
        // Feb 28 → Mar 29 posting: period Feb 28 – Mar 28
        assertPeriod(periods.get(1), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 28));
        // Mar 29 → Apr 29 posting: period Mar 29 – Apr 28
        assertPeriod(periods.get(2), LocalDate.of(2025, 3, 29), LocalDate.of(2025, 4, 28));
        // Apr 29 → May 29 posting: period Apr 29 – May 28 (extends past upToDate Apr 30)
        assertPeriod(periods.get(3), LocalDate.of(2025, 4, 29), LocalDate.of(2025, 5, 28));
    }

    @Test
    void anniversaryMonthly_accountOpenedOn31st_shortMonthsUseLastDay() {
        // Jan 31 → Feb 28 (28 days), then Mar 31, Apr 30, May 31, Jun 30, …
        LocalDate start = LocalDate.of(2025, 1, 31);
        LocalDate end = LocalDate.of(2025, 5, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_MONTHLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(5);
        assertPeriod(periods.get(0), LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 27)); // Feb 28 - 1
        assertPeriod(periods.get(1), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 30)); // Mar 31 - 1
        assertPeriod(periods.get(2), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 29)); // Apr 30 - 1
        assertPeriod(periods.get(3), LocalDate.of(2025, 4, 30), LocalDate.of(2025, 5, 30)); // May 31 - 1
        // last period extends past upToDate May 31 → May 31 – Jun 29
        assertPeriod(periods.get(4), LocalDate.of(2025, 5, 31), LocalDate.of(2025, 6, 29)); // Jun 30 - 1
    }

    // ── ANNIVERSARY_QUARTERLY ────────────────────────────────────────────────

    @Test
    void anniversaryQuarterly_accountOpenedOn15th_periodsAlignToThe15th() {
        // Account opened Jan 15 → posts every 3 months on the 15th
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 12, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_QUARTERLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(4);
        assertPeriod(periods.get(0), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 4, 14));
        assertPeriod(periods.get(1), LocalDate.of(2024, 4, 15), LocalDate.of(2024, 7, 14));
        assertPeriod(periods.get(2), LocalDate.of(2024, 7, 15), LocalDate.of(2024, 10, 14));
        // last period extends past Dec 31 → Oct 15 – Jan 14 2025
        assertPeriod(periods.get(3), LocalDate.of(2024, 10, 15), LocalDate.of(2025, 1, 14));
    }

    @Test
    void anniversaryQuarterly_accountOpenedOn29th_februaryQuarterUsesLastDay() {
        // Nov 29 + 3 months = Feb 28 (Feb 2025 has 28 days)
        LocalDate start = LocalDate.of(2024, 11, 29);
        LocalDate end = LocalDate.of(2025, 5, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_QUARTERLY, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        // Nov 29 → Feb 28 posting: period Nov 29 – Feb 27
        assertPeriod(periods.get(0), LocalDate.of(2024, 11, 29), LocalDate.of(2025, 2, 27));
        // Feb 28 → May 29 posting: period Feb 28 – May 28
        assertPeriod(periods.get(1), LocalDate.of(2025, 2, 28), LocalDate.of(2025, 5, 28));
        // May 29 → Aug 29 posting: period May 29 – Aug 28 (extends past upToDate May 31)
        assertPeriod(periods.get(2), LocalDate.of(2025, 5, 29), LocalDate.of(2025, 8, 28));
    }

    // ── ANNIVERSARY_BIANNUAL ─────────────────────────────────────────────────

    @Test
    void anniversaryBiAnnual_accountOpenedOn15th_periodsAlignToThe15th() {
        // Account opened Jan 15 → posts every 6 months on the 15th
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2025, 1, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_BIANNUAL, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        assertPeriod(periods.get(0), LocalDate.of(2024, 1, 15), LocalDate.of(2024, 7, 14));
        assertPeriod(periods.get(1), LocalDate.of(2024, 7, 15), LocalDate.of(2025, 1, 14));
        // last period extends past Jan 31 → Jan 15 2025 – Jul 14 2025
        assertPeriod(periods.get(2), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 7, 14));
    }

    // ── ANNIVERSARY_ANNUAL ───────────────────────────────────────────────────

    @Test
    void anniversaryAnnual_accountOpenedOn15th_periodsAlignToThe15th() {
        // Account opened Mar 15 → posts annually on Mar 15
        LocalDate start = LocalDate.of(2024, 3, 15);
        LocalDate end = LocalDate.of(2026, 3, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_ANNUAL, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        assertPeriod(periods.get(0), LocalDate.of(2024, 3, 15), LocalDate.of(2025, 3, 14));
        assertPeriod(periods.get(1), LocalDate.of(2025, 3, 15), LocalDate.of(2026, 3, 14));
        // last period extends past Mar 31 2026 → Mar 15 2026 – Mar 14 2027
        assertPeriod(periods.get(2), LocalDate.of(2026, 3, 15), LocalDate.of(2027, 3, 14));
    }

    @Test
    void anniversaryAnnual_accountOpenedOnFeb29_leapYearHandling() {
        // Feb 29 2024 (leap) → next year Feb has 28 days → post on Feb 28
        LocalDate start = LocalDate.of(2024, 2, 29);
        LocalDate end = LocalDate.of(2026, 3, 31);

        List<LocalDateInterval> periods = SavingsInterestCalculationUtil.determineInterestPostingPeriods(start, end,
                SavingsPostingInterestPeriodType.ANNIVERSARY_ANNUAL, FINANCIAL_YEAR_BEGINNING_MONTH, Collections.emptyList());

        assertThat(periods).hasSize(3);
        // Feb 29 2024 → Feb 28 2025 posting: period Feb 29 2024 – Feb 27 2025
        assertPeriod(periods.get(0), LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 27));
        // Feb 28 2025 → Feb 28 2026 posting: period Feb 28 2025 – Feb 27 2026
        assertPeriod(periods.get(1), LocalDate.of(2025, 2, 28), LocalDate.of(2026, 2, 27));
        // last period extends past Mar 31 2026 → Feb 28 2026 – Feb 27 2027
        assertPeriod(periods.get(2), LocalDate.of(2026, 2, 28), LocalDate.of(2027, 2, 27));
    }
}
