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
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyFrequencyType;
import org.junit.jupiter.api.Test;

/**
 * Covers how the recorded pauses reshape a delinquency period, and that a period started by a reset inside an active
 * pause keeps its start on the reset date, as the breach schedule does.
 */
class WorkingCapitalLoanDelinquencyPauseUtilsTest {

    private static final LocalDate RESET_DATE = LocalDate.of(2026, 1, 12);
    private static final LocalDate PAUSE_START = LocalDate.of(2026, 1, 11);
    private static final LocalDate PAUSE_END = LocalDate.of(2026, 1, 13);

    @Test
    void restartResetDates_keepsEveryResetThatStartedANewPeriodEvenWhenUndone() {
        // the undo leaves the cut in place, so an undone restart reset is still a boundary
        final WorkingCapitalLoanDelinquencyAction undoneRestart = reset(LocalDate.of(2026, 1, 5), true);
        undoneRestart.setEndDate(LocalDate.of(2026, 1, 6));
        final WorkingCapitalLoanDelinquencyAction flagOnlyReset = reset(LocalDate.of(2026, 1, 8), false);

        final List<LocalDate> dates = WorkingCapitalLoanDelinquencyPauseUtils
                .restartResetDates(List.of(pause(), undoneRestart, flagOnlyReset, reset(RESET_DATE, true)));

        assertThat(dates).containsExactly(LocalDate.of(2026, 1, 5), RESET_DATE);
    }

    @Test
    void rescheduledToDate_periodRestartedInsidePauseByAnUndoneReset_stillCountsThePauseFromTheReset() {
        final WorkingCapitalLoanDelinquencyAction undoneReset = reset(RESET_DATE, true);
        undoneReset.setEndDate(RESET_DATE.plusDays(1));

        final LocalDate toDate = WorkingCapitalLoanDelinquencyRangeScheduleEvaluationUtils.calculateRescheduledToDate(RESET_DATE, 3, 6,
                DelinquencyFrequencyType.DAYS, null, List.of(pause(), undoneReset));

        // 6 days from the reset end on 17 Jan, extended by the pause days 12 and 13 Jan only, not 11 Jan
        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 19));
    }

    @Test
    void applyRecordedPauses_periodStartingInsidePauseWithoutReset_movesPastThePause() {
        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanDelinquencyPauseUtils.applyRecordedPauses(RESET_DATE,
                LocalDate.of(2026, 1, 17), List.of(pause()));

        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 20)));
    }

    @Test
    void applyRecordedPauses_periodRestartedInsidePause_keepsItsStartAndCountsThePauseFromTheReset() {
        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanDelinquencyPauseUtils.applyRecordedPauses(RESET_DATE,
                LocalDate.of(2026, 1, 17), List.of(pause(), reset(RESET_DATE, true)));

        // 6 days from the reset, extended by the pause days 12 and 13 Jan only
        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(RESET_DATE, LocalDate.of(2026, 1, 19)));
    }

    @Test
    void applyRecordedPauses_periodRestartedInsidePause_countsAResumedPauseUpToTheResumeDate() {
        final WorkingCapitalLoanDelinquencyAction longPause = pause();
        longPause.setEndDate(LocalDate.of(2026, 1, 15));
        final WorkingCapitalLoanDelinquencyAction resume = new WorkingCapitalLoanDelinquencyAction();
        resume.setAction(DelinquencyAction.RESUME);
        resume.setStartDate(LocalDate.of(2026, 1, 13));

        final WorkingCapitalLoanPeriodBounds bounds = WorkingCapitalLoanDelinquencyPauseUtils.applyRecordedPauses(RESET_DATE,
                LocalDate.of(2026, 1, 17), List.of(longPause, reset(RESET_DATE, true), resume));

        assertThat(bounds).isEqualTo(new WorkingCapitalLoanPeriodBounds(RESET_DATE, LocalDate.of(2026, 1, 19)));
    }

    @Test
    void rescheduledToDate_periodRestartedInsidePause_agreesWithTheReDate() {
        final LocalDate toDate = WorkingCapitalLoanDelinquencyRangeScheduleEvaluationUtils.calculateRescheduledToDate(RESET_DATE, 3, 2,
                DelinquencyFrequencyType.DAYS, null, List.of(pause(), reset(RESET_DATE, true)));

        // a 2-day frequency ends the restarted period on 13 Jan, extended by the pause days 12 and 13 Jan
        assertThat(toDate).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    private static WorkingCapitalLoanDelinquencyAction pause() {
        final WorkingCapitalLoanDelinquencyAction pause = new WorkingCapitalLoanDelinquencyAction();
        pause.setAction(DelinquencyAction.PAUSE);
        pause.setStartDate(PAUSE_START);
        pause.setEndDate(PAUSE_END);
        return pause;
    }

    private static WorkingCapitalLoanDelinquencyAction reset(final LocalDate resetDate, final boolean startNewPeriod) {
        final WorkingCapitalLoanDelinquencyAction reset = new WorkingCapitalLoanDelinquencyAction();
        reset.setAction(DelinquencyAction.RESET);
        reset.setStartDate(resetDate);
        reset.setStartNewPeriod(startNewPeriod);
        return reset;
    }
}
