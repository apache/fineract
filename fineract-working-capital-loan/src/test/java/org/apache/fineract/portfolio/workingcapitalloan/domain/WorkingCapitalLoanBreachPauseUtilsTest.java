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
 * Covers how the pauses reshape a breach period, and that a period restarted by a reset inside a pause keeps its start.
 */
class WorkingCapitalLoanBreachPauseUtilsTest {

    private static final LocalDate RESET_DATE = LocalDate.of(2026, 1, 12);
    private static final WorkingCapitalLoanPausePeriod PAUSE_11_TO_13 = new WorkingCapitalLoanPausePeriod(LocalDate.of(2026, 1, 11),
            LocalDate.of(2026, 1, 13));

    @Test
    void applyPauses_periodStartingInsidePauseWithoutReset_movesPastThePause() {
        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanBreachPauseUtils.applyPauses(RESET_DATE, LocalDate.of(2026, 1, 17),
                List.of(PAUSE_11_TO_13), List.of());

        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 20)));
    }

    @Test
    void applyPauses_periodRestartedInsidePause_keepsItsStartAndCountsThePauseFromTheReset() {
        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanBreachPauseUtils.applyPauses(RESET_DATE, LocalDate.of(2026, 1, 17),
                List.of(PAUSE_11_TO_13), List.of(RESET_DATE));

        // 6 days from the reset, extended by the pause days 12 and 13 Jan only
        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(RESET_DATE, LocalDate.of(2026, 1, 19)));
    }

    @Test
    void applyPauses_periodRestartedInsidePause_dropsPausesThatEndedBeforeTheReset() {
        final WorkingCapitalLoanPausePeriod earlierPause = new WorkingCapitalLoanPausePeriod(LocalDate.of(2026, 1, 3),
                LocalDate.of(2026, 1, 4));
        final WorkingCapitalLoanPausePeriod laterPause = new WorkingCapitalLoanPausePeriod(LocalDate.of(2026, 1, 14),
                LocalDate.of(2026, 1, 14));

        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanBreachPauseUtils.applyPauses(RESET_DATE, LocalDate.of(2026, 1, 17),
                List.of(earlierPause, PAUSE_11_TO_13, laterPause), List.of(RESET_DATE));

        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(RESET_DATE, LocalDate.of(2026, 1, 20)));
    }

    @Test
    void rescheduledToDate_periodRestartedInsidePause_agreesWithTheReDate() {
        final WorkingCapitalLoanBreachAction pause = new WorkingCapitalLoanBreachAction();
        pause.setAction(WorkingCapitalLoanBreachActionType.PAUSE);
        pause.setStartDate(PAUSE_11_TO_13.startDate());
        pause.setEndDate(PAUSE_11_TO_13.endDate());

        final LocalDate toDate = WorkingCapitalLoanBreachScheduleEvaluationUtils.calculateRescheduledToDate(RESET_DATE, 3, 2,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, null, List.of(pause), List.of(RESET_DATE));

        // a 2-day frequency ends the restarted period on 13 Jan, extended by the pause days 12 and 13 Jan
        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 15));
    }
}
