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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Covers the rule that only the first breach schedule period carries the breach grace days, on every path that dates or
 * re-dates a period.
 */
class WorkingCapitalLoanBreachScheduleEvaluationUtilsGraceDaysTest {

    private static final LocalDate FROM_DATE = LocalDate.of(2026, 1, 1);
    private static final int FREQUENCY_DAYS = 15;
    private static final int GRACE_DAYS = 5;

    @Test
    void naturalToDate_firstPeriod_carriesTheGraceDays() {
        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateNaturalToDate(FROM_DATE, 1, FREQUENCY_DAYS,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, GRACE_DAYS);

        // 15-day frequency -> [Jan 1 .. Jan 15], extended by the 5 grace days.
        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 20));
    }

    @Test
    void naturalToDate_laterPeriod_ignoresTheGraceDays() {
        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateNaturalToDate(FROM_DATE, 2, FREQUENCY_DAYS,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, GRACE_DAYS);

        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void naturalToDate_withoutGraceDaysConfigured_isTheFrequencyAlone() {
        assertThat(WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateNaturalToDate(FROM_DATE, 1, FREQUENCY_DAYS,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, null)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateNaturalToDate(FROM_DATE, 1, FREQUENCY_DAYS,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, 0)).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void rescheduledToDate_firstPeriod_keepsTheGraceDaysUnderTheNewFrequency() {
        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateRescheduledToDate(FROM_DATE, 1, 2,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, GRACE_DAYS, List.of());

        // A frequency shorter than the grace days would otherwise end the period before the grace is over.
        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 7));
    }

    @Test
    void rescheduledToDate_laterPeriod_ignoresTheGraceDays() {
        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateRescheduledToDate(FROM_DATE, 3, 2,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, GRACE_DAYS, List.of());

        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 2));
    }
}
