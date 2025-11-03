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
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.apache.fineract.portfolio.util.Memo;

@ToString(exclude = { "previous" })
@EqualsAndHashCode(exclude = { "previous" })
public class RepaymentPeriod {

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
    private Money emi;
    @Setter
    private Money originalEmi;
    private Money paidPrincipal;
    private Money paidInterest;
    @Setter
    private Money futureUnrecognizedInterest;

    @JsonExclude
    @Getter
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
    private Money totalDisbursedAmount;

    @Setter
    private Money totalCapitalizedIncomeAmount;
    @JsonExclude
    @Getter
    private final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;
    @JsonExclude
    private MonetaryCurrency currency;

    protected RepaymentPeriod(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, List<InterestPeriod> interestPeriods,
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
        newRepaymentPeriod.getInterestPeriods().add(InterestPeriod.withEmptyAmounts(newRepaymentPeriod, fromDate, dueDate));
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copy(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.getFromDate(),
                repaymentPeriod.getDueDate(), new ArrayList<>(), repaymentPeriod.getEmi(), repaymentPeriod.getOriginalEmi(),
                repaymentPeriod.getPaidPrincipal(), repaymentPeriod.getPaidInterest(), repaymentPeriod.getFutureUnrecognizedInterest(), mc,
                repaymentPeriod.getLoanProductRelatedDetail());
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.getInterestPeriods()) {
            newRepaymentPeriod.getInterestPeriods().add(InterestPeriod.copy(newRepaymentPeriod, interestPeriod, mc));
        }
        return newRepaymentPeriod;
    }

    public static RepaymentPeriod copyWithoutPaidAmounts(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        final Money zero = Money.zero(repaymentPeriod.getCurrency(), mc);
        final RepaymentPeriod newRepaymentPeriod = new RepaymentPeriod(previous, repaymentPeriod.getFromDate(),
                repaymentPeriod.getDueDate(), new ArrayList<>(), repaymentPeriod.getEmi(), repaymentPeriod.getOriginalEmi(), zero, zero,
                zero, mc, repaymentPeriod.getLoanProductRelatedDetail());
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.getInterestPeriods()) {
            var interestPeriodCopy = InterestPeriod.copy(newRepaymentPeriod, interestPeriod);
            if (!interestPeriodCopy.getBalanceCorrectionAmount().isZero()) {
                interestPeriodCopy.addBalanceCorrectionAmount(interestPeriodCopy.getBalanceCorrectionAmount().negated());
            }
            newRepaymentPeriod.getInterestPeriods().add(interestPeriodCopy);
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
        Money calculatedDueInterest = getZero();
        if (!isInterestMoved()) {
            calculatedDueInterest = Money.of(getEmi().getCurrencyData(),
                    getInterestPeriods().stream().map(InterestPeriod::getCalculatedDueInterest).reduce(BigDecimal.ZERO, BigDecimal::add),
                    mc);
        }
        calculatedDueInterest = calculatedDueInterest.add(getFutureUnrecognizedInterest(), getMc());
        if (getPrevious().isPresent()) {
            calculatedDueInterest = calculatedDueInterest.add(getPrevious().get().getUnrecognizedInterest(), getMc());
        }
        return MathUtil.negativeToZero(calculatedDueInterest, getMc());
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
        return getEmi().plus(getTotalCreditedAmount(), mc).plus(getFutureUnrecognizedInterest(), getMc()); //
    }

    /**
     * Gives back principal due + charge back principal based on (EMI - Calculated Due Interest)
     *
     * @return
     */
    public Money getCalculatedDuePrincipal() {
        return MathUtil.negativeToZero(getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest().minus(getCalculatedDueInterest(), getMc()),
                getMc());
    }

    /**
     * Sum of credited principals
     *
     * @return
     */
    public Money getCreditedPrincipal() {
        return MathUtil.negativeToZero(getInterestPeriods().stream() //
                .map(InterestPeriod::getCreditedPrincipal) //
                .reduce(getZero(), (value, previous) -> value.plus(previous, getMc())), getMc()); //
    }

    /**
     * Sum of credited interests
     *
     * @return
     */
    public Money getCreditedInterest() {
        return MathUtil.negativeToZero(getInterestPeriods().stream() //
                .map(InterestPeriod::getCreditedInterest) //
                .reduce(getZero(), (value, previous) -> value.plus(previous, getMc())), getMc()); //
    }

    /**
     * Sum of capitalized income principals
     *
     * @return
     */
    public Money getCapitalizedIncomePrincipal() {
        return MathUtil.negativeToZero(getInterestPeriods().stream() //
                .map(InterestPeriod::getCapitalizedIncomePrincipal) //
                .reduce(getZero(), (value, previous) -> value.plus(previous, getMc())), getMc()); //
    }

    /**
     * Gives back due principal + credited principal or paid principal
     *
     * @return
     */
    public Money getDuePrincipal() {
        // Due principal might be the maximum paid if there is pay-off or early repayment
        return MathUtil.max(MathUtil
                .negativeToZero(getEmiPlusCreditedAmountsPlusFutureUnrecognizedInterest().minus(getDueInterest(), getMc()), getMc()),
                getPaidPrincipal(), false);
    }

    /**
     * Gives back sum of all credited principal + credited interest
     *
     * @return
     */
    public Money getTotalCreditedAmount() {
        return getCreditedPrincipal().plus(getCreditedInterest(), getMc());
    }

    /**
     * Total paid amounts has everything: paid principal + paid interest + paid charge principal + paid charge interest
     *
     * @return
     */
    public Money getTotalPaidAmount() {
        return getPaidPrincipal().plus(getPaidInterest(), getMc());
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
        return getCalculatedDueInterest().minus(getDueInterest(), getMc());
    }

    public Money getCreditedAmounts() {
        return interestPeriods.stream().map(InterestPeriod::getCreditedAmounts).reduce(getZero(), (m1, m2) -> m1.plus(m2, getMc()));
    }

    public Money getOutstandingLoanBalance() {
        if (outstandingBalanceCalculation == null) {
            outstandingBalanceCalculation = Memo.of(() -> {
                InterestPeriod lastInterestPeriod = getInterestPeriods().getLast();
                Money calculatedOutStandingLoanBalance = lastInterestPeriod.getOutstandingLoanBalance() //
                        .plus(lastInterestPeriod.getBalanceCorrectionAmount(), getMc()) //
                        .plus(lastInterestPeriod.getCapitalizedIncomePrincipal(), getMc()) //
                        .plus(lastInterestPeriod.getDisbursementAmount(), getMc()) //
                        .plus(getPaidPrincipal(), getMc()) //
                        .minus(getDuePrincipal(), getMc()); //
                return MathUtil.negativeToZero(calculatedOutStandingLoanBalance, getMc());
            }, () -> new Object[] { paidPrincipal, paidInterest, interestPeriods, totalDisbursedAmount });
        }
        return outstandingBalanceCalculation.get();
    }

    public void addPaidPrincipalAmount(Money paidPrincipal) {
        this.paidPrincipal = MathUtil.plus(this.getPaidPrincipal(), paidPrincipal, getMc());
    }

    public void addPaidInterestAmount(Money paidInterest) {
        this.paidInterest = MathUtil.plus(this.getPaidInterest(), paidInterest, getMc());
    }

    public Money getInitialBalanceForEmiRecalculation() {
        Money initialBalance;
        if (getPrevious().isPresent()) {
            initialBalance = getPrevious().get().getOutstandingLoanBalance();
        } else {
            initialBalance = getZero();
        }
        Money totalDisbursedAmount = getInterestPeriods().stream() //
                .map(InterestPeriod::getDisbursementAmount) //
                .reduce(getZero(), (m1, m2) -> m1.plus(m2, getMc())); //
        Money totalCapitalizedIncomeAmount = getInterestPeriods().stream() //
                .map(InterestPeriod::getCapitalizedIncomePrincipal) //
                .reduce(getZero(), (m1, m2) -> m1.plus(m2, getMc())); //
        return initialBalance.add(totalDisbursedAmount, getMc()).add(totalCapitalizedIncomeAmount, getMc());
    }

    public Money getZero() {
        return Money.zero(getCurrency(), getMc());
    }

    public InterestPeriod getFirstInterestPeriod() {
        return getInterestPeriods().getFirst();
    }

    public InterestPeriod getLastInterestPeriod() {
        List<InterestPeriod> interestPeriods = getInterestPeriods();
        return interestPeriods.getLast();
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

    /**
     * Gives back getDueInterest minus paid interest
     *
     * @return
     */
    public Money getOutstandingInterest() {
        return MathUtil.negativeToZero(getDueInterest().minus(getPaidInterest()), getMc());
    }

    public Money getOutstandingPrincipal() {
        return MathUtil.negativeToZero(getDuePrincipal().minus(getPaidPrincipal()), getMc());
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
    public Money calculateTotalDisbursedAndCapitalizedIncomeAmountTillGivenPeriod(InterestPeriod tillPeriod) {
        Money res = MathUtil.plus(getMc(), getTotalDisbursedAmount(), getTotalCapitalizedIncomeAmount());
        for (InterestPeriod interestPeriod : this.getInterestPeriods()) {
            if (interestPeriod.equals(tillPeriod)) {
                break;
            }
            if (!interestPeriod.getDueDate().equals(getFromDate())) {
                if (interestPeriod.getDisbursementAmount() != null) {
                    res = res.plus(interestPeriod.getDisbursementAmount(), getMc());
                }
                if (interestPeriod.getCapitalizedIncomePrincipal() != null) {
                    res = res.plus(interestPeriod.getCapitalizedIncomePrincipal(), getMc());
                }
            }
        }
        return res;
    }

    public MonetaryCurrency getCurrency() {
        if (currency == null) {
            currency = MonetaryCurrency.fromCurrencyData(loanProductRelatedDetail.getCurrencyData());
        }
        return currency;
    }

    public Money getEmi() {
        return MathUtil.nullToZero(emi, getCurrency(), getMc());
    }

    public Money getOriginalEmi() {
        return MathUtil.nullToZero(originalEmi, getCurrency(), getMc());
    }

    public Money getPaidPrincipal() {
        return MathUtil.nullToZero(paidPrincipal, getCurrency(), getMc());
    }

    public Money getPaidInterest() {
        return MathUtil.nullToZero(paidInterest, getCurrency(), getMc());
    }

    public Money getFutureUnrecognizedInterest() {
        return MathUtil.nullToZero(futureUnrecognizedInterest, getCurrency(), getMc());
    }

    public Money getTotalDisbursedAmount() {
        return MathUtil.nullToZero(totalDisbursedAmount, getCurrency(), getMc());
    }

    public Money getTotalCapitalizedIncomeAmount() {
        return MathUtil.nullToZero(totalCapitalizedIncomeAmount, getCurrency(), getMc());
    }
}
