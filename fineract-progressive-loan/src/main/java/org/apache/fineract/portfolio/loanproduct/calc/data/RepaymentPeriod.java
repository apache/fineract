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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isInPeriod;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.util.Memo;

@ToString(exclude = { "previous" })
@EqualsAndHashCode(exclude = { "previous" })
public final class RepaymentPeriod {

    private final RepaymentPeriod previous;
    @Getter
    private final LocalDate fromDate;
    @Setter
    @Getter
    private LocalDate dueDate;
    @Getter
    @Setter
    private List<InterestPeriod> interestPeriods;
    @Setter
    @Getter
    private Money emi;
    @Setter
    @Getter
    private Money originalEmi;
    @Getter
    private Money paidPrincipal;
    @Getter
    private Money paidInterest;

    private final MathContext mc;

    private Memo<BigDecimal> rateFactorPlus1Calculation;
    private Memo<Money> calculatedDueInterestCalculation;
    private Memo<Money> dueInterestCalculation;
    private Memo<Money> outstandingBalanceCalculation;

    private RepaymentPeriod(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, List<InterestPeriod> interestPeriods,
            Money emi, Money originalEmi, Money paidPrincipal, Money paidInterest, MathContext mc) {
        this.previous = previous;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.interestPeriods = interestPeriods;
        this.emi = emi;
        this.originalEmi = originalEmi;
        this.paidPrincipal = paidPrincipal;
        this.paidInterest = paidInterest;
        this.mc = mc;
    }

    public static RepaymentPeriod create(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, Money emi, MathContext mc) {
        final Money zero = emi.zero();
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, fromDate, dueDate, new ArrayList<>(), emi, emi, zero, zero,
                mc);
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        newRepaymentPeriod.interestPeriods.add(InterestPeriod.withEmptyAmounts(newRepaymentPeriod, fromDate, dueDate));
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copy(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.fromDate, repaymentPeriod.dueDate,
                new ArrayList<>(), repaymentPeriod.emi, repaymentPeriod.originalEmi, repaymentPeriod.paidPrincipal,
                repaymentPeriod.paidInterest, mc);
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.interestPeriods) {
            newRepaymentPeriod.interestPeriods.add(InterestPeriod.copy(newRepaymentPeriod, interestPeriod));
        }
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copyWithoutPaidAmounts(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final Money zero = repaymentPeriod.emi.zero();
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.fromDate, repaymentPeriod.dueDate,
                new ArrayList<>(), repaymentPeriod.emi, repaymentPeriod.originalEmi, zero, zero, mc);
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.interestPeriods) {
            var interestPeriodCopy = InterestPeriod.copy(newRepaymentPeriod, interestPeriod);
            if (!interestPeriodCopy.getBalanceCorrectionAmount().isZero()) {
                interestPeriodCopy.addBalanceCorrectionAmount(interestPeriodCopy.getBalanceCorrectionAmount().negated());
            }
            newRepaymentPeriod.interestPeriods.add(interestPeriodCopy);
        }
        return newRepaymentPeriod;
    }

    public Optional<RepaymentPeriod> getPrevious() {
        return Optional.ofNullable(previous);
    }

    /**
     * This method gives back sum of (Rate Factor +1) from the interest periods
     *
     * @return
     */
    public BigDecimal getRateFactorPlus1() {
        if (rateFactorPlus1Calculation == null) {
            rateFactorPlus1Calculation = Memo.of(this::calculateRateFactorPlus1, () -> this.interestPeriods);
        }
        return rateFactorPlus1Calculation.get();
    }

    private BigDecimal calculateRateFactorPlus1() {
        return interestPeriods.stream().map(InterestPeriod::getRateFactor).reduce(BigDecimal.ONE, BigDecimal::add);
    }

    /**
     * Gives back calculated due interest + chargeback interest
     *
     * @return
     */
    @NotNull
    public Money getCalculatedDueInterest() {
        if (calculatedDueInterestCalculation == null) {
            calculatedDueInterestCalculation = Memo.of(this::calculateCalculatedDueInterest,
                    () -> new Object[] { this.previous, this.interestPeriods });
        }
        return calculatedDueInterestCalculation.get();
    }

    private Money calculateCalculatedDueInterest() {
        Money calculatedDueInterest = getInterestPeriods().stream().map(InterestPeriod::getCalculatedDueInterest).reduce(getZero(mc),
                (m1, m2) -> m1.plus(m2, mc));
        if (getPrevious().isPresent()) {
            calculatedDueInterest = calculatedDueInterest.add(getPrevious().get().getUnrecognizedInterest(), mc);
        }
        return calculatedDueInterest;
    }

    /**
     * Gives back due interest + chargeback interest OR paid interest
     *
     * @return
     */
    public Money getDueInterest() {
        if (dueInterestCalculation == null) {
            // Due interest might be the maximum paid if there is pay-off or early repayment
            dueInterestCalculation = Memo.of(() -> MathUtil.max(
                    getPaidPrincipal().isGreaterThan(getCalculatedDuePrincipal()) ? getPaidInterest() : getCalculatedDueInterest(),
                    getPaidInterest(), false), () -> new Object[] { paidPrincipal, paidInterest, interestPeriods });
        }
        return dueInterestCalculation.get();
    }

    /**
     * Gives back an EMI amount which includes chargeback amounts as well
     *
     * @return
     */
    public Money getEmiPlusChargeback() {
        return getEmi().plus(getTotalChargebackAmount(), mc); //
    }

    /**
     * Gives back principal due + charge back principal based on (EMI - Calculated Due Interest)
     *
     * @return
     */
    public Money getCalculatedDuePrincipal() {
        return getEmiPlusChargeback().minus(getCalculatedDueInterest(), mc);
    }

    /**
     * Sum of chargeback principals
     *
     * @return
     */
    public Money getChargebackPrincipal() {
        return interestPeriods.stream() //
                .map(InterestPeriod::getChargebackPrincipal) //
                .reduce(getZero(mc), (value, previous) -> value.plus(previous, mc)); //
    }

    /**
     * Sum of chargeback interests
     *
     * @return
     */
    public Money getChargebackInterest() {
        return interestPeriods.stream() //
                .map(InterestPeriod::getChargebackInterest) //
                .reduce(getZero(mc), (value, previous) -> value.plus(previous, mc)); //
    }

    /**
     * Gives back due principal + chargeback principal or paid principal
     *
     * @return
     */
    public Money getDuePrincipal() {
        // Due principal might be the maximum paid if there is pay-off or early repayment
        return MathUtil.max(getEmiPlusChargeback().minus(getDueInterest(), mc), getPaidPrincipal(), false);
    }

    /**
     * Gives back sum of all chargeback principal + chargeback interest
     *
     * @return
     */
    public Money getTotalChargebackAmount() {
        return getChargebackPrincipal().plus(getChargebackInterest(), mc);
    }

    /**
     * Total paid amounts has everything: paid principal + paid interest + paid charge principal + paid charge interest
     *
     * @return
     */
    public Money getTotalPaidAmount() {
        return getPaidPrincipal().plus(getPaidInterest());
    }

    public boolean isFullyPaid() {
        return getEmi().isEqualTo(getTotalPaidAmount());
    }

    /**
     * This method counts those interest amounts when there is no place in EMI. Which typically can happen if there is a
     * not full paid early repayment. In this case we can count in the next repayment period.
     *
     * @return
     */
    public Money getUnrecognizedInterest() {
        return getCalculatedDueInterest().minus(getDueInterest(), mc);
    }

    public Money getCreditedAmounts() {
        return interestPeriods.stream().map(InterestPeriod::getCreditedAmounts).reduce(getZero(mc), (m1, m2) -> m1.plus(m2, mc));
    }

    public Money getOutstandingLoanBalance() {
        if (outstandingBalanceCalculation == null) {
            outstandingBalanceCalculation = Memo.of(() -> {
                InterestPeriod lastInterestPeriod = getInterestPeriods().get(getInterestPeriods().size() - 1);
                Money calculatedOutStandingLoanBalance = lastInterestPeriod.getOutstandingLoanBalance() //
                        .plus(lastInterestPeriod.getBalanceCorrectionAmount(), mc) //
                        .plus(lastInterestPeriod.getDisbursementAmount(), mc) //
                        .minus(getDuePrincipal(), mc)//
                        .plus(getPaidPrincipal(), mc);//
                return MathUtil.negativeToZero(calculatedOutStandingLoanBalance, mc);
            }, () -> new Object[] { paidPrincipal, paidInterest, interestPeriods });
        }
        return outstandingBalanceCalculation.get();
    }

    public void addPaidPrincipalAmount(Money paidPrincipal) {
        this.paidPrincipal = MathUtil.plus(this.paidPrincipal, paidPrincipal, mc);
    }

    public void addPaidInterestAmount(Money paidInterest) {
        this.paidInterest = MathUtil.plus(this.paidInterest, paidInterest, mc);
    }

    public Money getInitialBalanceForEmiRecalculation() {
        Money initialBalance;
        if (getPrevious().isPresent()) {
            initialBalance = getPrevious().get().getOutstandingLoanBalance();
        } else {
            initialBalance = getZero(mc);
        }
        Money totalDisbursedAmount = getInterestPeriods().stream() //
                .map(InterestPeriod::getDisbursementAmount) //
                .reduce(getZero(mc), (m1, m2) -> m1.plus(m2, mc)); //
        return initialBalance.add(totalDisbursedAmount, mc);
    }

    private Money getZero(MathContext mc) {
        // EMI is always initiated
        return this.emi.zero(mc);
    }

    public InterestPeriod getFirstInterestPeriod() {
        return getInterestPeriods().get(0);
    }

    public InterestPeriod getLastInterestPeriod() {
        List<InterestPeriod> interestPeriods = getInterestPeriods();
        return interestPeriods.get(interestPeriods.size() - 1);
    }

    public Optional<InterestPeriod> findInterestPeriod(@NotNull LocalDate transactionDate) {
        return interestPeriods.stream() //
                .filter(interestPeriod -> isInPeriod(transactionDate, interestPeriod.getFromDate(), interestPeriod.getDueDate(),
                        isFirstRepaymentPeriod() && interestPeriod.isFirstInterestPeriod()))//
                .reduce((one, two) -> two);
    }

    public boolean isFirstRepaymentPeriod() {
        return previous == null;
    }
}
