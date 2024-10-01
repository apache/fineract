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
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

@Data
@Accessors(fluent = true)
public class ProgressiveLoanInterestScheduleModel {

    private final List<EmiRepaymentPeriod> repaymentPeriods;
    private final List<EmiInterestPeriod> interestPeriods;
    private final List<EmiInterestRate> interestRates;
    private final LoanProductRelatedDetail loanProductRelatedDetail;
    private final Integer installmentAmountInMultiplesOf;
    private final MathContext mc;

    public ProgressiveLoanInterestScheduleModel(List<EmiRepaymentPeriod> repaymentPeriods,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf, MathContext mc) {
        this.repaymentPeriods = repaymentPeriods;
        this.interestPeriods = createInterestPeriodsBasedOnRepaymentPeriods(repaymentPeriods, loanProductRelatedDetail.getCurrency());
        this.interestRates = new ArrayList<>();
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.mc = mc;
        refreshInterestPeriodsAssociations();
    }

    private ProgressiveLoanInterestScheduleModel(List<EmiRepaymentPeriod> repaymentPeriods, final List<EmiInterestPeriod> interestPeriods,
            LoanProductRelatedDetail loanProductRelatedDetail, Integer installmentAmountInMultiplesOf, MathContext mc) {
        this.repaymentPeriods = repaymentPeriods;
        this.interestPeriods = interestPeriods;
        this.interestRates = new ArrayList<>();
        this.loanProductRelatedDetail = loanProductRelatedDetail;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.mc = mc;
        refreshInterestPeriodsAssociations();
    }

    private List<EmiInterestPeriod> createInterestPeriodsBasedOnRepaymentPeriods(final List<EmiRepaymentPeriod> repayments,
            final MonetaryCurrency currency) {
        final Money zeroAmount = Money.zero(currency);
        return repayments
                .stream().map(repaymentPeriod -> new EmiInterestPeriod(repaymentPeriod, repaymentPeriod.getFromDate(),
                        repaymentPeriod.getDueDate(), BigDecimal.ZERO, zeroAmount, zeroAmount, zeroAmount))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ProgressiveLoanInterestScheduleModel deepCopy() {
        final var repaymentPeriodCopies = copyRepaymentPeriods(this.repaymentPeriods);
        final var interestPeriodCopies = copyInterestPeriods(this.interestPeriods, repaymentPeriodCopies);
        return new ProgressiveLoanInterestScheduleModel(repaymentPeriodCopies, interestPeriodCopies, loanProductRelatedDetail,
                installmentAmountInMultiplesOf, mc);
    }

    private List<EmiInterestPeriod> copyInterestPeriods(final List<EmiInterestPeriod> interestPeriods,
            final List<EmiRepaymentPeriod> repaymentPeriods) {
        var repaymentPeriodDueDateMap = new HashMap<LocalDate, EmiRepaymentPeriod>();
        for (var repaymentPeriod : repaymentPeriods) {
            repaymentPeriodDueDateMap.put(repaymentPeriod.getDueDate(), repaymentPeriod);
        }

        return interestPeriods.stream()
                .map(interestPeriod -> new EmiInterestPeriod(interestPeriod,
                        repaymentPeriodDueDateMap.get(interestPeriod.getRepaymentPeriod().getDueDate())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<EmiRepaymentPeriod> copyRepaymentPeriods(final List<EmiRepaymentPeriod> repaymentPeriods) {
        final List<EmiRepaymentPeriod> repaymentCopies = new ArrayList<>(repaymentPeriods.size() + 3);
        EmiRepaymentPeriod previousPeriod = null;
        for (var repaymentModel : this.repaymentPeriods()) {
            var currentPeriod = new EmiRepaymentPeriod(repaymentModel, previousPeriod);
            if (previousPeriod != null) {
                previousPeriod.setNext(currentPeriod);
            }
            previousPeriod = currentPeriod;
            repaymentCopies.add(currentPeriod);
        }
        return repaymentCopies;
    }

    public void refreshInterestPeriodsAssociations() {
        repaymentPeriods.forEach(repayment -> repayment.updateInterestPeriods(interestPeriods));
    }

    public void resetInterestPeriodsAssociations() {
        repaymentPeriods.forEach(repaymentPeriod -> {
            for (var interestPeriod : repaymentPeriod.getInterestPeriods()) {
                if (!interestPeriod.getFromDate().isBefore(repaymentPeriod.getFromDate())
                        && (repaymentPeriod.isLastPeriod() || interestPeriod.getFromDate().isBefore(repaymentPeriod.getDueDate()))) {
                    interestPeriod.setRepaymentPeriod(repaymentPeriod);
                }
            }
            repaymentPeriod.updateInterestPeriods(interestPeriods);
        });
    }

    public BigDecimal getInterestRate(final LocalDate effectiveDate) {
        return interestRates.isEmpty() ? loanProductRelatedDetail.getAnnualNominalInterestRate() : findInterestRate(effectiveDate);
    }

    private BigDecimal findInterestRate(final LocalDate effectiveDate) {
        return interestRates.stream().filter(ir -> !ir.effectiveFrom().isAfter(effectiveDate)).map(EmiInterestRate::interestRate)
                .findFirst().orElse(loanProductRelatedDetail.getAnnualNominalInterestRate());
    }

    public Optional<EmiRepaymentPeriod> findRepaymentPeriod(final LocalDate repaymentPeriodDueDate) {
        if (repaymentPeriodDueDate == null) {
            return Optional.empty();
        }
        return repaymentPeriods.stream()//
                .filter(repaymentPeriodItem -> repaymentPeriodItem.getDueDate().isEqual(repaymentPeriodDueDate))//
                .findFirst();
    }

    public List<EmiRepaymentPeriod> getRelatedRepaymentPeriods(final LocalDate calculateFromRepaymentPeriodDueDate) {
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
        final var firstPeriod = repaymentPeriods.get(0);
        final var lastPeriod = repaymentPeriods.size() > 1 ? repaymentPeriods.get(repaymentPeriods.size() - 1) : firstPeriod;
        return Math.toIntExact(ChronoUnit.DAYS.between(firstPeriod.getFromDate(), lastPeriod.getDueDate()));
    }

    public EmiInterestPeriod addInterestRate(final LocalDate newInterestSubmittedOnDate, final BigDecimal newInterestRate) {
        final LocalDate newInterestEffectiveDate = newInterestSubmittedOnDate.minusDays(1);

        interestRates.add(new EmiInterestRate(newInterestEffectiveDate, newInterestSubmittedOnDate, newInterestRate));
        interestRates.sort(Collections.reverseOrder());

        return findInterestPeriodForInterestChange(newInterestEffectiveDate)
                .orElseGet(() -> insertInterestPeriod(newInterestEffectiveDate));
    }

    Optional<EmiInterestPeriod> findInterestPeriodForInterestChange(final LocalDate interestRateChangeEffectiveDate) {
        if (interestRateChangeEffectiveDate == null) {
            return Optional.empty();
        }
        return interestPeriods.stream()//
                .filter(interestPeriod -> interestRateChangeEffectiveDate.isEqual(interestPeriod.getFromDate()))//
                .findFirst();
    }

    EmiInterestPeriod insertInterestPeriod(final LocalDate interestRateChangeEffectiveDate) {
        // period start date
        final EmiInterestPeriod previousInterestPeriod = interestPeriods.stream()
                .filter(interestPeriod -> interestRateChangeEffectiveDate.isAfter(interestPeriod.getFromDate())
                        && interestRateChangeEffectiveDate.isBefore(interestPeriod.getDueDate()))//
                .findFirst()//
                .get();//

        final Money zeroAmount = Money.zero(loanProductRelatedDetail.getCurrency());
        final var interestPeriod = new EmiInterestPeriod(previousInterestPeriod.getRepaymentPeriod(), interestRateChangeEffectiveDate,
                previousInterestPeriod.getDueDate(), BigDecimal.ZERO, zeroAmount, zeroAmount, zeroAmount);

        previousInterestPeriod.setDueDate(interestRateChangeEffectiveDate);

        interestPeriods.add(interestPeriod);
        Collections.sort(interestPeriods);

        interestPeriod.getRepaymentPeriod().updateInterestPeriods(interestPeriods);

        return previousInterestPeriod;
    }

    public Optional<EmiRepaymentPeriod> changeOutstandingBalanceAndUpdateInterestPeriods(final LocalDate repaymentPeriodDueDate,
            final LocalDate balanceChangeDate, final Money disbursedAmount, final Money correctionAmount) {
        return findRepaymentPeriodForBalanceChange(repaymentPeriodDueDate).stream()//
                .peek(updateInterestPeriodOnRepaymentPeriod(balanceChangeDate, disbursedAmount, correctionAmount, false))//
                .findFirst();//
    }

    public Optional<EmiRepaymentPeriod> insertVirtualInterestPeriod(final LocalDate periodDueDate, final LocalDate operationDate) {
        final var zeroAmount = Money.zero(loanProductRelatedDetail.getCurrency());
        return findRepaymentPeriod(periodDueDate).stream()//
                .peek(updateInterestPeriodOnRepaymentPeriod(operationDate, zeroAmount, zeroAmount, true))//
                .findFirst();//
    }

    Optional<EmiRepaymentPeriod> findRepaymentPeriodForBalanceChange(final LocalDate repaymentPeriodDueDate) {
        if (repaymentPeriodDueDate == null) {
            return Optional.empty();
        }
        return repaymentPeriods.stream()//
                .filter(repaymentPeriod -> repaymentPeriodDueDate.isEqual(repaymentPeriod.getDueDate()))//
                .findFirst();
    }

    private Consumer<EmiRepaymentPeriod> updateInterestPeriodOnRepaymentPeriod(final LocalDate balanceChangeDate,
            final Money disbursedAmount, final Money correctionAmount, final boolean tillBalanceChangeDate) {
        return repaymentPeriod -> {
            if (tillBalanceChangeDate && balanceChangeDate.isEqual(repaymentPeriod.getFromDate())) {
                insertInterestPeriodIntoStart(repaymentPeriod, disbursedAmount, correctionAmount);
                return;
            }

            final var interestPeriodOptional = findInterestPeriodForBalanceChange(repaymentPeriod, balanceChangeDate);
            if (interestPeriodOptional.isPresent()) {
                interestPeriodOptional.get().addDisbursedAmount(disbursedAmount);
                interestPeriodOptional.get().addCorrectionAmount(correctionAmount);
            } else {
                insertInterestPeriod(repaymentPeriod, balanceChangeDate, disbursedAmount, correctionAmount);
            }
        };
    }

    Optional<EmiInterestPeriod> findInterestPeriodForBalanceChange(final EmiRepaymentPeriod repaymentPeriod,
            final LocalDate balanceChangeDate) {
        if (repaymentPeriod == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> balanceChangeDate.isEqual(interestPeriod.getFromDate()))//
                .findFirst();
    }

    void insertInterestPeriod(final EmiRepaymentPeriod repaymentPeriod, final LocalDate interestPeriodFromDate, final Money disbursedAmount,
            final Money correctionAmount) {
        // interestPeriodFromDate is after disb.date because this case when disbursement date is different then interest
        // period start date
        final EmiInterestPeriod previousInterestPeriod = interestPeriods.stream()
                .filter(operationRelatedPreviousInterestPeriod(repaymentPeriod, interestPeriodFromDate))//
                .findFirst()//
                .get();//

        final boolean changeAfterLastRepaymentPeriod = repaymentPeriod.isLastPeriod()
                && previousInterestPeriod.getDueDate().isEqual(repaymentPeriod.getDueDate())
                && !interestPeriodFromDate.isBefore(repaymentPeriod.getDueDate());
        final LocalDate interestPeriodDueDate = changeAfterLastRepaymentPeriod ? interestPeriodFromDate.plusDays(1)
                : previousInterestPeriod.getDueDate();
        final var interestPeriod = new EmiInterestPeriod(repaymentPeriod, interestPeriodFromDate, interestPeriodDueDate, BigDecimal.ZERO,
                disbursedAmount, correctionAmount, Money.zero(disbursedAmount.getCurrency()));

        previousInterestPeriod.setDueDate(interestPeriodFromDate);

        interestPeriods.add(interestPeriod);
        Collections.sort(interestPeriods);
        repaymentPeriod.updateInterestPeriods(interestPeriods);
    }

    void insertInterestPeriodIntoStart(final EmiRepaymentPeriod repaymentPeriod, final Money disbursedAmount,
            final Money correctionAmount) {
        final Money zeroAmount = Money.zero(disbursedAmount.getCurrency());
        // interestPeriodFromDate is after disb.date because this case when disbursement date is different then interest
        // we always have at least one period
        final EmiInterestPeriod selectedInterestPeriod = repaymentPeriod.getInterestPeriods().stream()
                .filter(ip -> ip.getFromDate().equals(repaymentPeriod.getFromDate())).findFirst()
                .orElse(repaymentPeriod.getInterestPeriods().get(0));

        final LocalDate interestPeriodDueDate = selectedInterestPeriod.getFromDate();
        final var newInterestPeriod = new EmiInterestPeriod(selectedInterestPeriod.getRepaymentPeriod(),
                selectedInterestPeriod.getFromDate(), interestPeriodDueDate, BigDecimal.ZERO, zeroAmount, zeroAmount,
                Money.zero(disbursedAmount.getCurrency()));

        newInterestPeriod.setDisbursedAmount(disbursedAmount.add(selectedInterestPeriod.getDisbursedAmount()));
        newInterestPeriod.setCorrectionAmount(correctionAmount.add(selectedInterestPeriod.getCorrectionAmount()));

        // reset amounts on next periods
        selectedInterestPeriod.setDisbursedAmount(zeroAmount);
        selectedInterestPeriod.setCorrectionAmount(zeroAmount);
        selectedInterestPeriod.setFromDate(interestPeriodDueDate);

        interestPeriods.add(newInterestPeriod);
        Collections.sort(interestPeriods);
        repaymentPeriod.updateInterestPeriods(interestPeriods);
    }

    private static Predicate<EmiInterestPeriod> operationRelatedPreviousInterestPeriod(EmiRepaymentPeriod repaymentPeriod,
            LocalDate operationDate) {
        return interestPeriod -> operationDate.isAfter(interestPeriod.getFromDate())
                && (operationDate.isBefore(interestPeriod.getDueDate()) || (repaymentPeriod.getDueDate().equals(interestPeriod.getDueDate())
                        && !operationDate.isBefore(repaymentPeriod.getDueDate())));
    }
}
