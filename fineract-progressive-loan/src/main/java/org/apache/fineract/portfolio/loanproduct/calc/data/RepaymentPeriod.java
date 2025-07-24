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
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.apache.fineract.portfolio.util.Memo;

@ToString(exclude = { "previous" })
@EqualsAndHashCode(exclude = { "previous" })
public final class RepaymentPeriod {

    @JsonExclude
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
    @Setter
    @Getter
    private Money futureUnrecognizedInterest;

    @JsonExclude
    private final MathContext mc;

    @JsonExclude
    private Memo<BigDecimal> rateFactorPlus1Calculation;
    @JsonExclude
    private Memo<Money> calculatedDueInterestCalculation;
    @JsonExclude
    private Memo<Money> dueInterestCalculation;
    @JsonExclude
    private Memo<Money> outstandingBalanceCalculation;
    @Getter
    @Setter
    private boolean isInterestMoved = false;

    @Setter
    @Getter
    private BigDecimal totalDisbursedAmount;

    @Setter
    @Getter
    private BigDecimal totalCapitalizedIncomeAmount;

    @JsonExclude
    @Getter
    private final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;

    private RepaymentPeriod(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, List<InterestPeriod> interestPeriods,
            Money emi, Money originalEmi, Money paidPrincipal, Money paidInterest, Money futureUnrecognizedInterest, MathContext mc,
            LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail) {
        this.previous = previous;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.interestPeriods = interestPeriods;
        this.emi = emi;
        this.originalEmi = originalEmi;
        this.paidPrincipal = paidPrincipal;
        this.paidInterest = paidInterest;
        this.futureUnrecognizedInterest = futureUnrecognizedInterest;
        this.mc = mc;
        this.loanProductRelatedDetail = loanProductRelatedDetail;
    }

    public static RepaymentPeriod empty(RepaymentPeriod previous, MathContext mc,
            LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail) {
        return new RepaymentPeriod(previous, null, null, new ArrayList<>(), null, null, null, null, null, mc, loanProductRelatedDetail);
    }

    public static RepaymentPeriod create(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, Money emi, MathContext mc,
            LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail) {
        final Money zero = emi.zero();
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, fromDate, dueDate, new ArrayList<>(), emi, emi, zero, zero,
                zero, mc, loanProductRelatedDetail);
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        newRepaymentPeriod.interestPeriods.add(InterestPeriod.withEmptyAmounts(newRepaymentPeriod, fromDate, dueDate));
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copy(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.fromDate, repaymentPeriod.dueDate,
                new ArrayList<>(), repaymentPeriod.emi, repaymentPeriod.originalEmi, repaymentPeriod.paidPrincipal,
                repaymentPeriod.paidInterest, repaymentPeriod.futureUnrecognizedInterest, mc,
                repaymentPeriod.getLoanProductRelatedDetail());
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.interestPeriods) {
            newRepaymentPeriod.interestPeriods.add(InterestPeriod.copy(newRepaymentPeriod, interestPeriod, mc));
        }
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copyWithoutPaidAmounts(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final Money zero = repaymentPeriod.emi.zero();
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.fromDate, repaymentPeriod.dueDate,
                new ArrayList<>(), repaymentPeriod.emi, repaymentPeriod.originalEmi, zero, zero, zero, mc,
                repaymentPeriod.getLoanProductRelatedDetail());
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
     * Gives back calculated due interest + credited interest
     *
     * @return
     */
    @NotNull
    public Money getCalculatedDueInterest() {
        if (calculatedDueInterestCalculation == null) {
            calculatedDueInterestCalculation = Memo.of(this::calculateCalculatedDueInterest, () -> new Object[] { this.previous,
                    this.interestPeriods, this.futureUnrecognizedInterest, this.isInterestMoved, this.totalDisbursedAmount });
        }
        return calculatedDueInterestCalculation.get();
    }

    public Money calculateCalculatedDueInterest() {
        Money calculatedDueInterest = getZero(mc);
        if (!isInterestMoved) {
            calculatedDueInterest = Money.of(emi.getCurrencyData(),
                    getInterestPeriods().stream().map(InterestPeriod::getCalculatedDueInterest).reduce(BigDecimal.ZERO, BigDecimal::add),
                    mc);
        }
        calculatedDueInterest = calculatedDueInterest.add(getFutureUnrecognizedInterest(), mc);
        if (getPrevious().isPresent()) {
            calculatedDueInterest = calculatedDueInterest.add(getPrevious().get().getUnrecognizedInterest(), mc);
        }
        return MathUtil.negativeToZero(calculatedDueInterest, mc);
    }

    /**
     * Gives back due interest + credited interest OR paid interest
     *
     * @return
     */
    public Money getDueInterest() {
        if (dueInterestCalculation == null) {
            // Due interest might be the maximum paid if there is pay-off or early repayment
            dueInterestCalculation = Memo.of(
                    () -> MathUtil.max(getPaidPrincipal().isGreaterThan(getCalculatedDuePrincipal()) ? getPaidInterest()
                            : MathUtil.min(getCalculatedDueInterest(), getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest(), false),
                            getPaidInterest(), false),
                    () -> new Object[] { paidPrincipal, paidInterest, interestPeriods, futureUnrecognizedInterest, totalDisbursedAmount,
                            emi });
        }
        return dueInterestCalculation.get();
    }

    /**
     * Gives back an EMI amount which includes credited amounts and future unrecognized interest as well
     *
     * @return
     */
    public Money getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest() {
        return getEmi().plus(getTotalCreditedAmount(), mc).plus(getFutureUnrecognizedInterest(), mc); //
    }

    /**
     * Gives back principal due + charge back principal based on (EMI - Calculated Due Interest)
     *
     * @return
     */
    public Money getCalculatedDuePrincipal() {
        return MathUtil.negativeToZero(getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest().minus(getCalculatedDueInterest(), mc), mc);
    }

    /**
     * Sum of credited principals
     *
     * @return
     */
    public Money getCreditedPrincipal() {
        return MathUtil.negativeToZero(interestPeriods.stream() //
                .map(InterestPeriod::getCreditedPrincipal) //
                .reduce(getZero(mc), (value, previous) -> value.plus(previous, mc)), mc); //
    }

    /**
     * Sum of credited interests
     *
     * @return
     */
    public Money getCreditedInterest() {
        return MathUtil.negativeToZero(interestPeriods.stream() //
                .map(InterestPeriod::getCreditedInterest) //
                .reduce(getZero(mc), (value, previous) -> value.plus(previous, mc)), mc); //
    }

    /**
     * Sum of capitalized income principals
     *
     * @return
     */
    public Money getCapitalizedIncomePrincipal() {
        return MathUtil.negativeToZero(interestPeriods.stream() //
                .map(InterestPeriod::getCapitalizedIncomePrincipal) //
                .reduce(getZero(mc), (value, previous) -> value.plus(previous, mc)), mc); //
    }

    /**
     * Gives back due principal + credited principal or paid principal
     *
     * @return
     */
    public Money getDuePrincipal() {
        // Due principal might be the maximum paid if there is pay-off or early repayment
        return MathUtil.max(
                MathUtil.negativeToZero(getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest().minus(getDueInterest(), mc), mc),
                getPaidPrincipal(), false);
    }

    /**
     * Gives back sum of all credited principal + credited interest
     *
     * @return
     */
    public Money getTotalCreditedAmount() {
        return getCreditedPrincipal().plus(getCreditedInterest(), mc);
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
        return getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest().isEqualTo(getTotalPaidAmount());
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
                        .plus(lastInterestPeriod.getCapitalizedIncomePrincipal(), mc) //
                        .plus(lastInterestPeriod.getDisbursementAmount(), mc) //
                        .minus(getDuePrincipal(), mc)//
                        .plus(getPaidPrincipal(), mc);//
                return MathUtil.negativeToZero(calculatedOutStandingLoanBalance, mc);
            }, () -> new Object[] { paidPrincipal, paidInterest, interestPeriods, totalDisbursedAmount });
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
        Money totalCapitalizedIncomeAmount = getInterestPeriods().stream() //
                .map(InterestPeriod::getCapitalizedIncomePrincipal) //
                .reduce(getZero(mc), (m1, m2) -> m1.plus(m2, mc)); //
        return initialBalance.add(totalDisbursedAmount, mc).add(totalCapitalizedIncomeAmount, mc);
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

    public Money getOutstandingPrincipal() {
        return MathUtil.negativeToZero(getDuePrincipal().minus(getPaidPrincipal()), mc);
    }

    public void resetDerivedComponents() {
        this.paidInterest = paidInterest.zero();
        this.paidPrincipal = paidPrincipal.zero();
    }

    /**
     * @param tillPeriod
     *            can be null. if null it calculates total disbursement including last interest period.
     * @return disbursed and capitalized income amount till interest period.
     */
    public BigDecimal calculateTotalDisbursedAndCapitalizedIncomeAmountTillGivenPeriod(InterestPeriod tillPeriod) {
        BigDecimal res = MathUtil.nullToZero(getTotalDisbursedAmount()).add(MathUtil.nullToZero(getTotalCapitalizedIncomeAmount()));
        for (InterestPeriod interestPeriod : this.interestPeriods) {
            if (interestPeriod.equals(tillPeriod)) {
                break;
            }
            if (!interestPeriod.getDueDate().equals(getFromDate())) {
                if (interestPeriod.getDisbursementAmount() != null) {
                    res = res.add(interestPeriod.getDisbursementAmount().getAmount());
                }
                if (interestPeriod.getCapitalizedIncomePrincipal() != null) {
                    res = res.add(interestPeriod.getCapitalizedIncomePrincipal().getAmount());
                }
            }
        }
        return res;
    }
}
