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
package org.apache.fineract.portfolio.loanproduct.calc.data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;

@Getter
@ToString(exclude = { "repaymentPeriod" })
@EqualsAndHashCode(exclude = { "repaymentPeriod" })
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InterestPeriod implements Comparable<InterestPeriod> {

    @JsonExclude
    private final RepaymentPeriod repaymentPeriod;
    @Setter
    @NotNull
    private LocalDate fromDate;
    @Setter
    @NotNull
    private LocalDate dueDate;
    @Setter
    private BigDecimal rateFactor;
    @Setter
    private BigDecimal rateFactorTillPeriodDueDate;

    private Money chargebackPrincipal;
    private Money chargebackInterest;

    private Money disbursementAmount;
    private Money balanceCorrectionAmount;
    private Money outstandingLoanBalance;

    @JsonExclude
    private final MathContext mc;
    private final boolean isPaused;

    public static InterestPeriod copy(@NotNull RepaymentPeriod repaymentPeriod, @NotNull InterestPeriod interestPeriod, MathContext mc) {
        return new InterestPeriod(repaymentPeriod, interestPeriod.getFromDate(), interestPeriod.getDueDate(),
                interestPeriod.getRateFactor(), interestPeriod.getRateFactorTillPeriodDueDate(), interestPeriod.getChargebackPrincipal(),
                interestPeriod.getChargebackInterest(), interestPeriod.getDisbursementAmount(), interestPeriod.getBalanceCorrectionAmount(),
                interestPeriod.getOutstandingLoanBalance(), mc, interestPeriod.isPaused());
    }

    public static InterestPeriod empty(@NotNull RepaymentPeriod repaymentPeriod, MathContext mc) {
        return new InterestPeriod(repaymentPeriod, null, null, null, null, null, null, null, null, null, mc, false);
    }

    public static InterestPeriod copy(@NotNull RepaymentPeriod repaymentPeriod, @NotNull InterestPeriod interestPeriod) {
        return new InterestPeriod(repaymentPeriod, interestPeriod.getFromDate(), interestPeriod.getDueDate(),
                interestPeriod.getRateFactor(), interestPeriod.getRateFactorTillPeriodDueDate(), interestPeriod.getChargebackPrincipal(),
                interestPeriod.getChargebackInterest(), interestPeriod.getDisbursementAmount(), interestPeriod.getBalanceCorrectionAmount(),
                interestPeriod.getOutstandingLoanBalance(), interestPeriod.getMc(), interestPeriod.isPaused());
    }

    public static InterestPeriod withEmptyAmounts(@NotNull RepaymentPeriod repaymentPeriod, @NotNull LocalDate fromDate,
            LocalDate dueDate) {
        final Money zero = repaymentPeriod.getEmi().zero();
        return new InterestPeriod(repaymentPeriod, fromDate, dueDate, BigDecimal.ZERO, BigDecimal.ZERO, zero, zero, zero, zero, zero,
                zero.getMc(), false);
    }

    public static InterestPeriod withPausedAndEmptyAmounts(@NotNull RepaymentPeriod repaymentPeriod, @NotNull LocalDate fromDate,
            LocalDate dueDate) {
        final Money zero = repaymentPeriod.getEmi().zero();
        return new InterestPeriod(repaymentPeriod, fromDate, dueDate, BigDecimal.ZERO, BigDecimal.ZERO, zero, zero, zero, zero, zero,
                zero.getMc(), true);
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

    public void addChargebackPrincipalAmount(final Money chargebackPrincipal) {
        this.chargebackPrincipal = MathUtil.plus(this.chargebackPrincipal, chargebackPrincipal, mc);
    }

    public void addChargebackInterestAmount(final Money chargebackInterest) {
        this.chargebackInterest = MathUtil.plus(this.chargebackInterest, chargebackInterest, mc);
    }

    public BigDecimal getCalculatedDueInterest() {
        if (isPaused) {
            return chargebackInterest.getAmount();
        }

        long lengthTillPeriodDueDate = getLengthTillPeriodDueDate();
        final BigDecimal interestDueTillRepaymentDueDate = lengthTillPeriodDueDate == 0 //
                ? BigDecimal.ZERO //
                : getOutstandingLoanBalance().getAmount() //
                        .multiply(getRateFactorTillPeriodDueDate(), mc) //
                        .divide(BigDecimal.valueOf(lengthTillPeriodDueDate), mc) //
                        .multiply(BigDecimal.valueOf(getLength()), mc); //
        return MathUtil.negativeToZero(MathUtil.add(chargebackInterest.getAmount(), interestDueTillRepaymentDueDate, mc));
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
                this.outstandingLoanBalance = MathUtil.negativeToZero(previousInterestPeriod.getOutstandingLoanBalance()//
                        .plus(previousInterestPeriod.getDisbursementAmount(), mc)//
                        .plus(previousInterestPeriod.getBalanceCorrectionAmount(), mc)//
                        .minus(previousRepaymentPeriod.get().getDuePrincipal(), mc)//
                        .plus(previousRepaymentPeriod.get().getPaidPrincipal(), mc), mc);//
            }
        } else {
            int index = getRepaymentPeriod().getInterestPeriods().indexOf(this);
            InterestPeriod previousInterestPeriod = getRepaymentPeriod().getInterestPeriods().get(index - 1);
            this.outstandingLoanBalance = MathUtil.negativeToZero(previousInterestPeriod.getOutstandingLoanBalance() //
                    .plus(previousInterestPeriod.getBalanceCorrectionAmount(), mc) //
                    .plus(previousInterestPeriod.getDisbursementAmount(), mc)); //
        }
    }

    /**
     * Include principal like amounts (all disbursement amount + chargeback principal)
     */
    public Money getCreditedAmounts() {
        return getDisbursementAmount().plus(getChargebackPrincipal(), mc);
    }

    public boolean isFirstInterestPeriod() {
        return this.equals(getRepaymentPeriod().getFirstInterestPeriod());
    }
}
