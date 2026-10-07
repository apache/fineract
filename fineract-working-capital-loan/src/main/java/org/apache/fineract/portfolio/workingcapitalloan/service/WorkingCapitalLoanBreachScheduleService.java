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
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;

public interface WorkingCapitalLoanBreachScheduleService {

    boolean generateInitialPeriod(WorkingCapitalLoan loan);

    boolean generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate);

    boolean hasSchedule(Long loanId);

    /**
     * Removes every breach schedule period of the loan together with the breach and near breach actions recorded
     * against it, so the next disbursement generates the schedule again from the loan's current breach configuration
     * alone.
     */
    void deleteScheduleAndActions(Long loanId);

    List<WorkingCapitalLoanBreachScheduleData> retrieveBreachSchedule(Long loanId);

    boolean evaluateBreachOnDate(WorkingCapitalLoanBreachSchedule period, LocalDate businessDate);

    void applyRepayment(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal amount);

    void applyRepaymentUndo(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal amount);

    boolean evaluateBreach(WorkingCapitalLoan loan, LocalDate businessDate);

    /**
     * Replays the recorded breach actions over the schedule after {@code action} was recorded, re-dating the periods it
     * reaches and rewriting their demand.
     */
    void replayForBreachAction(WorkingCapitalLoan loan, WorkingCapitalLoanBreachAction action);

    void recalculateMinimumPayment(WorkingCapitalLoan loan);

    void recalculatePastDueAmount(WorkingCapitalLoan loan);

    /** Derives the reset flags from the persisted breach actions, so a new RESET or UNDO_RESET must be saved first. */
    void applyActiveResetFlags(WorkingCapitalLoan loan);

    void reprocessBreachSchedule(WorkingCapitalLoan loan);

    void splitPeriodAtResetAndReprocess(WorkingCapitalLoan loan, LocalDate resetDate);

    void restoreSplitPeriodAndReprocess(WorkingCapitalLoan loan, WorkingCapitalLoanBreachAction undoneReset);

    /**
     * Re-derives the near breach of the open period when the loan has just been reopened from {@code statusBefore}: the
     * near breach is not evaluated while a loan is closed, so it would otherwise stay stale until the next COB.
     */
    void rederiveNearBreachIfReopened(WorkingCapitalLoan loan, LoanStatus statusBefore);
}
