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
package org.apache.fineract.portfolio.loanproduct.calc;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestRepaymentInterestPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestRepaymentModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.mapper.ProgressiveLoanInterestRepaymentModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ProgressiveEMICalculator implements EMICalculator {

    private final ProgressiveLoanInterestRepaymentModelMapper progressiveLoanInterestRepaymentModelMapper;

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    @Override
    public ProgressiveLoanInterestScheduleModel generateInterestScheduleModel(final List<LoanScheduleModelRepaymentPeriod> periods,
            final LoanProductRelatedDetail loanProductRelatedDetail, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        final Money zeroAmount = Money.zero(loanProductRelatedDetail.getCurrency());
        final ArrayList<ProgressiveLoanInterestRepaymentModel> interestRepaymentModelList = new ArrayList<>(periods.size());
        for (final LoanScheduleModelRepaymentPeriod period : periods) {
            interestRepaymentModelList
                    .add(new ProgressiveLoanInterestRepaymentModel(period.periodFromDate(), period.periodDueDate(), zeroAmount));
        }
        if (!interestRepaymentModelList.isEmpty()) {
            interestRepaymentModelList.get(interestRepaymentModelList.size() - 1).setLastPeriod(true);
        }
        return new ProgressiveLoanInterestScheduleModel(interestRepaymentModelList, loanProductRelatedDetail,
                installmentAmountInMultiplesOf, mc);
    }

    @Override
    public Optional<ProgressiveLoanInterestRepaymentModel> findInterestRepaymentPeriod(
            final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate repaymentPeriodDueDate) {
        if (scheduleModel == null || repaymentPeriodDueDate == null) {
            return Optional.empty();
        }
        return scheduleModel.repayments().stream()//
                .filter(interestRepaymentPeriodItem -> interestRepaymentPeriodItem.getDueDate().isEqual(repaymentPeriodDueDate))//
                .findFirst();
    }

    Optional<ProgressiveLoanInterestRepaymentModel> findInterestRepaymentPeriodForBalanceChange(
            final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate balanceChangeDate) {
        if (scheduleModel == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        return scheduleModel.repayments().stream()//
                .filter(repaymentPeriod -> !balanceChangeDate.isBefore(repaymentPeriod.getFromDate())
                        && (repaymentPeriod.isLastPeriod() || balanceChangeDate.isBefore(repaymentPeriod.getDueDate())))//
                .findFirst();
    }

    Optional<ProgressiveLoanInterestRepaymentInterestPeriod> findInterestPeriodForBalanceChange(
            final ProgressiveLoanInterestRepaymentModel repaymentPeriod, final LocalDate balanceChangeDate) {
        if (repaymentPeriod == null || balanceChangeDate == null) {
            return Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> balanceChangeDate.isEqual(interestPeriod.getFromDate()))//
                .findFirst();
    }

    Optional<ProgressiveLoanInterestRepaymentInterestPeriod> findInterestPeriodForInterestChange(
            final ProgressiveLoanInterestRepaymentModel repaymentPeriod, final LocalDate interestRateChangeEffectiveDate) {
        if (repaymentPeriod == null || interestRateChangeEffectiveDate == null) {
            return Optional.empty();
        }
        return repaymentPeriod.getInterestPeriods().stream()//
                .filter(interestPeriod -> interestRateChangeEffectiveDate.isEqual(interestPeriod.getFromDate()))//
                .findFirst();
    }

    Optional<ProgressiveLoanInterestRepaymentModel> findInterestRepaymentPeriodForInterestChange(
            final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate interestChangeEffectiveDate) {
        if (scheduleModel == null || interestChangeEffectiveDate == null) {
            return Optional.empty();
        }
        return scheduleModel.repayments().stream()//
                .filter(repaymentPeriod -> !interestChangeEffectiveDate.isBefore(repaymentPeriod.getFromDate())
                        && interestChangeEffectiveDate.isBefore(repaymentPeriod.getDueDate()))//
                .findFirst();
    }

    /**
     * Add disbursement to Interest Period
     */
    @Override
    public void addDisbursement(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate disbursementDueDate,
            final Money disbursedAmount) {
        changeOutstandingBalanceAndUpdateInterestPeriods(scheduleModel, disbursementDueDate, disbursedAmount,
                Money.zero(disbursedAmount.getCurrency()))
                .ifPresent((repaymentPeriod) -> calculateEMIValueAndRateFactors(repaymentPeriod.getDueDate(), scheduleModel));
    }

    Optional<ProgressiveLoanInterestRepaymentModel> changeOutstandingBalanceAndUpdateInterestPeriods(
            final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate balanceChangeDate, final Money disbursedAmount,
            final Money correctionAmount) {
        return findInterestRepaymentPeriodForBalanceChange(scheduleModel, balanceChangeDate).stream()//
                .peek(updateInterestPeriodOnRepaymentPeriod(balanceChangeDate, disbursedAmount, correctionAmount, false))//
                .findFirst();//
    }

    @NotNull
    private Consumer<ProgressiveLoanInterestRepaymentModel> updateInterestPeriodOnRepaymentPeriod(final LocalDate balanceChangeDate,
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

    void insertInterestPeriodIntoStart(final ProgressiveLoanInterestRepaymentModel repaymentPeriod, final Money disbursedAmount,
            final Money correctionAmount) {
        final Money zeroAmount = Money.zero(disbursedAmount.getCurrency());
        // interestPeriodFromDate is after disb.date because this case when disbursement date is different then interest
        // we always have at least one period
        final ProgressiveLoanInterestRepaymentInterestPeriod selectedInterestPeriod = repaymentPeriod.getInterestPeriods().get(0);

        final LocalDate interestPeriodDueDate = selectedInterestPeriod.getFromDate();
        final var newInterestPeriod = new ProgressiveLoanInterestRepaymentInterestPeriod(selectedInterestPeriod.getFromDate(),
                interestPeriodDueDate, BigDecimal.ZERO, zeroAmount, zeroAmount, Money.zero(disbursedAmount.getCurrency()));

        newInterestPeriod.setDisbursedAmount(disbursedAmount.add(selectedInterestPeriod.getDisbursedAmount()));
        newInterestPeriod.setCorrectionAmount(correctionAmount.add(selectedInterestPeriod.getCorrectionAmount()));

        // reset amounts on next periods
        selectedInterestPeriod.setDisbursedAmount(zeroAmount);
        selectedInterestPeriod.setCorrectionAmount(zeroAmount);
        selectedInterestPeriod.setFromDate(interestPeriodDueDate);

        repaymentPeriod.getInterestPeriods().add(newInterestPeriod);
        Collections.sort(repaymentPeriod.getInterestPeriods());
    }

    void insertInterestPeriod(final ProgressiveLoanInterestRepaymentModel repaymentPeriod, final LocalDate interestPeriodFromDate,
            final Money disbursedAmount, final Money correctionAmount) {
        // interestPeriodFromDate is after disb.date because this case when disbursement date is different then interest
        // period start date
        final ProgressiveLoanInterestRepaymentInterestPeriod previousInterestPeriod = repaymentPeriod.getInterestPeriods().stream()
                .filter(operationRelatedPreviousInterestPeriod(repaymentPeriod, interestPeriodFromDate))//
                .findFirst()//
                .get();//

        final boolean changeAfterLastRepaymentPeriod = repaymentPeriod.isLastPeriod()
                && previousInterestPeriod.getDueDate().isEqual(repaymentPeriod.getDueDate())
                && !interestPeriodFromDate.isBefore(repaymentPeriod.getDueDate());
        final LocalDate interestPeriodDueDate = changeAfterLastRepaymentPeriod ? interestPeriodFromDate.plusDays(1)
                : previousInterestPeriod.getDueDate();
        final var interestPeriod = new ProgressiveLoanInterestRepaymentInterestPeriod(interestPeriodFromDate, interestPeriodDueDate,
                BigDecimal.ZERO, disbursedAmount, correctionAmount, Money.zero(disbursedAmount.getCurrency()));

        previousInterestPeriod.setDueDate(interestPeriodFromDate);

        repaymentPeriod.getInterestPeriods().add(interestPeriod);
        Collections.sort(repaymentPeriod.getInterestPeriods());
    }

    private static Predicate<ProgressiveLoanInterestRepaymentInterestPeriod> operationRelatedPreviousInterestPeriod(
            ProgressiveLoanInterestRepaymentModel repaymentPeriod, LocalDate operationDate) {
        return interestPeriod -> operationDate.isAfter(interestPeriod.getFromDate())
                && (operationDate.isBefore(interestPeriod.getDueDate()) || (repaymentPeriod.getDueDate().equals(interestPeriod.getDueDate())
                        && !operationDate.isBefore(repaymentPeriod.getDueDate())));
    }

    @Override
    public ProgressiveLoanInterestScheduleModel makeScheduleModelDeepCopy(final ProgressiveLoanInterestScheduleModel scheduleModel) {
        return makeScheduleModelDeepCopy(scheduleModel, scheduleModel.loanProductRelatedDetail(),
                scheduleModel.installmentAmountInMultiplesOf(), scheduleModel.mc());
    }

    @Override
    public ProgressiveLoanInterestScheduleModel makeScheduleModelDeepCopy(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LoanProductRelatedDetail loanProductRelatedDetail, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        List<ProgressiveLoanInterestRepaymentModel> repayments = new ArrayList<>(scheduleModel.repayments().size());
        for (var repaymentModel : scheduleModel.repayments()) {
            repayments.add(new ProgressiveLoanInterestRepaymentModel(repaymentModel));
        }
        return new ProgressiveLoanInterestScheduleModel(repayments, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }

    @Override
    public void changeInterestRate(final ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate newInterestEffectiveDate,
            final BigDecimal newInterestRate) {
        final ProgressiveLoanInterestRepaymentModel repaymentPeriod = findInterestRepaymentPeriodForInterestChange(scheduleModel,
                newInterestEffectiveDate).orElse(null);
        if (repaymentPeriod == null) {
            return;
        }
        scheduleModel.addInterestRate(newInterestEffectiveDate, newInterestRate);
        var interestPeriodOptional = findInterestPeriodForInterestChange(repaymentPeriod, newInterestEffectiveDate);
        if (interestPeriodOptional.isEmpty()) {
            insertInterestPeriod(scheduleModel, repaymentPeriod, newInterestEffectiveDate);
        }

        calculateEMIValueAndRateFactors(repaymentPeriod.getDueDate(), scheduleModel);
    }

    void insertInterestPeriod(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final ProgressiveLoanInterestRepaymentModel repaymentPeriod, final LocalDate interestChangeDueDate) {
        // period start date
        final ProgressiveLoanInterestRepaymentInterestPeriod previousInterestPeriod = repaymentPeriod.getInterestPeriods().stream()
                .filter(interestPeriod -> interestChangeDueDate.isAfter(interestPeriod.getFromDate())
                        && interestChangeDueDate.isBefore(interestPeriod.getDueDate()))//
                .findFirst()//
                .get();//

        final Money zeroAmount = Money.zero(scheduleModel.loanProductRelatedDetail().getCurrency());
        final var interestPeriod = new ProgressiveLoanInterestRepaymentInterestPeriod(interestChangeDueDate,
                previousInterestPeriod.getDueDate(), BigDecimal.ZERO, zeroAmount, zeroAmount, zeroAmount);

        previousInterestPeriod.setDueDate(interestChangeDueDate);

        repaymentPeriod.getInterestPeriods().add(interestPeriod);
        Collections.sort(repaymentPeriod.getInterestPeriods());
    }

    @Override
    public void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate balanceCorrectionDate,
            Money balanceCorrectionAmount) {
        final Money zeroAmount = Money.zero(balanceCorrectionAmount.getCurrency());
        changeOutstandingBalanceAndUpdateInterestPeriods(scheduleModel, balanceCorrectionDate, zeroAmount, balanceCorrectionAmount)
                .ifPresent(repaymentPeriod -> {
                    calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculatePrincipalInterestComponentsForPeriods(scheduleModel);
                });
    }

    @Override
    public Optional<ProgressiveLoanInterestRepaymentModel> getPayableDetails(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LocalDate periodDueDate, final LocalDate payDate) {
        final var newScheduleModel = makeScheduleModelDeepCopy(scheduleModel);
        final var zeroAmount = Money.zero(scheduleModel.loanProductRelatedDetail().getCurrency());

        return findInterestRepaymentPeriod(newScheduleModel, periodDueDate).stream()
                .peek(updateInterestPeriodOnRepaymentPeriod(payDate, zeroAmount, zeroAmount, true))//
                .peek(repaymentPeriod -> {
                    calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculatePrincipalInterestComponentsForPeriod(repaymentPeriod, payDate);
                }).findFirst();
    }

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     */
    void calculateEMIValueAndRateFactors(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        final List<ProgressiveLoanInterestRepaymentModel> relatedRepaymentPeriods = getRelatedRepaymentPeriods(
                calculateFromRepaymentPeriodDueDate, scheduleModel);
        calculateRateFactorMinus1ForPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateEMIOnPeriods(relatedRepaymentPeriods, scheduleModel);
        calculatePrincipalInterestComponentsForPeriods(scheduleModel);
        checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(scheduleModel, relatedRepaymentPeriods);
    }

    private static List<ProgressiveLoanInterestRepaymentModel> getRelatedRepaymentPeriods(LocalDate calculateFromRepaymentPeriodDueDate,
            ProgressiveLoanInterestScheduleModel scheduleModel) {
        return calculateFromRepaymentPeriodDueDate == null ? scheduleModel.repayments()
                : scheduleModel.repayments().stream().filter(period -> !period.getDueDate().isBefore(calculateFromRepaymentPeriodDueDate))
                        .toList();
    }

    private void checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final List<ProgressiveLoanInterestRepaymentModel> relatedRepaymentPeriods) {
        final Money emiDifference = getDifferenceBetweenLastTwoPeriod(relatedRepaymentPeriods, scheduleModel);
        final int numberOfRelatedPeriods = relatedRepaymentPeriods.size();
        double lowerHalfOfRelatedPeriods = Math.floor(numberOfRelatedPeriods / 2.0);
        if (emiDifference.isZero() || lowerHalfOfRelatedPeriods == 0.0) {
            return;
        }
        final Money originalEmi = relatedRepaymentPeriods.get(numberOfRelatedPeriods - 2).getEqualMonthlyInstallment();
        boolean shouldBeAdjusted = emiDifference.abs().multipliedBy(100)
                .isGreaterThan(Money.of(originalEmi.getCurrency(), BigDecimal.valueOf(lowerHalfOfRelatedPeriods)));

        final MathContext mc = scheduleModel.mc();
        if (shouldBeAdjusted) {
            Money adjustment = emiDifference.dividedBy(numberOfRelatedPeriods, mc.getRoundingMode());
            Money adjustedEqualMonthlyInstallmentValue = applyInstallmentAmountInMultiplesOf(scheduleModel, originalEmi.plus(adjustment));
            if (adjustedEqualMonthlyInstallmentValue.compareTo(originalEmi) == 0) {
                return;
            }
            final LocalDate relatedPeriodsFirstDueDate = relatedRepaymentPeriods.get(0).getDueDate();
            final var newScheduleModel = makeScheduleModelDeepCopy(scheduleModel);
            newScheduleModel.repayments().forEach(period -> {
                if (!period.getDueDate().isBefore(relatedPeriodsFirstDueDate)) {
                    period.setEqualMonthlyInstallment(adjustedEqualMonthlyInstallmentValue);
                }
            });
            calculatePrincipalInterestComponentsForPeriods(newScheduleModel);
            final Money newEmiDifference = getDifferenceBetweenLastTwoPeriod(newScheduleModel.repayments(), scheduleModel);
            final boolean newEmiHasLessDifference = newEmiDifference.abs().compareTo(emiDifference.abs()) < 0;
            if (!newEmiHasLessDifference) {
                return;
            }

            final Iterator<ProgressiveLoanInterestRepaymentModel> relatedPeriodFromNewModelIterator = newScheduleModel//
                    .repayments().stream()//
                    .filter(period -> !period.getDueDate().isBefore(relatedPeriodsFirstDueDate))//
                    .toList().iterator();//

            relatedRepaymentPeriods.forEach(relatedRepaymentPeriod -> {
                if (!relatedPeriodFromNewModelIterator.hasNext()) {
                    return;
                }
                final ProgressiveLoanInterestRepaymentModel newRepaymentPeriod = relatedPeriodFromNewModelIterator.next();
                relatedRepaymentPeriod.setEqualMonthlyInstallment(newRepaymentPeriod.getEqualMonthlyInstallment());
                relatedRepaymentPeriod.setPrincipalDue(newRepaymentPeriod.getInterestDue());
                relatedRepaymentPeriod.setPrincipalDue(newRepaymentPeriod.getPrincipalDue());
                relatedRepaymentPeriod.setRemainingBalance(newRepaymentPeriod.getRemainingBalance());
                relatedRepaymentPeriod.setInterestPeriods(newRepaymentPeriod.getInterestPeriods());
            });
        }
    }

    /**
     * Convert Interest Percentage to fraction of 1
     *
     * @param interestRate
     *            Interest Rate in Percentage
     *
     * @param mc
     *
     * @return Rate Interest Rate in fraction format
     */
    BigDecimal calcNominalInterestRatePercentage(final BigDecimal interestRate, MathContext mc) {
        return MathUtil.nullToZero(interestRate).divide(DIVISOR_100, mc);
    }

    /**
     * * Calculate rate factors from ONLY repayment periods
     */
    void calculateRateFactorMinus1ForPeriods(final List<ProgressiveLoanInterestRepaymentModel> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriods.forEach(repaymentPeriod -> calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel));
    }

    void calculateRateFactorMinus1ForRepaymentPeriod(final ProgressiveLoanInterestRepaymentModel repaymentPeriod,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriod.getInterestPeriods().forEach(interestPeriod -> interestPeriod
                .setRateFactorMinus1(calculateRateFactorMinus1PerPeriod(repaymentPeriod, interestPeriod, scheduleModel)));
    }

    /**
     * Calculate Rate Factor-1 for an exact Period
     */
    BigDecimal calculateRateFactorMinus1PerPeriod(final ProgressiveLoanInterestRepaymentModel repaymentPeriod,
            final ProgressiveLoanInterestRepaymentInterestPeriod interestPeriod, final ProgressiveLoanInterestScheduleModel scheduleModel) {
        final MathContext mc = scheduleModel.mc();
        final LoanProductRelatedDetail loanProductRelatedDetail = scheduleModel.loanProductRelatedDetail();
        final BigDecimal interestRate = calcNominalInterestRatePercentage(scheduleModel.getInterestRate(interestPeriod.getFromDate()), mc);
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        final BigDecimal daysInMonth = BigDecimal.valueOf(daysInMonthType.getNumberOfDays(interestPeriod.getFromDate()));
        final BigDecimal daysInYear = BigDecimal.valueOf(daysInYearType.getNumberOfDays(interestPeriod.getFromDate()));
        final BigDecimal actualDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(interestPeriod.getFromDate(), interestPeriod.getDueDate()));
        final BigDecimal calculatedDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.getFromDate(), repaymentPeriod.getDueDate()));
        final int numberOfYearsDifferenceInPeriod = interestPeriod.getDueDate().getYear() - interestPeriod.getFromDate().getYear();
        final boolean partialPeriodCalculationNeeded = daysInYearType == DaysInYearType.ACTUAL && numberOfYearsDifferenceInPeriod > 0;

        // TODO check: loanApplicationTerms.calculatePeriodsBetweenDates(startDate, endDate); // calculate period data
        // TODO review: (repayment frequency: days, weeks, years; validation day is month fix 30)
        // TODO refactor this logic to represent in interest period
        if (partialPeriodCalculationNeeded) {
            final BigDecimal cumulatedPeriodFractions = calculatePeriodFractions(interestPeriod, mc);
            return rateFactorByRepaymentPartialPeriod(interestRate, repaymentEvery, cumulatedPeriodFractions, BigDecimal.ONE,
                    BigDecimal.ONE, mc).setScale(mc.getPrecision(), mc.getRoundingMode());
        }

        return calculateRateFactorMinus1PerPeriodBasedOnRepaymentFrequency(interestRate, repaymentFrequency, repaymentEvery, daysInMonth,
                daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc).setScale(mc.getPrecision(), mc.getRoundingMode());
    }

    /**
     * Calculate Period fractions part based on how much year has in the period
     *
     * @param interestPeriod
     * @param mc
     * @return
     */
    BigDecimal calculatePeriodFractions(ProgressiveLoanInterestRepaymentInterestPeriod interestPeriod, MathContext mc) {
        BigDecimal cumulatedRateFactor = BigDecimal.ZERO;
        int actualYear = interestPeriod.getFromDate().getYear();
        int endYear = interestPeriod.getDueDate().getYear();
        LocalDate actualDate = interestPeriod.getFromDate();
        LocalDate endOfActualYear;

        while (actualYear <= endYear) {
            endOfActualYear = actualYear == endYear ? interestPeriod.getDueDate() : LocalDate.of(actualYear, 12, 31);
            BigDecimal numberOfDaysInYear = BigDecimal.valueOf(Year.of(actualYear).length());
            BigDecimal calculatedDaysInActualYear = BigDecimal.valueOf(DateUtils.getDifferenceInDays(actualDate, endOfActualYear));
            cumulatedRateFactor = cumulatedRateFactor.add(calculatedDaysInActualYear.divide(numberOfDaysInYear, mc), mc);
            actualDate = endOfActualYear;
            actualYear++;
        }
        return cumulatedRateFactor;
    }

    /**
     * Calculate Rate Factor based on Repayment Frequency Type
     *
     * @param interestRate
     * @param repaymentFrequency
     * @param repaymentEvery
     * @param daysInMonth
     * @param daysInYear
     * @param actualDaysInPeriod
     * @param calculatedDaysInPeriod
     * @param mc
     * @return
     */
    BigDecimal calculateRateFactorMinus1PerPeriodBasedOnRepaymentFrequency(final BigDecimal interestRate,
            final PeriodFrequencyType repaymentFrequency, final BigDecimal repaymentEvery, final BigDecimal daysInMonth,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return switch (repaymentFrequency) {
            case DAYS -> rateFactorMinus1ByRepaymentEveryDay(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod,
                    calculatedDaysInPeriod, mc);
            case WEEKS -> rateFactorMinus1ByRepaymentEveryWeek(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod,
                    calculatedDaysInPeriod, mc);
            case MONTHS -> rateFactorMinus1ByRepaymentEveryMonth(interestRate, repaymentEvery, daysInMonth, daysInYear, actualDaysInPeriod,
                    calculatedDaysInPeriod, mc);
            default -> throw new UnsupportedOperationException("Invalid repayment frequency"); // not supported yet
        };
    }

    void calculateEMIOnPeriods(final List<ProgressiveLoanInterestRepaymentModel> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        if (repaymentPeriods.isEmpty()) {
            return;
        }
        final MathContext mc = scheduleModel.mc();
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorN(repaymentPeriods, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(repaymentPeriods, mc));
        final var startPeriod = repaymentPeriods.get(0);
        final Money remainingBalanceFromPreviousPeriod = getRemainingBalanceFromPreviousPeriod(scheduleModel, startPeriod);
        final Money outstandingBalance = remainingBalanceFromPreviousPeriod.add(startPeriod.getDisbursedAmountInPeriod());

        final Money equalMonthlyInstallment = Money.of(outstandingBalance.getCurrency(),
                calculateEMIValue(rateFactorN, outstandingBalance.getAmount(), fnResult, mc));
        final Money finalEqualMonthlyInstallment = applyInstallmentAmountInMultiplesOf(scheduleModel, equalMonthlyInstallment);

        repaymentPeriods.forEach(period -> period.setEqualMonthlyInstallment(finalEqualMonthlyInstallment));
    }

    Money getRemainingBalanceFromPreviousPeriod(ProgressiveLoanInterestScheduleModel scheduleModel,
            ProgressiveLoanInterestRepaymentModel startPeriod) {
        return scheduleModel.repayments().stream().filter(period -> period.getDueDate().isEqual(startPeriod.getFromDate()))
                .map(ProgressiveLoanInterestRepaymentModel::getRemainingBalance).findFirst()
                .orElse(Money.zero(scheduleModel.loanProductRelatedDetail().getCurrency()));
    }

    Money applyInstallmentAmountInMultiplesOf(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final Money equalMonthlyInstallment) {
        return scheduleModel.installmentAmountInMultiplesOf() != null
                ? Money.roundToMultiplesOf(equalMonthlyInstallment, scheduleModel.installmentAmountInMultiplesOf())
                : equalMonthlyInstallment;
    }

    Money getDifferenceBetweenLastTwoPeriod(final List<ProgressiveLoanInterestRepaymentModel> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        int numberOfUpcomingPeriods = repaymentPeriods.size();
        if (numberOfUpcomingPeriods < 2) {
            return Money.zero(scheduleModel.loanProductRelatedDetail().getCurrency());
        }
        final var lastPeriod = repaymentPeriods.get(numberOfUpcomingPeriods - 1);
        final var penultimatePeriod = repaymentPeriods.get(numberOfUpcomingPeriods - 2);
        return lastPeriod.getEqualMonthlyInstallment().minus(penultimatePeriod.getEqualMonthlyInstallment());
    }

    /**
     * Calculate Rate Factor Product from rate factors
     */
    BigDecimal calculateRateFactorN(final List<ProgressiveLoanInterestRepaymentModel> periods, final MathContext mc) {
        return periods.stream().map(ProgressiveLoanInterestRepaymentModel::getRateFactor).reduce(BigDecimal.ONE,
                (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     */
    BigDecimal calculateFnResult(final List<ProgressiveLoanInterestRepaymentModel> periods, final MathContext mc) {
        return periods.stream()//
                .skip(1)//
                .map(ProgressiveLoanInterestRepaymentModel::getRateFactor)//
                .reduce(BigDecimal.ONE, (BigDecimal previousValue, BigDecimal rateFactor) -> fnValue(previousValue, rateFactor, mc));//
    }

    /**
     * Calculate the EMI (Equal Monthly Installment) value
     */
    BigDecimal calculateEMIValue(final BigDecimal rateFactorN, final BigDecimal outstandingBalanceForRest, final BigDecimal fnResult,
            final MathContext mc) {
        return rateFactorN.multiply(outstandingBalanceForRest, mc).divide(fnResult, mc);
    }

    /**
     * To calculate the daily payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (repaid every / days in year) * actual days in period / calculated days in
     * period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @param mc
     * @return Rate Factor for period
     */
    BigDecimal rateFactorMinus1ByRepaymentEveryDay(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return rateFactorMinus1ByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate the weekly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (7 * repaid every / days in year) * actual days in period / calculated days
     * in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @param mc
     * @return Rate Factor for period
     */
    BigDecimal rateFactorMinus1ByRepaymentEveryWeek(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return rateFactorMinus1ByRepaymentPeriod(interestRate, ONE_WEEK_IN_DAYS, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate the monthly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * (days in month * repaid every / days in year) * actual days in period /
     * calculated days in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInMonth
     *            Days in Month based on DaysInMonth enum
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @param mc
     * @return Rate Factor for period
     */
    BigDecimal rateFactorMinus1ByRepaymentEveryMonth(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal daysInMonth, final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod,
            final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        return rateFactorMinus1ByRepaymentPeriod(interestRate, daysInMonth, repaymentEvery, daysInYear, actualDaysInPeriod,
                calculatedDaysInPeriod, mc);
    }

    /**
     * To calculate installment period payment. We're going to be using simple interest. The Rate Factor for simple
     * interest is calculated by the following formula:
     *
     * Rate factor = 1 + (rate of interest * ( repayment period multiplier in days * repaid every * days in month / days
     * in year) * actual days in period / calculated days in period ) Where
     *
     * @param interestRate
     *            Rate of Interest
     *
     * @param repaymentPeriodMultiplierInDays
     *            Multiplier number in days of the repayment every parameter
     *
     * @param repaymentEvery
     *            Repaid Every
     *
     * @param daysInYear
     *            Days is Year based on DaysInYear enum
     *
     * @param actualDaysInPeriod
     *            Always the actual number of days in the actual period
     *
     * @param calculatedDaysInPeriod
     *            Calculated days in Period (It has importance related to Reschedule)
     *
     * @param mc
     * @return Rate Factor for period
     */
    BigDecimal rateFactorMinus1ByRepaymentPeriod(final BigDecimal interestRate, final BigDecimal repaymentPeriodMultiplierInDays,
            final BigDecimal repaymentEvery, final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod,
            final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        final BigDecimal interestFractionPerPeriod = repaymentPeriodMultiplierInDays.multiply(repaymentEvery, mc).divide(daysInYear, mc);
        return interestRate//
                .multiply(interestFractionPerPeriod, mc)//
                .multiply(actualDaysInPeriod, mc)//
                .divide(calculatedDaysInPeriod, mc);//
    }

    /**
     * Calculate Rate Factor based on Partial Period
     *
     */
    BigDecimal rateFactorByRepaymentPartialPeriod(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal cumulatedPeriodRatio, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        final BigDecimal interestFractionPerPeriod = repaymentEvery.multiply(cumulatedPeriodRatio, mc);
        return interestRate//
                .multiply(interestFractionPerPeriod, mc)//
                .multiply(actualDaysInPeriod, mc)//
                .divide(calculatedDaysInPeriod, mc);//
    }

    /**
     * To calculate the function value for each period, we are going to use the next formula:
     *
     * fn = 1 + fnValueFrom * rateFactorEnd
     *
     * @param previousFnValue
     *
     * @param currentRateFactor
     *
     * @param mc
     *
     */
    BigDecimal fnValue(final BigDecimal previousFnValue, final BigDecimal currentRateFactor, final MathContext mc) {
        return BigDecimal.ONE.add(previousFnValue.multiply(currentRateFactor, mc), mc);
    }

    void calculatePrincipalInterestComponentsForPeriods(final ProgressiveLoanInterestScheduleModel scheduleModel) {
        Money outstandingBalance = Money.zero(scheduleModel.loanProductRelatedDetail().getCurrency());
        for (var repaymentPeriod : scheduleModel.repayments()) {
            repaymentPeriod.setInitialBalance(outstandingBalance);
            calculatePrincipalInterestComponentsForPeriod(repaymentPeriod, null);
            outstandingBalance = repaymentPeriod.getRemainingBalance();
        }
    }

    void calculatePrincipalInterestComponentsForPeriod(final ProgressiveLoanInterestRepaymentModel repaymentPeriod,
            final LocalDate calculateTill) {
        final Money zeroAmount = Money.zero(repaymentPeriod.getInitialBalance().getCurrency());
        Money outstandingBalance = repaymentPeriod.getInitialBalance();
        Money balanceCorrection = zeroAmount;
        Money cumulatedInterest = zeroAmount;

        for (ProgressiveLoanInterestRepaymentInterestPeriod interestPeriod : repaymentPeriod.getInterestPeriods()) {
            final boolean shouldInvalidateInterestPeriod = calculateTill != null && interestPeriod.getDueDate().isAfter(calculateTill);
            if (shouldInvalidateInterestPeriod) {
                interestPeriod.setInterestDue(zeroAmount);
                interestPeriod.setDisbursedAmount(zeroAmount);
                interestPeriod.setCorrectionAmount(zeroAmount);
                continue;
            }
            outstandingBalance = outstandingBalance.plus(interestPeriod.getDisbursedAmount());
            balanceCorrection = balanceCorrection.plus(interestPeriod.getCorrectionAmount());
            final Money calculatedInterest = outstandingBalance.plus(balanceCorrection).multipliedBy(interestPeriod.getRateFactorMinus1());
            interestPeriod.setInterestDue(calculatedInterest);
            cumulatedInterest = cumulatedInterest.plus(calculatedInterest);
        }

        final Money calculatedPrincipal = repaymentPeriod.isLastPeriod() ? outstandingBalance
                : repaymentPeriod.getEqualMonthlyInstallment().minus(cumulatedInterest);

        if (repaymentPeriod.isLastPeriod()) {
            repaymentPeriod.setEqualMonthlyInstallment(calculatedPrincipal.add(cumulatedInterest));
        }

        final Money remainingBalance = outstandingBalance.minus(calculatedPrincipal);
        repaymentPeriod.setPrincipalDue(calculatedPrincipal);
        repaymentPeriod.setRemainingBalance(remainingBalance);
    }

    @Override
    public ProgressiveLoanInterestScheduleModel generateModel(LoanProductRelatedDetail loanProductRelatedDetail,
            Integer installmentAmountInMultiplesOf, List<LoanRepaymentScheduleInstallment> repaymentPeriods, MathContext mc) {
        List<LoanRepaymentScheduleInstallment> repaymentModelsWithoutDownPayment = repaymentPeriods.stream()
                .filter(period -> !period.isDownPayment() && !period.isAdditional()).toList();
        List<ProgressiveLoanInterestRepaymentModel> repaymentModels = progressiveLoanInterestRepaymentModelMapper
                .map(repaymentModelsWithoutDownPayment);

        if (!repaymentModels.isEmpty()) {
            repaymentModels.get(repaymentModels.size() - 1).setLastPeriod(true);
        }
        return new ProgressiveLoanInterestScheduleModel(repaymentModels, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }
}
