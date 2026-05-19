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
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;

public interface WorkingCapitalLoanAmortizationScheduleWriteService {

    void generateAndSaveAmortizationSchedule(Long loanId, ProjectedAmortizationScheduleGenerateRequest request);

    void generateAndSaveAmortizationScheduleOnDisbursement(WorkingCapitalLoan loan, BigDecimal disbursedAmount, LocalDate disbursementDate);

    void generateAndSaveAmortizationScheduleOnApproval(WorkingCapitalLoan loan);

    void regenerateAmortizationScheduleOnUndoDisbursal(WorkingCapitalLoan loan);

    void applyRepayment(WorkingCapitalLoan loan, LocalDate transactionDate, BigDecimal repaymentAmount);

    BigDecimal getWorkingCapitalLoanDiscountAmount(WorkingCapitalLoan loan);

    void regenerateAmortizationScheduleOnRateChange(WorkingCapitalLoan loan, BigDecimal newRate);

    /**
     * After a discount fee adjustment: regenerates the projected schedule with the new loan-level discount (as on
     * disbursement generation) and re-applies recorded actual repayments only.
     */
    void applyDiscountFeeAdjustment(WorkingCapitalLoan loan, LocalDate adjustmentTransactionDate);
}
