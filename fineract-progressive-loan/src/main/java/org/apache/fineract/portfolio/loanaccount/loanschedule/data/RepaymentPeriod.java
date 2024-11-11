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
public class RepaymentPeriod {

    private final RepaymentPeriod previous;
    @Getter
    private final LocalDate fromDate;
    @Getter
    private final LocalDate dueDate;
    @Getter
    private final List<InterestPeriod> interestPeriods;
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

    private Memo<BigDecimal> rateFactorPlus1Calculation;
    private Memo<Money> calculatedDueInterestCalculation;
    private Memo<Money> dueInterestCalculation;
    private Memo<Money> outstandingBalanceCalculation;
    private final MathContext mc;

    public RepaymentPeriod(RepaymentPeriod previous, LocalDate fromDate, LocalDate dueDate, Money emi, MathContext mc) {
        this.previous = previous;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.emi = emi;
        this.originalEmi = emi;
        this.mc = mc;
        this.interestPeriods = new ArrayList<>();
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        getInterestPeriods().add(new InterestPeriod(this, getFromDate(), getDueDate(), BigDecimal.ZERO, BigDecimal.ZERO, getZero(mc),
                getZero(mc), getZero(mc), mc));
        this.paidInterest = getZero(mc);
        this.paidPrincipal = getZero(mc);
    }

    public RepaymentPeriod(RepaymentPeriod previous, RepaymentPeriod repaymentPeriod, MathContext mc) {
        this.previous = previous;
        this.fromDate = repaymentPeriod.fromDate;
        this.dueDate = repaymentPeriod.dueDate;
        this.emi = repaymentPeriod.emi;
        this.originalEmi = repaymentPeriod.originalEmi;
        this.interestPeriods = new ArrayList<>();
        this.paidPrincipal = repaymentPeriod.paidPrincipal;
        this.paidInterest = repaymentPeriod.paidInterest;
        this.mc = mc;
        // There is always at least 1 interest period, by default with same from-due date as repayment period
        for (InterestPeriod interestPeriod : repaymentPeriod.interestPeriods) {
            interestPeriods.add(new InterestPeriod(this, interestPeriod));
        }
    }

    public Optional<RepaymentPeriod> getPrevious() {
        return Optional.ofNullable(previous);
    }

    public BigDecimal getRateFactorPlus1() {
        if (rateFactorPlus1Calculation == null) {
            rateFactorPlus1Calculation = Memo.of(this::calculateRateFactorPlus1, () -> this.interestPeriods);
        }
        return rateFactorPlus1Calculation.get();
    }

    private BigDecimal calculateRateFactorPlus1() {
        return interestPeriods.stream().map(InterestPeriod::getRateFactor).reduce(BigDecimal.ONE, BigDecimal::add);
    }

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

    public Money getDueInterest() {
        if (dueInterestCalculation == null) {
            // Due interest might be the maximum paid if there is pay-off or early repayment
            dueInterestCalculation = Memo.of(() -> MathUtil.max(
                    getPaidPrincipal().isGreaterThan(getCalculatedDuePrincipal()) ? getPaidInterest() : getCalculatedDueInterest(),
                    getPaidInterest(), false), () -> new Object[] { paidPrincipal, paidInterest, interestPeriods });
        }
        return dueInterestCalculation.get();
    }

    public Money getCalculatedDuePrincipal() {
        return getEmi().minus(getCalculatedDueInterest(), mc);
    }

    public Money getDuePrincipal() {
        // Due principal might be the maximum paid if there is pay-off or early repayment
        return MathUtil.max(getEmi().minus(getDueInterest(), mc), getPaidPrincipal(), false);
    }

    public Money getTotalPaidAmount() {
        return getPaidPrincipal().plus(getPaidInterest());
    }

    public boolean isFullyPaid() {
        return getEmi().isEqualTo(getTotalPaidAmount());
    }

    public Money getUnrecognizedInterest() {
        return getCalculatedDueInterest().minus(getDueInterest(), mc);
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
        Money totalDisbursedAmount = getInterestPeriods().stream().map(InterestPeriod::getDisbursementAmount).reduce(getZero(mc),
                (m1, m2) -> m1.plus(m2, mc));
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
