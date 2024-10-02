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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.EmiInterestPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.EmiRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ProgressiveEMICalculator implements EMICalculator {

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    @Override
    public ProgressiveLoanInterestScheduleModel generateInterestScheduleModel(final List<LoanScheduleModelRepaymentPeriod> periods,
            final LoanProductRelatedDetail loanProductRelatedDetail, final Integer installmentAmountInMultiplesOf, final MathContext mc) {
        final Money zeroAmount = Money.zero(loanProductRelatedDetail.getCurrency());
        final ArrayList<EmiRepaymentPeriod> interestRepaymentModelList = new ArrayList<>(periods.size());
        EmiRepaymentPeriod previousPeriod = null;
        for (final LoanScheduleModelRepaymentPeriod period : periods) {
            EmiRepaymentPeriod currentPeriod = new EmiRepaymentPeriod(period.periodFromDate(), period.periodDueDate(), zeroAmount,
                    previousPeriod);
            if (previousPeriod != null) {
                previousPeriod.setNext(currentPeriod);
            }
            previousPeriod = currentPeriod;
            interestRepaymentModelList.add(currentPeriod);

        }
        return new ProgressiveLoanInterestScheduleModel(interestRepaymentModelList, loanProductRelatedDetail,
                installmentAmountInMultiplesOf, mc);
    }

    @Override
    public Optional<EmiRepaymentPeriod> findRepaymentPeriod(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LocalDate repaymentPeriodDueDate) {
        if (scheduleModel == null) {
            return Optional.empty();
        }
        return scheduleModel.findRepaymentPeriod(repaymentPeriodDueDate);
    }

    /**
     * Add disbursement to Interest Period
     */
    @Override
    public void addDisbursement(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate repaymentPeriodDueDate,
            final LocalDate disbursementDueDate, final Money disbursedAmount) {
        scheduleModel.resetInterestPeriodsAssociations(); // TODO: recalculate interest from the beginning
        scheduleModel
                .changeOutstandingBalanceAndUpdateInterestPeriods(repaymentPeriodDueDate, disbursementDueDate, disbursedAmount,
                        Money.zero(disbursedAmount.getCurrency()))
                .ifPresent((repaymentPeriod) -> calculateEMIValueAndRateFactors(repaymentPeriod.getDueDate(), scheduleModel));
    }

    // private static Predicate<EmiInterestPeriod> operationRelatedPreviousInterestPeriod(EmiRepaymentPeriod
    // repaymentPeriod,
    // LocalDate operationDate) {
    // return interestPeriod -> operationDate.isAfter(interestPeriod.getFromDate())
    // && (operationDate.isBefore(interestPeriod.getDueDate()) ||
    // (repaymentPeriod.getDueDate().equals(interestPeriod.getDueDate())
    // && !operationDate.isBefore(repaymentPeriod.getDueDate())));
    // }

    @Override
    public void changeInterestRate(final ProgressiveLoanInterestScheduleModel scheduleModel, final LocalDate newInterestSubmittedOnDate,
            final BigDecimal newInterestRate) {
        scheduleModel.resetInterestPeriodsAssociations(); // TODO: recalculate interest from the beginning
        var interestPeriod = scheduleModel.addInterestRate(newInterestSubmittedOnDate, newInterestRate);
        calculateEMIValueAndRateFactors(interestPeriod.getRepaymentPeriod().getDueDate(), scheduleModel);
    }

    @Override
    public void addBalanceCorrection(ProgressiveLoanInterestScheduleModel scheduleModel, LocalDate repaymentPeriodDueDate,
            LocalDate balanceCorrectionDate, Money balanceCorrectionAmount) {
        final Money zeroAmount = Money.zero(balanceCorrectionAmount.getCurrency());
        scheduleModel.changeOutstandingBalanceAndUpdateInterestPeriods(repaymentPeriodDueDate, balanceCorrectionDate, zeroAmount,
                balanceCorrectionAmount).ifPresent(repaymentPeriod -> {
                    calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel);
                    calculatePrincipalInterestComponentsForPeriods(scheduleModel);
                });
    }

    @Override
    public Optional<EmiRepaymentPeriod> getPayableDetails(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LocalDate periodDueDate, final LocalDate payDate) {
        return scheduleModel.deepCopy().insertVirtualInterestPeriod(periodDueDate, payDate).stream().peek(repaymentPeriod -> {
            calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel);
            calculatePrincipalInterestComponentsForPeriod(repaymentPeriod, payDate);
        }).findFirst();
    }

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     */
    void calculateEMIValueAndRateFactors(final LocalDate calculateFromRepaymentPeriodDueDate,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        final List<EmiRepaymentPeriod> relatedRepaymentPeriods = scheduleModel
                .getRelatedRepaymentPeriods(calculateFromRepaymentPeriodDueDate);
        calculateRateFactorMinus1ForPeriods(relatedRepaymentPeriods, scheduleModel);
        calculateEMIOnPeriods(relatedRepaymentPeriods, scheduleModel);
        calculatePrincipalInterestComponentsForPeriods(scheduleModel);
        checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(scheduleModel, relatedRepaymentPeriods);
    }

    private void checkAndAdjustEmiIfNeededOnRelatedRepaymentPeriods(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final List<EmiRepaymentPeriod> relatedRepaymentPeriods) {
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
            final var newScheduleModel = scheduleModel.deepCopy();
            newScheduleModel.repaymentPeriods().forEach(period -> {
                if (!period.getDueDate().isBefore(relatedPeriodsFirstDueDate)) {
                    period.setEqualMonthlyInstallment(adjustedEqualMonthlyInstallmentValue);
                }
            });
            calculatePrincipalInterestComponentsForPeriods(newScheduleModel);
            final Money newEmiDifference = getDifferenceBetweenLastTwoPeriod(newScheduleModel.repaymentPeriods(), scheduleModel);
            final boolean newEmiHasLessDifference = newEmiDifference.abs().compareTo(emiDifference.abs()) < 0;
            if (!newEmiHasLessDifference) {
                return;
            }

            final Iterator<EmiRepaymentPeriod> relatedPeriodFromNewModelIterator = newScheduleModel.repaymentPeriods().stream()//
                    .filter(period -> !period.getDueDate().isBefore(relatedPeriodsFirstDueDate))//
                    .toList().iterator();//

            relatedRepaymentPeriods.forEach(relatedRepaymentPeriod -> {
                if (!relatedPeriodFromNewModelIterator.hasNext()) {
                    return;
                }
                final EmiRepaymentPeriod newRepaymentPeriod = relatedPeriodFromNewModelIterator.next();
                relatedRepaymentPeriod.setEqualMonthlyInstallment(newRepaymentPeriod.getEqualMonthlyInstallment());
                relatedRepaymentPeriod.setPrincipalDue(newRepaymentPeriod.getPrincipalDue());
                relatedRepaymentPeriod.setRemainingBalance(newRepaymentPeriod.getRemainingBalance());
                relatedRepaymentPeriod.updateInterestPeriods(newRepaymentPeriod.getInterestPeriods());
                // TODO: fix interest periods
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
    void calculateRateFactorMinus1ForPeriods(final List<EmiRepaymentPeriod> repaymentPeriods,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriods.forEach(repaymentPeriod -> calculateRateFactorMinus1ForRepaymentPeriod(repaymentPeriod, scheduleModel));
    }

    void calculateRateFactorMinus1ForRepaymentPeriod(final EmiRepaymentPeriod repaymentPeriod,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
        repaymentPeriod.getInterestPeriods().forEach(interestPeriod -> interestPeriod
                .setRateFactorMinus1(calculateRateFactorMinus1PerPeriod(repaymentPeriod, interestPeriod, scheduleModel)));
    }

    /**
     * Calculate Rate Factor-1 for an exact Period
     */
    BigDecimal calculateRateFactorMinus1PerPeriod(final EmiRepaymentPeriod repaymentPeriod, final EmiInterestPeriod interestPeriod,
            final ProgressiveLoanInterestScheduleModel scheduleModel) {
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
        final BigDecimal calculatedDaysInPeriod = BigDecimal.valueOf(DateUtils.getDifferenceInDays(
                interestPeriod.getOriginalRepaymentPeriod().getFromDate(), interestPeriod.getOriginalRepaymentPeriod().getDueDate()));
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
    BigDecimal calculatePeriodFractions(EmiInterestPeriod interestPeriod, MathContext mc) {
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

    void calculateEMIOnPeriods(final List<EmiRepaymentPeriod> repaymentPeriods, final ProgressiveLoanInterestScheduleModel scheduleModel) {
        if (repaymentPeriods.isEmpty()) {
            return;
        }
        final MathContext mc = scheduleModel.mc();
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorN(repaymentPeriods, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(repaymentPeriods, mc));
        final var startPeriod = repaymentPeriods.get(0);
        final Money remainingBalanceFromPreviousPeriod = startPeriod.getInitialBalance();
        final Money outstandingBalance = remainingBalanceFromPreviousPeriod.add(startPeriod.getDisbursedAmountInPeriod());

        final Money equalMonthlyInstallment = Money.of(outstandingBalance.getCurrency(),
                calculateEMIValue(rateFactorN, outstandingBalance.getAmount(), fnResult, mc));
        final Money finalEqualMonthlyInstallment = applyInstallmentAmountInMultiplesOf(scheduleModel, equalMonthlyInstallment);

        repaymentPeriods.forEach(period -> period.setEqualMonthlyInstallment(finalEqualMonthlyInstallment));
    }

    Money applyInstallmentAmountInMultiplesOf(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final Money equalMonthlyInstallment) {
        return scheduleModel.installmentAmountInMultiplesOf() != null
                ? Money.roundToMultiplesOf(equalMonthlyInstallment, scheduleModel.installmentAmountInMultiplesOf())
                : equalMonthlyInstallment;
    }

    Money getDifferenceBetweenLastTwoPeriod(final List<EmiRepaymentPeriod> repaymentPeriods,
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
    BigDecimal calculateRateFactorN(final List<EmiRepaymentPeriod> periods, final MathContext mc) {
        return periods.stream().map(EmiRepaymentPeriod::getRateFactor).reduce(BigDecimal.ONE,
                (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     */
    BigDecimal calculateFnResult(final List<EmiRepaymentPeriod> periods, final MathContext mc) {
        return periods.stream()//
                .skip(1)//
                .map(EmiRepaymentPeriod::getRateFactor)//
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
        for (var repaymentPeriod : scheduleModel.repaymentPeriods()) {
            calculatePrincipalInterestComponentsForPeriod(repaymentPeriod, null);
        }
    }

    void calculatePrincipalInterestComponentsForPeriod(final EmiRepaymentPeriod repaymentPeriod, final LocalDate calculateTill) {
        final Money zeroAmount = Money.zero(repaymentPeriod.getInitialBalance().getCurrency());
        Money outstandingBalance = repaymentPeriod.getInitialBalance();
        Money balanceCorrection = zeroAmount;
        Money cumulatedInterest = zeroAmount;

        var movedInterestPeriods = new ArrayList<EmiInterestPeriod>();
        boolean moveAllTheRestAsWell = false;
        for (EmiInterestPeriod interestPeriod : repaymentPeriod.getInterestPeriods()) {
            final boolean shouldInvalidateInterestPeriod = calculateTill != null && interestPeriod.getDueDate().isAfter(calculateTill);
            if (shouldInvalidateInterestPeriod) {
                interestPeriod.setInterestDue(zeroAmount);
                interestPeriod.setDisbursedAmount(zeroAmount);
                interestPeriod.setCorrectionAmount(zeroAmount);
                continue;
            }

            // disbursements and balance correction are counted on own repayment period
            final Money outstandingBalanceForInterestCalculation = outstandingBalance.plus(interestPeriod.getDisbursedAmount())
                    .plus(interestPeriod.getCorrectionAmount());

            final Money calculatedInterest = outstandingBalanceForInterestCalculation.multipliedBy(interestPeriod.getRateFactorMinus1());
            interestPeriod.setInterestDue(calculatedInterest);

            // if (interestPeriod.isAssignedOwnRepaymentPeriod()) {
            balanceCorrection = balanceCorrection.plus(interestPeriod.getCorrectionAmount());
            // }

            final boolean cumulatedInterestAndBalanceChangeIsGreaterThanEmiValue = balanceCorrection //
                    .abs() //
                    .plus(cumulatedInterest) //
                    .plus(calculatedInterest) //
                    .isGreaterThan(repaymentPeriod.getEqualMonthlyInstallment()); //
            final boolean shouldMoveToNextRepaymentPeriod = (!repaymentPeriod.isLastPeriod()
                    && cumulatedInterestAndBalanceChangeIsGreaterThanEmiValue) || moveAllTheRestAsWell;
            if (shouldMoveToNextRepaymentPeriod) {
                balanceCorrection = balanceCorrection.minus(interestPeriod.getCorrectionAmount());
                movedInterestPeriods.add(interestPeriod);
                moveInterestPeriodToNextRepaymentPeriod(interestPeriod);
                moveAllTheRestAsWell = true;
            } else {
                cumulatedInterest = cumulatedInterest.plus(calculatedInterest);
                outstandingBalance = outstandingBalanceForInterestCalculation;
            }
        }

        repaymentPeriod.getInterestPeriods().removeAll(movedInterestPeriods);

        final Money calculatedPrincipal = repaymentPeriod.isLastPeriod() ? outstandingBalance.minus(balanceCorrection)
                : repaymentPeriod.getEqualMonthlyInstallment().minus(cumulatedInterest);

        if (repaymentPeriod.isLastPeriod()) {
            repaymentPeriod.setEqualMonthlyInstallment(calculatedPrincipal.add(cumulatedInterest));
        }

        // We dont wanna deduct twice... balance correction means some of the principal was
        final Money remainingBalance = outstandingBalance.minus(calculatedPrincipal).minus(balanceCorrection);
        repaymentPeriod.setPrincipalDue(calculatedPrincipal);
        repaymentPeriod.setRemainingBalance(remainingBalance);
    }

    void moveInterestPeriodToNextRepaymentPeriod(final EmiInterestPeriod interestPeriod) {
        interestPeriod.getRepaymentPeriod().getNext().ifPresent(nextRepaymentPeriod -> {
            interestPeriod.setRepaymentPeriod(nextRepaymentPeriod);
            nextRepaymentPeriod.addInterestPeriod(interestPeriod);
            interestPeriod.setCorrectionAmount(interestPeriod.getCorrectionAmount().zero());
        });
    }

    @Override
    public ProgressiveLoanInterestScheduleModel generateModel(LoanProductRelatedDetail loanProductRelatedDetail,
            Integer installmentAmountInMultiplesOf, List<LoanRepaymentScheduleInstallment> repaymentPeriods, MathContext mc) {
        List<LoanRepaymentScheduleInstallment> repaymentModelsWithoutDownPayment = repaymentPeriods.stream()
                .filter(period -> !period.isDownPayment() && !period.isAdditional()).toList();

        List<EmiRepaymentPeriod> repaymentModels = new ArrayList<>();
        EmiRepaymentPeriod previousPeriod = null;
        for (var repaymentModel : repaymentModelsWithoutDownPayment) {
            EmiRepaymentPeriod currentPeriod = new EmiRepaymentPeriod(repaymentModel.getFromDate(), repaymentModel.getDueDate(),
                    Money.zero(repaymentModel.getLoan().getCurrency()), previousPeriod);
            if (previousPeriod != null) {
                previousPeriod.setNext(currentPeriod);
            }
            previousPeriod = currentPeriod;
            repaymentModels.add(currentPeriod);
        }

        return new ProgressiveLoanInterestScheduleModel(repaymentModels, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);
    }
}
