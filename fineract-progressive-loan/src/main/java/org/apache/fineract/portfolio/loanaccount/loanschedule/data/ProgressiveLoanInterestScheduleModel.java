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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

@Data
@Accessors(fluent = true)
public class ProgressiveLoanInterestScheduleModel {

    private final List<RepaymentPeriod> repaymentPeriods;
    private final List<InterestRate> interestRates;
    private final LoanProductRelatedDetail loanProductRelatedDetail;
    private final Integer installmentAmountInMultiplesOf;

    public ProgressiveLoanInterestScheduleModel(List<RepaymentPeriod> repaymentPeriods, LoanProductRelatedDetail loanProductRelatedDetail,
            Integer installmentAmountInMultiplesOf) {
        this.repaymentPeriods = repaymentPeriods;
        this.interestRates = new ArrayList<>();
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
    }

    private ProgressiveLoanInterestScheduleModel(List<RepaymentPeriod> repaymentPeriods, final List<InterestRate> interestRates,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf) {
        this.repaymentPeriods = copyRepaymentPeriods(repaymentPeriods);
        this.interestRates = new ArrayList<>(interestRates);
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
    }

    public ProgressiveLoanInterestScheduleModel deepCopy() {
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, interestRates, loanProductRelatedDetail,
                installmentAmountInMultiplesOf);
    }

    private List<RepaymentPeriod> copyRepaymentPeriods(final List<RepaymentPeriod> repaymentPeriods) {
        final List<RepaymentPeriod> repaymentCopies = new ArrayList<>(repaymentPeriods.size());
        RepaymentPeriod previousPeriod = null;
        for (RepaymentPeriod repaymentPeriod : repaymentPeriods) {
            RepaymentPeriod currentPeriod = new RepaymentPeriod(previousPeriod, repaymentPeriod);
            previousPeriod = currentPeriod;
            repaymentCopies.add(currentPeriod);
        }
        return repaymentCopies;
    }

    public BigDecimal getInterestRate(final LocalDate effectiveDate) {
        return interestRates.isEmpty() ? loanProductRelatedDetail.getAnnualNominalInterestRate() : findInterestRate(effectiveDate);
    }

    private BigDecimal findInterestRate(final LocalDate effectiveDate) {
        return interestRates.stream().filter(ir -> !ir.effectiveFrom().isAfter(effectiveDate)).map(InterestRate::interestRate).findFirst()
                .orElse(loanProductRelatedDetail.getAnnualNominalInterestRate());
    }

    public Optional<RepaymentPeriod> findRepaymentPeriod(final LocalDate repaymentPeriodDueDate) {
        if (repaymentPeriodDueDate == null) {
            return Optional.empty();
        }
        return repaymentPeriods.stream()//
                .filter(repaymentPeriodItem -> repaymentPeriodItem.getDueDate().isEqual(repaymentPeriodDueDate))//
                .findFirst();
    }

    public List<RepaymentPeriod> getRelatedRepaymentPeriods(final LocalDate calculateFromRepaymentPeriodDueDate) {
        if (calculateFromRepaymentPeriodDueDate == null) {
            return repaymentPeriods;
        }
        return repaymentPeriods.stream()//
                .filter(period -> !period.getDueDate().isBefore(calculateFromRepaymentPeriodDueDate))//
                .toList();//
    }

    public int getLoanTermInDays() {
        if (repaymentPeriods.isEmpty()) {
            return 0;
        }
        final RepaymentPeriod firstPeriod = repaymentPeriods.get(0);
        final RepaymentPeriod lastPeriod = repaymentPeriods.size() > 1 ? repaymentPeriods.get(repaymentPeriods.size() - 1) : firstPeriod;
        return Math.toIntExact(ChronoUnit.DAYS.between(firstPeriod.getFromDate(), lastPeriod.getDueDate()));
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
        return repaymentPeriods.stream()//
                .filter(repaymentPeriod -> {
                    if (repaymentPeriod.getPrevious().isPresent()) {
                        return balanceChangeDate.isAfter(repaymentPeriod.getFromDate())
                                && !balanceChangeDate.isAfter(repaymentPeriod.getDueDate());
                    } else {
                        return !balanceChangeDate.isBefore(repaymentPeriod.getFromDate())
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
                BigDecimal.ZERO, getZero(), getZero(), getZero());
        repaymentPeriod.getInterestPeriods().add(interestPeriod);
    }

    private Money getZero() {
        return Money.zero(loanProductRelatedDetail.getCurrency());
    }
}
