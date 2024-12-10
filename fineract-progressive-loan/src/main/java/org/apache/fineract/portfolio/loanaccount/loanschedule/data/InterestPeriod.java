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
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;

@Getter
@ToString(exclude = { "repaymentPeriod" })
@EqualsAndHashCode(exclude = { "repaymentPeriod" })
public class InterestPeriod implements Comparable<InterestPeriod> {

    private final RepaymentPeriod repaymentPeriod;
    private final LocalDate fromDate;
    @Setter
    @NotNull
    private LocalDate dueDate;
    @Setter
    private BigDecimal rateFactor;
    @Setter
    private BigDecimal rateFactorTillPeriodDueDate;
    private Money disbursementAmount;
    private Money balanceCorrectionAmount;
    private Money outstandingLoanBalance;
    private final MathContext mc;

    public InterestPeriod(RepaymentPeriod repaymentPeriod, LocalDate fromDate, LocalDate dueDate, BigDecimal rateFactor,
            BigDecimal rateFactorTillPeriodDueDate, Money disbursementAmount, Money balanceCorrectionAmount, Money outstandingLoanBalance,
            MathContext mc) {
        this.repaymentPeriod = repaymentPeriod;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.rateFactor = rateFactor;
        this.rateFactorTillPeriodDueDate = rateFactorTillPeriodDueDate;
        this.disbursementAmount = disbursementAmount;
        this.balanceCorrectionAmount = balanceCorrectionAmount;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.mc = mc;
    }

    public InterestPeriod(RepaymentPeriod repaymentPeriod, InterestPeriod interestPeriod) {
        this(repaymentPeriod, interestPeriod.getFromDate(), interestPeriod.getDueDate(), interestPeriod.getRateFactor(),
                interestPeriod.getRateFactorTillPeriodDueDate(), interestPeriod.getDisbursementAmount(),
                interestPeriod.getBalanceCorrectionAmount(), interestPeriod.getOutstandingLoanBalance(), interestPeriod.getMc());
    }

    @Override
    public int compareTo(@NotNull InterestPeriod o) {
        return dueDate.compareTo(o.dueDate);
    }

    public void addBalanceCorrectionAmount(final Money balanceCorrectionAmount) {
        this.balanceCorrectionAmount = MathUtil.plus(this.balanceCorrectionAmount, balanceCorrectionAmount);
    }

    public void addDisbursementAmount(final Money disbursementAmount) {
        this.disbursementAmount = MathUtil.plus(this.disbursementAmount, disbursementAmount, mc);
    }

    public Money getCalculatedDueInterest() {
        long lengthTillPeriodDueDate = getLengthTillPeriodDueDate();
        final BigDecimal interestDueTillRepaymentDueDate = lengthTillPeriodDueDate == 0 //
                ? BigDecimal.ZERO //
                : getOutstandingLoanBalance() //
                        .multipliedBy(getRateFactorTillPeriodDueDate(), mc).getAmount() //
                        .divide(BigDecimal.valueOf(lengthTillPeriodDueDate), mc) //
                        .multiply(BigDecimal.valueOf(getLength()), mc); //
        return Money.of(outstandingLoanBalance.getCurrencyData(), interestDueTillRepaymentDueDate, mc);
    }

    public long getLength() {
        return DateUtils.getDifferenceInDays(fromDate, dueDate);
    }

    public long getLengthTillPeriodDueDate() {
        return DateUtils.getDifferenceInDays(fromDate, repaymentPeriod.getDueDate());
    }

    public void updateOutstandingLoanBalance() {
        if (isFirstInterestPeriod()) {
            Optional<RepaymentPeriod> previousRepaymentPeriod = getRepaymentPeriod().getPrevious();
            if (previousRepaymentPeriod.isPresent()) {
                InterestPeriod previousInterestPeriod = previousRepaymentPeriod.get().getLastInterestPeriod();
                this.outstandingLoanBalance = previousInterestPeriod.getOutstandingLoanBalance()//
                        .plus(previousInterestPeriod.getDisbursementAmount(), mc)//
                        .plus(previousInterestPeriod.getBalanceCorrectionAmount(), mc)//
                        .minus(previousRepaymentPeriod.get().getDuePrincipal(), mc)//
                        .plus(previousRepaymentPeriod.get().getPaidPrincipal(), mc);//
            }
        } else {
            int index = getRepaymentPeriod().getInterestPeriods().indexOf(this);
            InterestPeriod previousInterestPeriod = getRepaymentPeriod().getInterestPeriods().get(index - 1);
            this.outstandingLoanBalance = previousInterestPeriod.getOutstandingLoanBalance() //
                    .plus(previousInterestPeriod.getBalanceCorrectionAmount(), mc) //
                    .plus(previousInterestPeriod.getDisbursementAmount(), mc); //
        }
    }

    public boolean isFirstInterestPeriod() {
        return this.equals(getRepaymentPeriod().getFirstInterestPeriod());
    }
}
