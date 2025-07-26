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
package org.apache.fineract.portfolio.loanaccount.serialization;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidLoanStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanDisbursalException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class LoanDisbursementValidator {

    private final LoanApplicationValidator loanApplicationValidator;

    public void compareDisbursedToApprovedOrProposedPrincipal(final Loan loan, final BigDecimal disbursedAmount,
            final BigDecimal totalDisbursed) {
        final BigDecimal capitalizedIncome = loan.getSummary().getTotalCapitalizedIncome();
        if (loan.loanProduct().isDisallowExpectedDisbursements() && loan.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
            validateOverMaximumAmount(loan, totalDisbursed, capitalizedIncome);
        } else {
            if (loan.loanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
                validateOverMaximumAmount(loan, disbursedAmount, capitalizedIncome);
            } else {
                if ((totalDisbursed.compareTo(loan.getApprovedPrincipal()) > 0)
                        || (totalDisbursed.add(capitalizedIncome).compareTo(loan.getApprovedPrincipal()) > 0)) {
                    final String errorMsg = "Loan can't be disbursed, disburse amount is exceeding approved principal.";
                    throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.principal", totalDisbursed,
                            loan.getApprovedPrincipal());
                }
            }
        }
    }

    public void validateOverMaximumAmount(final Loan loan, final BigDecimal totalDisbursed, final BigDecimal capitalizedIncome) {
        final BigDecimal maxDisbursedAmount = loanApplicationValidator.getOverAppliedMax(loan);
        if (totalDisbursed.add(capitalizedIncome).compareTo(maxDisbursedAmount) > 0) {
            final String errorMessage = String.format(
                    "Loan disbursal amount can't be greater than maximum applied loan amount calculation. "
                            + "Total disbursed amount: %s  Maximum disbursal amount: %s",
                    totalDisbursed.stripTrailingZeros().toPlainString(), maxDisbursedAmount.stripTrailingZeros().toPlainString());
            throw new InvalidLoanStateTransitionException("disbursal",
                    "amount.can't.be.greater.than.maximum.applied.loan.amount.calculation", errorMessage, totalDisbursed,
                    maxDisbursedAmount);
        }
    }

    public void validateDisburseAmountNotExceedingApprovedAmount(final Loan loan, final BigDecimal diff,
            final BigDecimal principalDisbursed) {
        if (!loan.loanProduct().isMultiDisburseLoan() && diff.compareTo(BigDecimal.ZERO) < 0) {
            final String errorMsg = "Loan can't be disbursed,disburse amount is exceeding approved amount ";
            throw new LoanDisbursalException(errorMsg, "disburse.amount.must.be.less.than.approved.amount", principalDisbursed,
                    loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount());
        }
    }

    public void validateDisburseDate(final Loan loan, final LocalDate disbursedOn, final LocalDate expectedDate) {
        if (expectedDate != null
                && (DateUtils.isAfter(disbursedOn, loan.fetchRepaymentScheduleInstallment(1).getDueDate())
                        || DateUtils.isAfter(disbursedOn, expectedDate))
                && DateUtils.isEqual(disbursedOn, loan.getActualDisbursementDate())) {
            final String errorMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate: " + expectedDate;
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.after.expected.first.repayment.date", errorMessage,
                    disbursedOn, expectedDate);
        }

        if (DateUtils.isDateInTheFuture(disbursedOn)) {
            final String errorMessage = "The date on which a loan with identifier : " + loan.getAccountNumber()
                    + " is disbursed cannot be in the future.";
            throw new InvalidLoanStateTransitionException("disbursal", "cannot.be.a.future.date", errorMessage, disbursedOn);
        }
    }
}
