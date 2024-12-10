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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

@Data
@Accessors(fluent = true)
public class ProgressiveLoanInterestScheduleModel {

    private final List<RepaymentPeriod> repaymentPeriods;
    private final TreeSet<InterestRate> interestRates;
    private final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;
    private final Integer installmentAmountInMultiplesOf;
    private final MathContext mc;
    private final Money zero;

    public ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, final Integer installmentAmountInMultiplesOf,
            final MathContext mc) {
        this.repaymentPeriods = repaymentPeriods;
        this.interestRates = new TreeSet<>(Collections.reverseOrder());
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.mc = mc;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
    }

    private ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods, final TreeSet<InterestRate> interestRates,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail, final Integer installmentAmountInMultiplesOf,
            final MathContext mc) {
        this.mc = mc;
        this.repaymentPeriods = copyRepaymentPeriods(repaymentPeriods);
        this.interestRates = new TreeSet<>(interestRates);
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
    }

    public ProgressiveLoanInterestScheduleModel deepCopy(MathContext mc) {
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, interestRates, loanProductRelatedDetail,
                installmentAmountInMultiplesOf, mc);
    }

    private List<RepaymentPeriod> copyRepaymentPeriods(final List<RepaymentPeriod> repaymentPeriods) {
        final List<RepaymentPeriod> repaymentCopies = new ArrayList<>(repaymentPeriods.size());
        RepaymentPeriod previousPeriod = null;
        for (RepaymentPeriod repaymentPeriod : repaymentPeriods) {
            RepaymentPeriod currentPeriod = new RepaymentPeriod(previousPeriod, repaymentPeriod, mc);
            previousPeriod = currentPeriod;
            repaymentCopies.add(currentPeriod);
        }
        return repaymentCopies;
    }

    public BigDecimal getInterestRate(final LocalDate effectiveDate) {
        return interestRates.isEmpty() ? loanProductRelatedDetail.getAnnualNominalInterestRate() : findInterestRate(effectiveDate);
    }

    private BigDecimal findInterestRate(final LocalDate effectiveDate) {
        return interestRates.stream() //
                .filter(ir -> !DateUtils.isAfter(ir.effectiveFrom(), effectiveDate)) //
                .map(InterestRate::interestRate) //
                .findFirst() //
                .orElse(loanProductRelatedDetail.getAnnualNominalInterestRate()); //
    }

    public void addInterestRate(final LocalDate newInterestEffectiveDate, final BigDecimal newInterestRate) {
        interestRates.add(new InterestRate(newInterestEffectiveDate, newInterestRate));
    }

    public Optional<RepaymentPeriod> findRepaymentPeriodByDueDate(final LocalDate repaymentPeriodDueDate) {
        if (repaymentPeriodDueDate == null) {
            return Optional.empty();
        }
        return repaymentPeriods.stream()//
                .filter(repaymentPeriodItem -> DateUtils.isEqual(repaymentPeriodItem.getDueDate(), repaymentPeriodDueDate))//
                .findFirst();
    }

    public List<RepaymentPeriod> getRelatedRepaymentPeriods(final LocalDate calculateFromRepaymentPeriodDueDate) {
        if (calculateFromRepaymentPeriodDueDate == null) {
            return repaymentPeriods;
        }
        return repaymentPeriods.stream()//
                .filter(period -> !DateUtils.isBefore(period.getDueDate(), calculateFromRepaymentPeriodDueDate))//
                .toList();//
    }

    public int getLoanTermInDays() {
        if (repaymentPeriods.isEmpty()) {
            return 0;
        }
        final RepaymentPeriod firstPeriod = repaymentPeriods.get(0);
        final RepaymentPeriod lastPeriod = repaymentPeriods.size() > 1 ? repaymentPeriods.get(repaymentPeriods.size() - 1) : firstPeriod;
        return DateUtils.getExactDifferenceInDays(firstPeriod.getFromDate(), lastPeriod.getDueDate());
    }

    public LocalDate getStartDate() {
        return !repaymentPeriods.isEmpty() ? repaymentPeriods.get(0).getFromDate() : null;
    }

    public LocalDate getMaturityDate() {
        return !repaymentPeriods.isEmpty() ? repaymentPeriods.get(repaymentPeriods.size() - 1).getDueDate() : null;
    }

    public Optional<RepaymentPeriod> changeOutstandingBalanceAndUpdateInterestPeriods(final LocalDate balanceChangeDate,
            final Money disbursedAmount, final Money correctionAmount) {
        return findRepaymentPeriodForBalanceChange(balanceChangeDate).stream()//
                .peek(updateInterestPeriodOnRepaymentPeriod(balanceChangeDate, disbursedAmount, correctionAmount))//
                .findFirst();//
    }

    Optional<RepaymentPeriod> findRepaymentPeriodForBalanceChange(final LocalDate balanceChangeDate) {
        if (balanceChangeDate == null) {
            return Optional.empty();
        }
        // TODO use isInPeriod
        return repaymentPeriods.stream()//
                .filter(repaymentPeriod -> {
                    final boolean isFirstPeriod = repaymentPeriod.getPrevious().isEmpty();
                    if (isFirstPeriod) {
                        return !balanceChangeDate.isBefore(repaymentPeriod.getFromDate())
                                && !balanceChangeDate.isAfter(repaymentPeriod.getDueDate());
                    } else {
                        return balanceChangeDate.isAfter(repaymentPeriod.getFromDate())
                                && !balanceChangeDate.isAfter(repaymentPeriod.getDueDate());
                    }
                })//
                .findFirst();
    }

    private Consumer<RepaymentPeriod> updateInterestPeriodOnRepaymentPeriod(final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount) {
        return repaymentPeriod -> {
            final Optional<InterestPeriod> interestPeriodOptional = findInterestPeriodForBalanceChange(repaymentPeriod, balanceChangeDate);
            if (interestPeriodOptional.isPresent()) {
                interestPeriodOptional.get().addDisbursementAmount(disbursedAmount);
                interestPeriodOptional.get().addBalanceCorrectionAmount(correctionAmount);
            } else {
                insertInterestPeriod(repaymentPeriod, balanceChangeDate, disbursedAmount, correctionAmount);
            }
        };
    }

    Optional<InterestPeriod> findInterestPeriodForBalanceChange(final RepaymentPeriod repaymentPeriod, final LocalDate balanceChangeDate) {
        if (repaymentPeriod == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> balanceChangeDate.isEqual(interestPeriod.getDueDate()))//
                .findFirst();
    }

    void insertInterestPeriod(final RepaymentPeriod repaymentPeriod, final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount) {
        final InterestPeriod previousInterestPeriod;
        if (balanceChangeDate.isAfter(repaymentPeriod.getFromDate())) {
            previousInterestPeriod = repaymentPeriod.getInterestPeriods().get(repaymentPeriod.getInterestPeriods().size() - 1);//
        } else {
            previousInterestPeriod = repaymentPeriod.getInterestPeriods().stream()
                    .filter(ip -> balanceChangeDate.isAfter(ip.getFromDate()) && !balanceChangeDate.isAfter(ip.getDueDate()))//
                    .reduce((first, second) -> second)//
                    .orElse(repaymentPeriod.getInterestPeriods().get(0));
        }
        LocalDate originalDueDate = previousInterestPeriod.getDueDate();
        LocalDate newDueDate = balanceChangeDate.isBefore(previousInterestPeriod.getFromDate()) ? previousInterestPeriod.getFromDate()
                : balanceChangeDate.isAfter(previousInterestPeriod.getDueDate()) ? previousInterestPeriod.getDueDate() : balanceChangeDate;
        previousInterestPeriod.setDueDate(newDueDate);
        previousInterestPeriod.addDisbursementAmount(disbursedAmount);
        previousInterestPeriod.addBalanceCorrectionAmount(correctionAmount);
        final InterestPeriod interestPeriod = new InterestPeriod(repaymentPeriod, previousInterestPeriod.getDueDate(), originalDueDate,
                BigDecimal.ZERO, BigDecimal.ZERO, zero, zero, zero, mc);
        repaymentPeriod.getInterestPeriods().add(interestPeriod);
    }

    public Money getTotalDueInterest() {
        return repaymentPeriods().stream().flatMap(rp -> rp.getInterestPeriods().stream().map(InterestPeriod::getCalculatedDueInterest))
                .reduce(zero(), Money::plus);
    }

    public Money getTotalDuePrincipal() {
        return repaymentPeriods.stream().flatMap(rp -> rp.getInterestPeriods().stream().map(InterestPeriod::getDisbursementAmount))
                .reduce(zero(), Money::plus);
    }

    public Money getTotalPaidInterest() {
        return repaymentPeriods().stream().map(RepaymentPeriod::getPaidInterest).reduce(zero, Money::plus);
    }

    public Money getTotalPaidPrincipal() {
        return repaymentPeriods().stream().map(RepaymentPeriod::getPaidPrincipal).reduce(zero, Money::plus);
    }

    public Optional<RepaymentPeriod> findRepaymentPeriod(@NotNull LocalDate transactionDate) {
        return repaymentPeriods.stream() //
                .filter(period -> isInPeriod(transactionDate, period.getFromDate(), period.getDueDate(), period.isFirstRepaymentPeriod()))//
                .findFirst();
    }
}
