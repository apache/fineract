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

import jakarta.validation.constraints.NotNull;
import static java.time.temporal.ChronoUnit.DAYS;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(exclude = {"repaymentPeriod"})
public class InterestPeriod implements Comparable<InterestPeriod> {

    @ToString.Exclude
    @Getter
    private final RepaymentPeriod repaymentPeriod;
    @Getter
    private final LocalDate fromDate;
    @Getter
    @Setter
    private LocalDate dueDate;
    @Getter
    @Setter
    private Money outstandingLoanBalance;
    @Getter
    private Money balanceCorrectionAmount;

    public InterestPeriod(RepaymentPeriod repaymentPeriod, LocalDate fromDate, LocalDate dueDate, Money outstandingLoanBalance, Money balanceCorrectionAmount) {
        this.repaymentPeriod = repaymentPeriod;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.balanceCorrectionAmount = balanceCorrectionAmount;
    }

    @Override
    public int compareTo(@NotNull InterestPeriod o) {
        return dueDate.compareTo(o.dueDate);
    }

    public void addBalanceCorrectionAmount(final Money balanceCorrectionAmount) {
        if (!MathUtil.isEmpty(balanceCorrectionAmount)) {
            this.balanceCorrectionAmount = this.balanceCorrectionAmount.add(balanceCorrectionAmount);
        }
    }

    public void addOutstandingLoanBalance(final Money outstandingLoanBalance) {
        if (!MathUtil.isEmpty(outstandingLoanBalance)) {
            this.outstandingLoanBalance = this.outstandingLoanBalance.add(outstandingLoanBalance);
        }
    }

    public BigDecimal getRateFactor() {
        long numberOfDaysOfIP = DAYS.between(dueDate, fromDate);
        long numberOfDaysOfRP = DAYS.between(repaymentPeriod.getDueDate(), repaymentPeriod.getFromDate());
        return repaymentPeriod.getRateFactor() //
                .divide(BigDecimal.valueOf(numberOfDaysOfRP), MoneyHelper.getMathContext()) //
                .multiply(BigDecimal.valueOf(numberOfDaysOfIP)); //
    }

    public Money getCalculatedDueInterest() {
        return outstandingLoanBalance.multipliedBy(getRateFactor());
    }
}
