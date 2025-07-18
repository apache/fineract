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
import static org.apache.fineract.portfolio.loanproduct.calc.data.LoanInterestScheduleModelModifiers.COPY;
import static org.apache.fineract.portfolio.loanproduct.calc.data.LoanInterestScheduleModelModifiers.EMI_RECALCULATION;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.serialization.gson.JsonExclude;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class ProgressiveLoanInterestScheduleModel {

    private final List<RepaymentPeriod> repaymentPeriods;
    private final TreeSet<InterestRate> interestRates;
    @JsonExclude
    private final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;
    private final Map<LoanTermVariationType, List<LoanTermVariationsData>> loanTermVariations;
    private final Integer installmentAmountInMultiplesOf;
    @JsonExclude
    private final MathContext mc;
    @JsonExclude
    private final Money zero;
    private final Map<LoanInterestScheduleModelModifiers, Boolean> modifiers;

    @Setter
    private LocalDate lastOverdueBalanceChange;

    public ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final List<LoanTermVariationsData> loanTermVariations, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        this.repaymentPeriods = new ArrayList<>(repaymentPeriods);
        this.interestRates = new TreeSet<>(Collections.reverseOrder());
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.loanTermVariations = buildLoanTermVariationMap(loanTermVariations);
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.mc = mc;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
        modifiers = new HashMap<>(Map.of(EMI_RECALCULATION, true, COPY, false));
    }

    private ProgressiveLoanInterestScheduleModel(final List<RepaymentPeriod> repaymentPeriods, final TreeSet<InterestRate> interestRates,
            final LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail,
            final Map<LoanTermVariationType, List<LoanTermVariationsData>> loanTermVariations, final Integer installmentAmountInMultiplesOf,
            final MathContext mc, final boolean isCopiedForCalculation) {
        this.mc = mc;
        this.repaymentPeriods = copyRepaymentPeriods(repaymentPeriods,
                (previousPeriod, repaymentPeriod) -> RepaymentPeriod.copy(previousPeriod, repaymentPeriod, mc));
        this.interestRates = new TreeSet<>(interestRates);
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.loanTermVariations = loanTermVariations;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.zero = Money.zero(loanProductRelatedDetail.getCurrencyData(), mc);
        modifiers = new HashMap<>(Map.of(EMI_RECALCULATION, true, COPY, isCopiedForCalculation));
    }

    public ProgressiveLoanInterestScheduleModel deepCopy(MathContext mc) {
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriods, interestRates, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc, false);
    }

    public ProgressiveLoanInterestScheduleModel copyWithoutPaidAmounts() {
        final List<RepaymentPeriod> repaymentPeriodCopies = copyRepaymentPeriods(repaymentPeriods,
                (previousPeriod, repaymentPeriod) -> RepaymentPeriod.copyWithoutPaidAmounts(previousPeriod, repaymentPeriod, mc));
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriodCopies, interestRates, loanProductRelatedDetail, loanTermVariations,
                installmentAmountInMultiplesOf, mc, true);
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
        final RepaymentPeriod firstPeriod = repaymentPeriods.getFirst();
        final RepaymentPeriod lastPeriod = repaymentPeriods.size() > 1 ? getLastRepaymentPeriod() : firstPeriod;
        return DateUtils.getExactDifferenceInDays(firstPeriod.getFromDate(), lastPeriod.getDueDate());
    }

    public LocalDate getStartDate() {
        return !repaymentPeriods.isEmpty() ? repaymentPeriods.getFirst().getFromDate() : null;
    }

    public LocalDate getMaturityDate() {
        return !repaymentPeriods.isEmpty() ? getLastRepaymentPeriod().getDueDate() : null;
    }

    public Optional<RepaymentPeriod> changeOutstandingBalanceAndUpdateInterestPeriods(final LocalDate balanceChangeDate,
            final Money disbursedAmount, final Money correctionAmount, final Money capitalizedIncomePrincipal) {
        return findRepaymentPeriodForBalanceChange(balanceChangeDate).stream()//
                .peek(updateInterestPeriodOnRepaymentPeriod(balanceChangeDate, disbursedAmount, correctionAmount,
                        capitalizedIncomePrincipal))//
                .findFirst();//
    }

    public Optional<RepaymentPeriod> updateInterestPeriodsForInterestPause(final LocalDate fromDate, final LocalDate endDate) {
        if (fromDate == null || endDate == null) {
            return Optional.empty();
        }

        final List<RepaymentPeriod> affectedPeriods = repaymentPeriods.stream()//
                .filter(period -> period.getFromDate().isBefore(endDate) && !period.getDueDate().isBefore(fromDate))//
                .toList();
        affectedPeriods.forEach(period -> insertInterestPausePeriods(period, fromDate, endDate));

        return affectedPeriods.stream().findFirst();
    }

    Optional<RepaymentPeriod> findRepaymentPeriodForBalanceChange(final LocalDate balanceChangeDate) {
        if (balanceChangeDate == null) {
            return Optional.empty();
        }
        return repaymentPeriods.stream()//
                .filter(period -> isInPeriod(balanceChangeDate, period.getFromDate(), period.getDueDate(), period.isFirstRepaymentPeriod()))//
                .findFirst();
    }

    private Consumer<RepaymentPeriod> updateInterestPeriodOnRepaymentPeriod(final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount, final Money capitalizedIncomePrincipal) {
        return repaymentPeriod -> {
            final boolean isChangeOnMaturityDate = isLastRepaymentPeriod(repaymentPeriod)
                    && balanceChangeDate.isEqual(repaymentPeriod.getDueDate());
            final Optional<InterestPeriod> interestPeriodOptional = findInterestPeriodForBalanceChange(repaymentPeriod, balanceChangeDate,
                    isChangeOnMaturityDate);
            if (interestPeriodOptional.isPresent()) {
                interestPeriodOptional.get().addDisbursementAmount(disbursedAmount);
                interestPeriodOptional.get().addCapitalizedIncomePrincipalAmount(capitalizedIncomePrincipal);
                interestPeriodOptional.get().addBalanceCorrectionAmount(correctionAmount);
            } else {
                insertInterestPeriod(repaymentPeriod, balanceChangeDate, disbursedAmount, correctionAmount, capitalizedIncomePrincipal);
            }
        };
    }

    private Optional<InterestPeriod> findInterestPeriodForBalanceChange(final RepaymentPeriod repaymentPeriod,
            final LocalDate balanceChangeDate, final boolean isChangeOnMaturityDate) {
        if (repaymentPeriod == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        // We want to create a 0 length interest period (if not existed yet) for any credit activity occurs on maturity
        // date
        if (isChangeOnMaturityDate) {
            var lastInterestPeriod = repaymentPeriod.getLastInterestPeriod();
            return lastInterestPeriod.getLength() == 0 ? Optional.of(lastInterestPeriod) : Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> balanceChangeDate.isEqual(interestPeriod.getDueDate()))//
                .findFirst();
    }

    void insertInterestPeriod(final RepaymentPeriod repaymentPeriod, final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount, Money capitalizedIncomePrincipal) {
        final InterestPeriod previousInterestPeriod = findPreviousInterestPeriod(repaymentPeriod, balanceChangeDate);
        final LocalDate originalDueDate = previousInterestPeriod.getDueDate();
        final LocalDate newDueDate = calculateNewDueDate(previousInterestPeriod, balanceChangeDate);
        final boolean isPaused = previousInterestPeriod.isPaused();

        previousInterestPeriod.setDueDate(newDueDate);
        previousInterestPeriod.addDisbursementAmount(disbursedAmount);
        previousInterestPeriod.addCapitalizedIncomePrincipalAmount(capitalizedIncomePrincipal);
        previousInterestPeriod.addBalanceCorrectionAmount(correctionAmount);

        final InterestPeriod interestPeriod = InterestPeriod.withEmptyAmounts(repaymentPeriod, newDueDate, originalDueDate, isPaused);
        repaymentPeriod.getInterestPeriods().add(interestPeriod);
    }

    private void insertInterestPausePeriods(final RepaymentPeriod repaymentPeriod, final LocalDate pauseStart, final LocalDate pauseEnd) {
        final boolean isPauseStartOnFirstDayOfPeriod = pauseStart.isEqual(repaymentPeriod.getFromDate().plusDays(1));
        final LocalDate effectivePauseStart = isPauseStartOnFirstDayOfPeriod ? pauseStart : pauseStart.minusDays(1);

        final LocalDate finalPauseStart = effectivePauseStart.isBefore(repaymentPeriod.getFromDate()) ? repaymentPeriod.getFromDate()
                : effectivePauseStart;
        final LocalDate finalPauseEnd = pauseEnd.isAfter(repaymentPeriod.getDueDate()) ? repaymentPeriod.getDueDate() : pauseEnd;

        final List<InterestPeriod> newInterestPeriods = new ArrayList<>();
        for (final InterestPeriod interestPeriod : repaymentPeriod.getInterestPeriods()) {
            if (interestPeriod.getDueDate().isBefore(finalPauseStart) || !interestPeriod.getFromDate().isBefore(finalPauseEnd)) {
                newInterestPeriods.add(interestPeriod);
            } else {
                if (interestPeriod.getFromDate().isBefore(finalPauseStart)) {
                    final InterestPeriod leftSlice = InterestPeriod.copy(repaymentPeriod, interestPeriod);
                    leftSlice.setDueDate(finalPauseStart);

                    newInterestPeriods.add(leftSlice);
                }
                if (interestPeriod.getDueDate().isAfter(finalPauseEnd)) {
                    final InterestPeriod rightSlice = InterestPeriod.copy(repaymentPeriod, interestPeriod);
                    rightSlice.setFromDate(finalPauseEnd);
                    newInterestPeriods.add(rightSlice);
                }
            }
        }

        final InterestPeriod pausedSlice = InterestPeriod.withPausedAndEmptyAmounts(repaymentPeriod, finalPauseStart, finalPauseEnd);
        newInterestPeriods.add(pausedSlice);

        newInterestPeriods.sort(Comparator.comparing(InterestPeriod::getFromDate));

        repaymentPeriod.setInterestPeriods(newInterestPeriods);
    }

    private InterestPeriod findPreviousInterestPeriod(final RepaymentPeriod repaymentPeriod, final LocalDate date) {
        if (date.isAfter(repaymentPeriod.getFromDate())) {
            return repaymentPeriod.getLastInterestPeriod();
        } else {
            return repaymentPeriod.getInterestPeriods().stream()
                    .filter(ip -> date.isAfter(ip.getFromDate()) && !date.isAfter(ip.getDueDate())).reduce((first, second) -> second)
                    .orElse(repaymentPeriod.getInterestPeriods().getFirst());
        }
    }

    /**
     * Gives back the total due interest amount in the whole repayment schedule. Also includes credited interest amount.
     *
     * @return
     */
    public Money getTotalDueInterest() {
        return MathUtil.negativeToZero(repaymentPeriods().stream().map(RepaymentPeriod::getDueInterest).reduce(zero(), Money::plus), mc);
    }

    /**
     * Gives back the total due principal amount in the whole repayment schedule based on disbursements. Do not contain
     * credited principal amount.
     *
     * @return
     */
    public Money getTotalDuePrincipal() {
        return MathUtil.negativeToZero(repaymentPeriods.stream().map(RepaymentPeriod::getCreditedAmounts).reduce(zero(), Money::plus), mc);
    }

    /**
     * Gives back the total paid interest amount in the whole repayment schedule.
     *
     * @return
     */
    public Money getTotalPaidInterest() {
        return MathUtil.negativeToZero(repaymentPeriods().stream().map(RepaymentPeriod::getPaidInterest).reduce(zero, Money::plus), mc);
    }

    /**
     * Gives back the total paid principal amount in the whole repayment schedule.
     *
     * @return
     */
    public Money getTotalPaidPrincipal() {
        return MathUtil.negativeToZero(repaymentPeriods().stream().map(RepaymentPeriod::getPaidPrincipal).reduce(zero, Money::plus), mc);
    }

    /**
     * Gives back the total credited principal amount in the whole repayment schedule.
     *
     * @return
     */
    public Money getTotalCreditedPrincipal() {
        return MathUtil.negativeToZero(repaymentPeriods().stream().map(RepaymentPeriod::getCreditedPrincipal).reduce(zero, Money::plus),
                mc);
    }

    public Money getTotalOutstandingPrincipal() {
        return MathUtil.negativeToZero(getTotalDuePrincipal().minus(getTotalPaidPrincipal()));
    }

    public Optional<RepaymentPeriod> findRepaymentPeriod(@NotNull LocalDate transactionDate) {
        return repaymentPeriods.stream() //
                .filter(period -> isInPeriod(transactionDate, period.getFromDate(), period.getDueDate(), period.isFirstRepaymentPeriod()))//
                .findFirst();
    }

    /**
     * Check if there is a disbursement in the model.
     *
     * @return
     */
    public boolean isEmpty() {
        return repaymentPeriods.stream() //
                .filter(rp -> !rp.getEmi().isZero()) //
                .findFirst() //
                .isEmpty(); //
    }

    @NotNull
    public RepaymentPeriod getLastRepaymentPeriod() {
        return repaymentPeriods.getLast();
    }

    public boolean isLastRepaymentPeriod(@NotNull RepaymentPeriod repaymentPeriod) {
        return getLastRepaymentPeriod().equals(repaymentPeriod);
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

    private Map<LoanTermVariationType, List<LoanTermVariationsData>> buildLoanTermVariationMap(
            final List<LoanTermVariationsData> loanTermVariationsData) {
        if (loanTermVariationsData == null) {
            return new HashMap<>();
        }
        return loanTermVariationsData.stream()
                .collect(Collectors.groupingBy(ltvd -> LoanTermVariationType.fromInt(ltvd.getTermType().getId().intValue())));
    }

    public void disableEMIRecalculation() {
        this.modifiers.put(EMI_RECALCULATION, false);
    }

    public boolean isEMIRecalculationEnabled() {
        return this.modifiers.get(EMI_RECALCULATION);
    }

    public boolean isCopy() {
        return this.modifiers.get(COPY);
    }
}
