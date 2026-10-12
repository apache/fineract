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
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachSchedule;

public interface WorkingCapitalLoanBreachScheduleService {

    boolean generateInitialPeriod(WorkingCapitalLoan loan);

    boolean generateNextPeriodIfNeeded(WorkingCapitalLoan loan, LocalDate businessDate);

    boolean hasSchedule(Long loanId);

    List<WorkingCapitalLoanBreachScheduleData> retrieveBreachSchedule(Long loanId);

    boolean evaluateBreachOnDate(WorkingCapitalLoanBreachSchedule period, LocalDate businessDate);

    void applyRepayment(Long loanId, LocalDate transactionDate, BigDecimal amount);

    void applyRepaymentUndo(Long loanId, LocalDate transactionDate, BigDecimal amount);

    boolean evaluateBreach(WorkingCapitalLoan loan, LocalDate businessDate);

    /**
     * Recalculates the schedule from the effective reschedule parameters resolved from the persisted RESCHEDULE
     * actions; a newly created reschedule action must therefore be saved before this is called. When {@code action}
     * carries a frequency group, the current open period is also re-dated: its toDate is recalculated from its fromDate
     * and the new frequency, extended by the recorded pauses that overlap the period.
     */
    void rescheduleMinimumPayment(WorkingCapitalLoan loan, WorkingCapitalLoanBreachAction action);

    void recalculatePeriodsForPauses(WorkingCapitalLoan loan);

    void recalculatePastDueAmount(WorkingCapitalLoan loan);

    void reprocessBreachSchedule(WorkingCapitalLoan loan);
}
