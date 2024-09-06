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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import lombok.Data;
import org.apache.fineract.organisation.monetary.domain.Money;

@Data
public class ProgressiveLoanInterestRepaymentModel {

    private final LocalDate fromDate;
    private final LocalDate dueDate;

    private LinkedList<ProgressiveLoanInterestRepaymentInterestPeriod> interestPeriods;

    private boolean isLastPeriod;

    private Money equalMonthlyInstallment;
    private Money principalDue;
    private Money remainingBalance;

    private Money initialBalance;

    public ProgressiveLoanInterestRepaymentModel(final LocalDate fromDate, final LocalDate dueDate, final Money equalMonthlyInstallment) {
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.equalMonthlyInstallment = equalMonthlyInstallment;
        this.isLastPeriod = false;

        final Money zeroAmount = Money.zero(equalMonthlyInstallment.getCurrency());
        this.initialBalance = zeroAmount;
        this.remainingBalance = zeroAmount;
        this.principalDue = zeroAmount;
        this.interestPeriods = new LinkedList<>();
        this.interestPeriods.add(
                new ProgressiveLoanInterestRepaymentInterestPeriod(fromDate, dueDate, BigDecimal.ZERO, zeroAmount, zeroAmount, zeroAmount));
    }

    public ProgressiveLoanInterestRepaymentModel(ProgressiveLoanInterestRepaymentModel repaymentModel) {
        this.fromDate = repaymentModel.fromDate;
        this.dueDate = repaymentModel.dueDate;
        this.isLastPeriod = repaymentModel.isLastPeriod;
        this.equalMonthlyInstallment = repaymentModel.equalMonthlyInstallment;
        this.initialBalance = repaymentModel.initialBalance;
        this.remainingBalance = repaymentModel.remainingBalance;
        this.principalDue = repaymentModel.principalDue;
        this.interestPeriods = new LinkedList<>();
        for (final ProgressiveLoanInterestRepaymentInterestPeriod interestPeriod : repaymentModel.interestPeriods) {
            this.interestPeriods.add(new ProgressiveLoanInterestRepaymentInterestPeriod(interestPeriod));
        }
    }

    public BigDecimal getRateFactor() {
        return interestPeriods.stream().map(ProgressiveLoanInterestRepaymentInterestPeriod::getRateFactorMinus1).reduce(BigDecimal.ONE,
                BigDecimal::add);
    }

    public Money getDisbursedAmountInPeriod() {
        return interestPeriods.stream().map(ProgressiveLoanInterestRepaymentInterestPeriod::getDisbursedAmount)
                .reduce(Money.zero(equalMonthlyInstallment.getCurrency()), Money::plus);
    }

    public Money getInterestDue() {
        return interestPeriods.stream().map(ProgressiveLoanInterestRepaymentInterestPeriod::getInterestDue)
                .reduce(Money.zero(equalMonthlyInstallment.getCurrency()), Money::plus);
    }

    public Money getCorrectionAmount() {
        return interestPeriods.stream().map(ProgressiveLoanInterestRepaymentInterestPeriod::getCorrectionAmount)
                .reduce(Money.zero(equalMonthlyInstallment.getCurrency()), Money::plus);
    }

    public Money getOutstandingBalance() {
        return initialBalance.plus(getDisbursedAmountInPeriod());
    }

    public Money getCorrectedOutstandingBalance() {
        return getOutstandingBalance().plus(getCorrectionAmount());
    }
}
