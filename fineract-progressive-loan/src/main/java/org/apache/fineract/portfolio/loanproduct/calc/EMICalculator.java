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
package org.apache.fineract.portfolio.loanproduct.calc;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

public interface EMICalculator {

    @NotNull
    ProgressiveLoanInterestScheduleModel generatePeriodInterestScheduleModel(@NotNull List<LoanScheduleModelRepaymentPeriod> periods,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf,
            MathContext mc);

    @NotNull
    ProgressiveLoanInterestScheduleModel generateInstallmentInterestScheduleModel(
            @NotNull List<LoanRepaymentScheduleInstallment> installments,
            @NotNull LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf,
            MathContext mc);

    Optional<RepaymentPeriod> findRepaymentPeriod(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate dueDate);

    void addDisbursement(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate disbursementDueDate, Money disbursedAmount);

    void changeInterestRate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate newInterestSubmittedOnDate,
            BigDecimal newInterestRate);

    void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate balanceCorrectionDate,
            Money balanceCorrectionAmount);

    void payInterest(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate, LocalDate transactionDate,
            Money interestAmount);

    void payPrincipal(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate, LocalDate transactionDate,
            Money principalAmount);

    @NotNull
    PeriodDueDetails getDueAmounts(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate);

    @NotNull
    Money getPeriodInterestTillDate(@NotNull ProgressiveLoanInterestScheduleModel scheduleModel, @NotNull LocalDate periodDueDate,
            @NotNull LocalDate targetDate);

    Money getOutstandingLoanBalanceOfPeriod(ProgressiveLoanInterestScheduleModel interestScheduleModel, LocalDate repaymentPeriodDueDate,
            LocalDate targetDate);

    OutstandingDetails getOutstandingAmountsTillDate(ProgressiveLoanInterestScheduleModel model, LocalDate targetDate);

    Money getSumOfDueInterestsOnDate(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate subjectDate);
}
