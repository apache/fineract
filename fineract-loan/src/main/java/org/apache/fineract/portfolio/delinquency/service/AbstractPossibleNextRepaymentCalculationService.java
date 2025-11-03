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
package org.apache.fineract.portfolio.delinquency.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

@RequiredArgsConstructor
public abstract class AbstractPossibleNextRepaymentCalculationService implements PossibleNextRepaymentCalculationService {

    @Override
    public BigDecimal possibleNextRepaymentAmount(Loan loan, LocalDate nextPaymentDueDate) {
        LoanRepaymentScheduleInstallment nextInstallment = loan.getRelatedRepaymentScheduleInstallment(nextPaymentDueDate);
        if (nextInstallment == null || nextInstallment.isObligationsMet()) {
            return BigDecimal.ZERO;
        }
        if (loan.isInterestRecalculationEnabled()
                // if rest frequency type is same as repayment, then interest values should be on the repayment schedule
                // correctly.
                && !loan.getLoanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment()
                // if charge off, installments already shows correct values, no further calculation is required.
                && !loan.isChargeOffOnDate(nextPaymentDueDate)
                // all strategy works like same as repayment on installment due date.
                && nextInstallment.getDueDate().isAfter(ThreadLocalContextUtil.getBusinessDate())
                // there is no overdue / overdue related to that installment is calculated.
                && !nextInstallment.getFromDate().isEqual(ThreadLocalContextUtil.getBusinessDate())
                && MathUtil.isGreaterThanZero(loan.getDisbursedAmount())) {
            // try to predict future outstanding balances with interest recalculation
            return calculateInterestRecalculationFutureOutstandingValue(loan, nextPaymentDueDate, nextInstallment);
        }
        return nextInstallment.getTotalOutstanding(loan.getCurrency()).getAmount();
    }

    public abstract BigDecimal calculateInterestRecalculationFutureOutstandingValue(Loan loan, LocalDate nextPaymentDueDate,
            LoanRepaymentScheduleInstallment nextInstallment);

}
