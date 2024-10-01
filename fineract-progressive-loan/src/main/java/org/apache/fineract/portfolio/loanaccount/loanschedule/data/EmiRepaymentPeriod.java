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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.organisation.monetary.domain.Money;

@ToString
@EqualsAndHashCode(exclude = { "previous", "next" })
public class EmiRepaymentPeriod {

    @ToString.Exclude
    private final EmiRepaymentPeriod previous;
    @Setter
    @ToString.Exclude
    private EmiRepaymentPeriod next;

    @Getter
    private final LocalDate fromDate;
    @Getter
    private final LocalDate dueDate;

    @Getter
    private List<EmiInterestPeriod> interestPeriods;

    @Getter
    @Setter
    private Money equalMonthlyInstallment;
    @Getter
    @Setter
    private Money principalDue;
    @Getter
    @Setter
    private Money remainingBalance;

    public EmiRepaymentPeriod(final LocalDate fromDate, final LocalDate dueDate, final Money equalMonthlyInstallment,
            final EmiRepaymentPeriod previous) {
        this.previous = previous;
        this.next = null;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.equalMonthlyInstallment = equalMonthlyInstallment;

        final Money zeroAmount = Money.zero(equalMonthlyInstallment.getCurrency());
        this.remainingBalance = zeroAmount;
        this.principalDue = zeroAmount;
        this.interestPeriods = new ArrayList<>();
    }

    public EmiRepaymentPeriod(final EmiRepaymentPeriod repaymentModel, final EmiRepaymentPeriod previous) {
        this.previous = previous;
        this.next = null;
        this.fromDate = repaymentModel.fromDate;
        this.dueDate = repaymentModel.dueDate;
        this.equalMonthlyInstallment = repaymentModel.equalMonthlyInstallment;
        this.remainingBalance = repaymentModel.remainingBalance;
        this.principalDue = repaymentModel.principalDue;
        this.interestPeriods = new ArrayList<>();
    }

    public void updateInterestPeriods(final List<EmiInterestPeriod> interestPeriods) {
        this.interestPeriods = interestPeriods.stream()
                .filter(interestPeriod -> interestPeriod.getRepaymentPeriod().equals(this)
                        || interestPeriod.getRepaymentPeriod().getDueDate().equals(this.dueDate))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Optional<EmiRepaymentPeriod> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public Optional<EmiRepaymentPeriod> getNext() {
        return Optional.ofNullable(next);
    }

    /**
     * Add and sort interest periods
     *
     * @param interestPeriod
     */
    public void addInterestPeriod(EmiInterestPeriod interestPeriod) {
        interestPeriods.add(interestPeriod);
        Collections.sort(interestPeriods);
    }

    public boolean isLastPeriod() {
        return next == null;
    }

    public BigDecimal getRateFactor() {
        return interestPeriods.stream().map(EmiInterestPeriod::getRateFactorMinus1).reduce(BigDecimal.ONE, BigDecimal::add);
    }

    public Money getDisbursedAmountInPeriod() {
        return interestPeriods.stream().map(EmiInterestPeriod::getDisbursedAmount).reduce(Money.zero(equalMonthlyInstallment.getCurrency()),
                Money::plus);
    }

    public Money getInterestDue() {
        return interestPeriods.stream().map(EmiInterestPeriod::getInterestDue).reduce(Money.zero(equalMonthlyInstallment.getCurrency()),
                Money::plus);
    }

    public Money getInitialBalance() {
        return previous != null ? previous.getRemainingBalance() : Money.zero(equalMonthlyInstallment.getCurrency());
    }

    public Money getCorrectionAmount() {
        return interestPeriods.stream().map(EmiInterestPeriod::getCorrectionAmount)
                .reduce(Money.zero(equalMonthlyInstallment.getCurrency()), Money::plus);
    }

    public Money getOutstandingBalance() {
        return getInitialBalance().plus(getDisbursedAmountInPeriod()).plus(getCorrectionAmount());
    }
}
