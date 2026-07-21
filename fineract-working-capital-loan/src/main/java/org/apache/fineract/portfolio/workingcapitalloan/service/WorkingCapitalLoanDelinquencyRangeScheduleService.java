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

    void generateInitialPeriod(WorkingCapitalLoan loan);

    List<WorkingCapitalLoanDelinquencyRangeSchedule> generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate);

    boolean hasSchedule(Long loanId);

    void applyRepayment(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal amount);

    void applyRepaymentUndo(WorkingCapitalLoan loan, LocalDate businessDate, BigDecimal amount);

    void evaluateExpiredPeriods(WorkingCapitalLoan loan, LocalDate businessDate);

    List<WorkingCapitalLoanDelinquencyRangeScheduleData> retrieveRangeSchedule(Long loanId);

    /**
     * Shifts the boundaries of every period overlapping or following {@code pauseStart} by the inclusive length of the
     * pause, including periods that have already been evaluated (minPaymentCriteriaMet != null) so a backdated pause
     * can also reach into a period that was previously closed.
     * {@link #reprocessDelinquencySchedule(WorkingCapitalLoan)} is expected to be invoked afterwards to re-derive their
     * evaluation.
     */
    void extendPeriodsForPause(WorkingCapitalLoan loan, LocalDate pauseStart, LocalDate pauseEnd);

    /**
     * Re-derives the base expectation of the current period and the boundaries of future periods from the effective
     * reschedule parameters resolved from the persisted RESCHEDULE actions; a newly created reschedule action must
     * therefore be saved before this is called. Amounts, the remaining-balance cap and expired-period evaluation are
     * left to {@link #reprocessDelinquencySchedule(WorkingCapitalLoan)}, which the caller must invoke afterwards.
     */
    void rescheduleMinimumPayment(WorkingCapitalLoan loan);

    void resumeActivePause(WorkingCapitalLoan loan, WorkingCapitalLoanDelinquencyAction activePause,
            WorkingCapitalLoanDelinquencyAction resumeAction);

    /**
     * Rebuilds paid amounts and evaluation for all periods by replaying the principal portions of the repayment type
     * transactions in chronological order.
     */
    void reprocessDelinquencySchedule(WorkingCapitalLoan loan);

    void resetPeriods(WorkingCapitalLoan workingCapitalLoan, WorkingCapitalLoanDelinquencyAction action);

    void undoResetPeriods(WorkingCapitalLoan workingCapitalLoan, WorkingCapitalLoanDelinquencyAction action,
            List<WorkingCapitalLoanDelinquencyAction> byWorkingCapitalLoanIdOrderById);
}
