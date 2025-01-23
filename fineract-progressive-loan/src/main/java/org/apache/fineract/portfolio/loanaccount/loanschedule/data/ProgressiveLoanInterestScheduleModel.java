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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsDataWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

@Data
@Accessors(fluent = true)
public class ProgressiveLoanInterestScheduleModel {

    private final List<RepaymentPeriod> repaymentPeriods;
    private final TreeSet<InterestRate> interestRates;
    private final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;
    private final LoanTermVariationsDataWrapper loanTermVariations;
    private final Integer installmentAmountInMultiplesOf;
    private final MathContext mc;
    private final Money zero;

    public ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final LoanTermVariationsDataWrapper loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        this.repaymentPeriods = repaymentPeriods;
        this.interestRates = new TreeSet<>(Collections.reverseOrder());
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.loanTermVariations = loanTermVariations;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.mc = mc;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
    }

    private ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods, final TreeSet<InterestRate> interestRates,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final LoanTermVariationsDataWrapper loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        this.mc = mc;
        this.repaymentPeriods = copyRepaymentPeriods(repaymentPeriods,
                (previousPeriod, repaymentPeriod) -> new RepaymentPeriod(previousPeriod, repaymentPeriod, mc));
        this.interestRates = new TreeSet<>(interestRates);
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.loanTermVariations = loanTermVariations;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
    }

    public ProgressiveLoanInterestScheduleModel deepCopy(MathContext mc) {
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, interestRates, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc);
    }

    public ProgressiveLoanInterestScheduleModel emptyCopy() {
        final List<RepaymentPeriod> repaymentPeriodCopies = copyRepaymentPeriods(repaymentPeriods,
                (previousPeriod, repaymentPeriod) -> new RepaymentPeriod(previousPeriod, repaymentPeriod.getFromDate(),
                        repaymentPeriod.getDueDate(), repaymentPeriod.getEmi().zero(), mc));
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriodCopies, interestRates, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc);
    }

    private List<RepaymentPeriod> copyRepaymentPeriods(final List<RepaymentPeriod> repaymentPeriods,
            final BiFunction<RepaymentPeriod, RepaymentPeriod, RepaymentPeriod> repaymentCopyFunction) {
        final List<RepaymentPeriod> repaymentCopies = new ArrayList<>(repaymentPeriods.size());
        RepaymentPeriod previousPeriod = null;
        for (RepaymentPeriod repaymentPeriod : repaymentPeriods) {
            RepaymentPeriod currentPeriod = repaymentCopyFunction.apply(previousPeriod, repaymentPeriod);
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

    public Optional<RepaymentPeriod> updateInterestPeriodsForInterestPause(final LocalDate fromDate, final LocalDate endDate) {
        if (fromDate == null || endDate == null) {
            return Optional.empty();
        }

        final List<RepaymentPeriod> affectedPeriods = repaymentPeriods.stream()//
                .filter(period -> isPeriodInRange(period, fromDate, endDate))//
                .toList();
        affectedPeriods.forEach(period -> insertInterestPausePeriods(period, fromDate, endDate));

        return affectedPeriods.stream().findFirst();
    }

    private boolean isPeriodInRange(final RepaymentPeriod repaymentPeriod, final LocalDate fromDate, final LocalDate endDate) {
        return DateUtils.isDateInRangeFromExclusiveToInclusive(fromDate, repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate())
                || DateUtils.isDateInRangeFromExclusiveToInclusive(endDate, repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate());
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

    private Optional<InterestPeriod> findInterestPeriodForBalanceChange(final RepaymentPeriod repaymentPeriod,
            final LocalDate balanceChangeDate) {
        if (repaymentPeriod == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> balanceChangeDate.isEqual(interestPeriod.getDueDate()))//
                .findFirst();
    }

    void insertInterestPeriod(final RepaymentPeriod repaymentPeriod, final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount) {
        final InterestPeriod previousInterestPeriod = findPreviousInterestPeriod(repaymentPeriod, balanceChangeDate);
        final LocalDate originalDueDate = previousInterestPeriod.getDueDate();
        final LocalDate newDueDate = calculateNewDueDate(previousInterestPeriod, balanceChangeDate);

        previousInterestPeriod.setDueDate(newDueDate);
        previousInterestPeriod.addDisbursementAmount(disbursedAmount);
        previousInterestPeriod.addBalanceCorrectionAmount(correctionAmount);

        final InterestPeriod interestPeriod = new InterestPeriod(repaymentPeriod, newDueDate, originalDueDate, BigDecimal.ZERO,
                BigDecimal.ZERO, zero, zero, zero, mc, false);
        repaymentPeriod.getInterestPeriods().add(interestPeriod);
    }

    private void insertInterestPausePeriods(final RepaymentPeriod repaymentPeriod, final LocalDate fromDate, final LocalDate endDate) {
        final InterestPeriod previousInterestPeriod = findPreviousInterestPeriod(repaymentPeriod, fromDate);
        final LocalDate originalFromDate = previousInterestPeriod.getFromDate();
        final LocalDate originalDueDate = previousInterestPeriod.getDueDate();
        final LocalDate newDueDate = calculateNewDueDate(previousInterestPeriod, fromDate.minusDays(1));

        if (fromDate.isAfter(originalFromDate) && endDate.isBefore(originalDueDate)) {
            previousInterestPeriod.setDueDate(newDueDate);
            final InterestPeriod interestPausePeriod = new InterestPeriod(repaymentPeriod, newDueDate, endDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, zero, zero, zero, mc, true);
            repaymentPeriod.getInterestPeriods().add(interestPausePeriod);
            final InterestPeriod interestAfterPausePeriod = new InterestPeriod(repaymentPeriod, endDate, originalDueDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, zero, zero, zero, mc, false);
            repaymentPeriod.getInterestPeriods().add(interestAfterPausePeriod);
        }

        if (fromDate.isAfter(originalFromDate) && endDate.isAfter(originalDueDate)) {
            previousInterestPeriod.setDueDate(newDueDate);
            final InterestPeriod interestPausePeriod = new InterestPeriod(repaymentPeriod, newDueDate, originalDueDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, zero, zero, zero, mc, true);
            repaymentPeriod.getInterestPeriods().add(interestPausePeriod);
        }

        if (fromDate.isBefore(originalFromDate) && endDate.isBefore(originalDueDate)) {
            repaymentPeriod.getInterestPeriods().clear();
            final InterestPeriod interestPausePeriod = new InterestPeriod(repaymentPeriod, newDueDate, originalDueDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, zero, zero, zero, mc, true);
            repaymentPeriod.getInterestPeriods().add(interestPausePeriod);
            InterestPeriod interestAfterPausePeriod = new InterestPeriod(repaymentPeriod, endDate, originalDueDate, BigDecimal.ZERO,
                    BigDecimal.ZERO, zero, zero, zero, mc, false);
            repaymentPeriod.getInterestPeriods().add(interestAfterPausePeriod);
        }
    }

    private InterestPeriod findPreviousInterestPeriod(final RepaymentPeriod repaymentPeriod, final LocalDate date) {
        if (date.isAfter(repaymentPeriod.getFromDate())) {
            return repaymentPeriod.getInterestPeriods().get(repaymentPeriod.getInterestPeriods().size() - 1);
        } else {
            return repaymentPeriod.getInterestPeriods().stream()
                    .filter(ip -> date.isAfter(ip.getFromDate()) && !date.isAfter(ip.getDueDate())).reduce((first, second) -> second)
                    .orElse(repaymentPeriod.getInterestPeriods().get(0));
        }
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

    public boolean isEmpty() {
        return repaymentPeriods.stream() //
                .filter(rp -> !rp.getEmi().isZero()) //
                .findFirst() //
                .isEmpty(); //
    }

    /**
     * This method gives you repayment pairs to copy attributes.
     *
     * @param periodFromDueDate
     *            Copy from this due periods.
     * @param copyFromPeriods
     *            Copy source
     * @param copyConsumer
     *            Consumer to copy attributes. Params: (from, to)
     */
    public void copyPeriodsFrom(final LocalDate periodFromDueDate, List<RepaymentPeriod> copyFromPeriods,
            BiConsumer<RepaymentPeriod, RepaymentPeriod> copyConsumer) {
        if (copyFromPeriods.isEmpty()) {
            return;
        }
        final Iterator<RepaymentPeriod> actualIterator = repaymentPeriods.iterator();
        final Iterator<RepaymentPeriod> copyFromIterator = copyFromPeriods.iterator();
        while (actualIterator.hasNext()) {
            final RepaymentPeriod copyFromPeriod = copyFromIterator.next();
            RepaymentPeriod actualPeriod = actualIterator.next();
            while (actualIterator.hasNext() && !copyFromPeriod.getDueDate().isEqual(actualPeriod.getDueDate())) {
                actualPeriod = actualIterator.next();
            }
            if (!actualPeriod.getDueDate().isBefore(periodFromDueDate)) {
                copyConsumer.accept(copyFromPeriod, actualPeriod);
            }
        }
    }

    private LocalDate calculateNewDueDate(final InterestPeriod previousInterestPeriod, final LocalDate date) {
        return date.isBefore(previousInterestPeriod.getFromDate()) ? previousInterestPeriod.getFromDate()
                : date.isAfter(previousInterestPeriod.getDueDate()) ? previousInterestPeriod.getDueDate() : date;
    }

}
