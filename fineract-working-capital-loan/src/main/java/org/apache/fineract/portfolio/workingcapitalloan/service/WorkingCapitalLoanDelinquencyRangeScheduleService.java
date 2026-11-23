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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeSchedule;

public interface WorkingCapitalLoanDelinquencyRangeScheduleService {

    boolean generateInitialPeriod(WorkingCapitalLoan loan);

    List<WorkingCapitalLoanDelinquencyRangeSchedule> generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate);

    boolean hasSchedule(Long loanId);

    /**
     * Removes every delinquency range schedule period of the loan together with its delinquency tag history and the
     * delinquency actions recorded against it, so the next disbursement generates the schedule again from the loan's
     * current delinquency configuration alone.
     */
    void deleteScheduleAndActions(Long loanId);

    /**
     * Applies a repayment to the delinquency range schedule periods in place. Does not classify delinquency: the caller
     * passes the returned mode to
     * {@link WorkingCapitalLoanDelinquencyClassificationService#classifyDelinquencyOnFinalState} once the loan status
     * has been changed and the loan saved.
     *
     * @return {@link WorkingCapitalLoanDelinquencyClassificationMode#INSTANT}, or
     *         {@link WorkingCapitalLoanDelinquencyClassificationMode#NONE} while delinquency is disabled
     */
    WorkingCapitalLoanDelinquencyClassificationMode applyRepaymentToPeriods(WorkingCapitalLoan loan, LocalDate transactionDate,
            BigDecimal amount);

    boolean evaluateExpiredPeriods(WorkingCapitalLoan loan, LocalDate businessDate);

    List<WorkingCapitalLoanDelinquencyRangeScheduleData> retrieveRangeSchedule(Long loanId);

    /**
     * Shifts the boundaries of every period overlapping or following {@code pauseStart} by the inclusive length of the
     * pause, including periods that have already been evaluated (minPaymentCriteriaMet != null) so a backdated pause
     * can also reach into a period that was previously closed. {@link #reprocessSchedulePeriods(WorkingCapitalLoan)} is
     * expected to be invoked afterwards to re-derive their evaluation, followed by the classification it returns.
     */
    void extendPeriodsForPause(WorkingCapitalLoan loan, LocalDate pauseStart, LocalDate pauseEnd);

    /**
     * Re-derives the base expectation of the current period and the boundaries of future periods from the effective
     * reschedule parameters resolved from the persisted RESCHEDULE actions; a newly created reschedule action must
     * therefore be saved before this is called. When {@code action} carries a frequency group, the current open period
     * is also re-dated: its toDate is recalculated from its fromDate and the new frequency, extended by the recorded
     * pauses that overlap the period. Amounts, the remaining-balance cap and expired-period evaluation are left to
     * {@link #reprocessSchedulePeriods(WorkingCapitalLoan)}, which the caller must invoke afterwards, followed by the
     * classification it returns.
     */
    void rescheduleMinimumPayment(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyAction action);

    void recalculateBaseExpectedAmount(WorkingCapitalLoan loan);

    /**
     * Ends the active pause at the resume date, shortens the periods it had extended and evaluates the periods that
     * have ended as a result. Does not classify delinquency: the caller passes the returned mode to
     * {@link WorkingCapitalLoanDelinquencyClassificationService#classifyDelinquencyOnFinalState}.
     *
     * @return {@link WorkingCapitalLoanDelinquencyClassificationMode#ALWAYS}, or
     *         {@link WorkingCapitalLoanDelinquencyClassificationMode#NONE} while delinquency is disabled
     */
    WorkingCapitalLoanDelinquencyClassificationMode resumeActivePause(WorkingCapitalLoan loan,
            WorkingCapitalLoanDelinquencyAction activePause, WorkingCapitalLoanDelinquencyAction resumeAction);

    /**
     * Rebuilds paid amounts and evaluation for all periods by replaying the principal portions of the repayment type
     * transactions in chronological order. Does not classify delinquency: the caller passes the returned mode to
     * {@link WorkingCapitalLoanDelinquencyClassificationService#classifyDelinquencyOnFinalState} once the loan status
     * has been changed and the loan saved.
     *
     * @return {@link WorkingCapitalLoanDelinquencyClassificationMode#ALWAYS}, or
     *         {@link WorkingCapitalLoanDelinquencyClassificationMode#NONE} while delinquency is disabled
     */
    WorkingCapitalLoanDelinquencyClassificationMode reprocessSchedulePeriods(WorkingCapitalLoan loan);

    /**
     * Brings a freshly generated schedule up to the business date. A disbursement backdated past the end of its
     * starting period leaves periods that have already ended: they are generated and evaluated here, through
     * {@link #reprocessSchedulePeriods(WorkingCapitalLoan)}, instead of at the next COB. Does nothing while no period
     * has ended before the business date. Does not classify delinquency.
     *
     * @return {@link WorkingCapitalLoanDelinquencyClassificationMode#ALWAYS} when the schedule was rebuilt, otherwise
     *         {@link WorkingCapitalLoanDelinquencyClassificationMode#NONE}
     */
    WorkingCapitalLoanDelinquencyClassificationMode reprocessSchedulePeriodsIfAnyEnded(WorkingCapitalLoan loan);

    void resetPeriods(WorkingCapitalLoan workingCapitalLoan, WorkingCapitalLoanDelinquencyAction action);

    /**
     * Clears the reset flag of the periods the undone reset had cut. Amounts and expired-period evaluation are left to
     * {@link #reprocessSchedulePeriods(WorkingCapitalLoan)}, which the caller must invoke afterwards, followed by the
     * classification it returns.
     */
    void undoResetPeriods(WorkingCapitalLoan workingCapitalLoan, WorkingCapitalLoanDelinquencyAction action,
            List<WorkingCapitalLoanDelinquencyAction> byWorkingCapitalLoanIdOrderById);
}
