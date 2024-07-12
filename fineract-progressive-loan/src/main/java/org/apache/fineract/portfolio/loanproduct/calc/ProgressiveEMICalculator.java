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
import java.util.List;
import java.util.Objects;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.PrincipalInterest;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;

@Component
public final class ProgressiveEMICalculator implements EMICalculator {

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     */
    @Override
    public EMICalculationResult calculateEMIValueAndRateFactors(final Money outstandingBalanceAsPerRest,
            final LoanProductRelatedDetail loanProductRelatedDetail, final List<? extends LoanScheduleModelPeriod> expectedRepaymentPeriods,
            final Integer actualPeriodNumber, final Integer numberOfRepayments, final MathContext mc) {
        final BigDecimal nominalInterestRatePerPeriod = calcNominalInterestRatePerPeriod(
                loanProductRelatedDetail.getNominalInterestRatePerPeriod(), mc);
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        final List<BigDecimal> rateFactorList = getRateFactorList(expectedRepaymentPeriods, nominalInterestRatePerPeriod, daysInYearType,
                daysInMonthType, repaymentFrequency, repaymentEvery, mc);

        return calculateEMI(rateFactorList, actualPeriodNumber, numberOfRepayments, outstandingBalanceAsPerRest, mc);
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
    BigDecimal calcNominalInterestRatePerPeriod(final BigDecimal interestRate, MathContext mc) {
        return MathUtil.nullToZero(interestRate).divide(DIVISOR_100, mc);
    }

    /**
     * * Calculate rate factors from ONLY repayment periods
     *
     * @param expectedRepaymentPeriods
     * @param interestRate
     * @param daysInYearType
     * @param daysInMonthType
     * @param repaymentFrequency
     * @param repaymentEvery
     * @param mc
     * @return
     */
    List<BigDecimal> getRateFactorList(final List<? extends LoanScheduleModelPeriod> expectedRepaymentPeriods,
            final BigDecimal interestRate, final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType,
            final PeriodFrequencyType repaymentFrequency, final BigDecimal repaymentEvery, final MathContext mc) {
        return expectedRepaymentPeriods.stream().filter(LoanScheduleModelPeriod::isRepaymentPeriod)
                .map(period -> MathUtil.stripTrailingZeros(calculateRateFactorPerPeriod(period, interestRate, daysInYearType,
                        daysInMonthType, repaymentFrequency, repaymentEvery, mc)))
                .toList();
    }

    /**
     * Calculate Rate Factor for an exact Period
     *
     * @param repaymentPeriod
     * @param interestRate
     * @param daysInYearType
     * @param daysInMonthType
     * @param repaymentFrequency
     * @param repaymentEvery
     * @param mc
     * @return
     */
    BigDecimal calculateRateFactorPerPeriod(final LoanScheduleModelPeriod repaymentPeriod, final BigDecimal interestRate,
            final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType, final PeriodFrequencyType repaymentFrequency,
            final BigDecimal repaymentEvery, final MathContext mc) {
        BigDecimal daysInMonth = BigDecimal.valueOf(daysInMonthType.getNumberOfDays(repaymentPeriod.periodFromDate()));
        BigDecimal daysInYear = BigDecimal.valueOf(daysInYearType.getNumberOfDays(repaymentPeriod.periodFromDate()));
        final BigDecimal actualDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.periodFromDate(), repaymentPeriod.periodDueDate()));
        final BigDecimal calculatedDaysInPeriod = BigDecimal
                .valueOf(DateUtils.getDifferenceInDays(repaymentPeriod.periodFromDate(), repaymentPeriod.periodDueDate()));
        final int numberOfYearsDifferenceInPeriod = repaymentPeriod.periodDueDate().getYear() - repaymentPeriod.periodFromDate().getYear();
        final boolean partialPeriodCalculationNeeded = daysInYearType == DaysInYearType.ACTUAL && numberOfYearsDifferenceInPeriod > 0;

        // TODO check: loanApplicationTerms.calculatePeriodsBetweenDates(startDate, endDate); // calculate period data
        // TODO review: (repayment frequency: days, weeks, years; validation day is month fix 30)
        if (partialPeriodCalculationNeeded) {
            final BigDecimal cumulatedPeriodFractions = calculatePeriodFractions(repaymentPeriod, mc);
            return rateFactorByRepaymentPartialPeriod(interestRate, repaymentEvery, cumulatedPeriodFractions, BigDecimal.ONE,
                    BigDecimal.ONE, mc);
        }

        return calculateRateFactorPerPeriodBasedOnRepaymentFrequency(interestRate, repaymentFrequency, repaymentEvery, daysInMonth,
                daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
    }

    /**
     * Calculate Period fractions part based on how much year has in the period
     *
     * @param repaymentPeriod
     * @param mc
     * @return
     */
    BigDecimal calculatePeriodFractions(LoanScheduleModelPeriod repaymentPeriod, MathContext mc) {
        BigDecimal cumulatedRateFactor = BigDecimal.ZERO;
        int actualYear = repaymentPeriod.periodFromDate().getYear();
        int endYear = repaymentPeriod.periodDueDate().getYear();
        LocalDate actualDate = repaymentPeriod.periodFromDate();
        LocalDate endOfActualYear;

        while (actualYear <= endYear) {
            endOfActualYear = actualYear == endYear ? repaymentPeriod.periodDueDate() : LocalDate.of(actualYear, 12, 31);
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
    BigDecimal calculateRateFactorPerPeriodBasedOnRepaymentFrequency(final BigDecimal interestRate,
            final PeriodFrequencyType repaymentFrequency, final BigDecimal repaymentEvery, final BigDecimal daysInMonth,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return switch (repaymentFrequency) {
            case DAYS ->
                rateFactorByRepaymentEveryDay(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
            case WEEKS ->
                rateFactorByRepaymentEveryWeek(interestRate, repaymentEvery, daysInYear, actualDaysInPeriod, calculatedDaysInPeriod, mc);
            case MONTHS -> rateFactorByRepaymentEveryMonth(interestRate, repaymentEvery, daysInMonth, daysInYear, actualDaysInPeriod,
                    calculatedDaysInPeriod, mc);
            default -> throw new UnsupportedOperationException("Invalid repayment frequency"); // not supported yet
        };
    }

    /**
     * Calculate EMI parts and return an EMI calculation result object with repayment installment rate factors
     */
    EMICalculationResult calculateEMI(final List<BigDecimal> rateFactorList, final Integer actualPeriodNumber,
            final Integer numberOfRepayments, final Money outstandingBalanceForRest, final MathContext mc) {
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorN(rateFactorList, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(rateFactorList, mc));

        final Money emiValue = Money.of(outstandingBalanceForRest.getCurrency(),
                calculateEMIValue(rateFactorN, outstandingBalanceForRest.getAmount(), fnResult, mc));
        final List<BigDecimal> rateFactorMinus1List = getRateFactorMinus1List(rateFactorList, mc);

        final Money adjustedEqualMonthlyInstallmentValue = adjustEMIForMoreStreamlinedRepaymentSchedule(actualPeriodNumber,
                numberOfRepayments, outstandingBalanceForRest, emiValue, rateFactorMinus1List, mc);

        return new EMICalculationResult(adjustedEqualMonthlyInstallmentValue, rateFactorMinus1List);
    }

    /**
     * Due to rounding or unequal installments, the first calculated EMI might not be the best one! Reiterate with
     * adjusted EMI to get a better streamlined repayment schedule (less difference between calculated EMI and last
     * installment EMI).
     */
    Money adjustEMIForMoreStreamlinedRepaymentSchedule(final Integer actualPeriodNumber, final Integer numberOfRepayments,
            final Money outstandingBalanceAsPerRest, final Money equalMonthlyInstallmentValue, List<BigDecimal> rateFactorMinus1List,
            final MathContext mc) {
        int numberOfUpcomingPeriods = numberOfRepayments - actualPeriodNumber + 1;
        if (numberOfUpcomingPeriods < 2) {
            return equalMonthlyInstallmentValue;
        }

        RepaymentScheduleModel repaymentScheduleModel = generateRepaymentScheduleModel(numberOfRepayments, outstandingBalanceAsPerRest,
                equalMonthlyInstallmentValue, rateFactorMinus1List);
        Money calculatedLastEMI = repaymentScheduleModel.getScheduleList().get(repaymentScheduleModel.getScheduleList().size() - 1).emi();
        Money originalDifference = calculatedLastEMI.minus(equalMonthlyInstallmentValue);
        if (originalDifference.isZero()) {
            return equalMonthlyInstallmentValue;
        }
        double lowerHalfOfUpcomingPeriods = Math.floor((double) numberOfUpcomingPeriods / 2);
        if (lowerHalfOfUpcomingPeriods == 0.0) {
            return equalMonthlyInstallmentValue;
        }
        boolean shouldBeAdjusted = originalDifference.abs().multipliedBy(100)
                .isGreaterThan(Money.of(equalMonthlyInstallmentValue.getCurrency(), BigDecimal.valueOf(lowerHalfOfUpcomingPeriods)));
        // Reiterate only when needed
        if (shouldBeAdjusted) {
            Money adjustment = originalDifference.dividedBy(numberOfUpcomingPeriods, mc.getRoundingMode());

            Money adjustedEqualMonthlyInstallmentValue = equalMonthlyInstallmentValue.plus(adjustment);
            RepaymentScheduleModel repaymentScheduleModelWithAdjustedEMI = generateRepaymentScheduleModel(numberOfRepayments,
                    outstandingBalanceAsPerRest, adjustedEqualMonthlyInstallmentValue, rateFactorMinus1List);
            Money calculatedLastEMIAfterAdjustment = repaymentScheduleModelWithAdjustedEMI.getScheduleList()
                    .get(repaymentScheduleModelWithAdjustedEMI.getScheduleList().size() - 1).emi();
            Money differenceAfterEMIAdjustment = calculatedLastEMIAfterAdjustment.minus(adjustedEqualMonthlyInstallmentValue);
            // Use the adjusted EMI only if it is better than the original one
            return differenceAfterEMIAdjustment.abs().isLessThan(originalDifference.abs()) ? adjustedEqualMonthlyInstallmentValue
                    : equalMonthlyInstallmentValue;
        } else {
            return equalMonthlyInstallmentValue;
        }
    }

    RepaymentScheduleModel generateRepaymentScheduleModel(final Integer numberOfRepayments, final Money outstandingBalanceAsPerRest,
            Money equalMonthlyInstallmentValue, List<BigDecimal> rateFactorMinus1List) {
        RepaymentScheduleModel repaymentScheduleModel = new RepaymentScheduleModel();
        Money balanceOfLoan = outstandingBalanceAsPerRest;
        for (int i = 0; i < numberOfRepayments; i++) {
            final Money calculatedInterest = balanceOfLoan.multipliedBy(rateFactorMinus1List.get(i));
            // WE need to calculate EMI differently for last installment (decided by number of repayments or when
            // schedule got shorter then planned)
            if (balanceOfLoan.isLessThan(equalMonthlyInstallmentValue.minus(calculatedInterest)) || i == numberOfRepayments - 1) {
                equalMonthlyInstallmentValue = balanceOfLoan.plus(calculatedInterest);
            }
            final Money calculatedPrincipal = equalMonthlyInstallmentValue.minus(calculatedInterest);
            repaymentScheduleModel.addRepaymentPeriodModel(
                    new RepaymentPeriodModel(balanceOfLoan, equalMonthlyInstallmentValue, calculatedInterest, calculatedPrincipal));
            balanceOfLoan = balanceOfLoan.minus(calculatedPrincipal);
            // We can stop processing if there is no outstanding principal
            if (balanceOfLoan.isZero()) {
                break;
            }
        }
        return repaymentScheduleModel;
    }

    /**
     * Rate factor -1 values
     *
     */
    List<BigDecimal> getRateFactorMinus1List(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().map(it -> it.subtract(BigDecimal.ONE, mc)).toList();
    }

    /**
     * Calculate Rate Factor Product from rate factors
     */
    BigDecimal calculateRateFactorN(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().reduce(BigDecimal.ONE, (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     */
    BigDecimal calculateFnResult(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().skip(1).reduce(BigDecimal.ONE,
                (BigDecimal previousValue, BigDecimal rateFactor) -> fnValue(previousValue, rateFactor, mc));
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
    BigDecimal rateFactorByRepaymentEveryDay(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInYear,
            final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, BigDecimal.ONE, repaymentEvery, daysInYear, actualDaysInPeriod,
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
    BigDecimal rateFactorByRepaymentEveryWeek(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInYear,
            final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, ONE_WEEK_IN_DAYS, repaymentEvery, daysInYear, actualDaysInPeriod,
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
    BigDecimal rateFactorByRepaymentEveryMonth(final BigDecimal interestRate, final BigDecimal repaymentEvery, final BigDecimal daysInMonth,
            final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        return rateFactorByRepaymentPeriod(interestRate, daysInMonth, repaymentEvery, daysInYear, actualDaysInPeriod,
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
    BigDecimal rateFactorByRepaymentPeriod(final BigDecimal interestRate, final BigDecimal repaymentPeriodMultiplierInDays,
            final BigDecimal repaymentEvery, final BigDecimal daysInYear, final BigDecimal actualDaysInPeriod,
            final BigDecimal calculatedDaysInPeriod, final MathContext mc) {
        final BigDecimal interestFractionPerPeriod = repaymentPeriodMultiplierInDays.multiply(repaymentEvery, mc).divide(daysInYear, mc);
        return BigDecimal.ONE.add(
                interestRate.multiply(interestFractionPerPeriod, mc).multiply(actualDaysInPeriod, mc).divide(calculatedDaysInPeriod, mc),
                mc);
    }

    /**
     * Calculate Rate Factor based on Partial Period
     *
     */
    BigDecimal rateFactorByRepaymentPartialPeriod(final BigDecimal interestRate, final BigDecimal repaymentEvery,
            final BigDecimal cumulatedPeriodRatio, final BigDecimal actualDaysInPeriod, final BigDecimal calculatedDaysInPeriod,
            final MathContext mc) {
        final BigDecimal interestFractionPerPeriod = repaymentEvery.multiply(cumulatedPeriodRatio);
        return BigDecimal.ONE.add(
                interestRate.multiply(interestFractionPerPeriod, mc).multiply(actualDaysInPeriod, mc).divide(calculatedDaysInPeriod, mc),
                mc);
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

    @Override
    public PrincipalInterest calculatePrincipalInterestComponentsForPeriod(final EMICalculationResult emiCalculationResult,
            final Money outstandingBalanceAsPerRest, final Integer installmentAmountInMultiplesOf, final Integer actualPeriodNumber,
            final Integer actualNoOfRepayments, final MathContext mc) {

        final Money equalMonthlyInstallmentValue = installmentAmountInMultiplesOf != null
                ? Money.roundToMultiplesOf(emiCalculationResult.getEqualMonthlyInstallmentValue(), installmentAmountInMultiplesOf)
                : emiCalculationResult.getEqualMonthlyInstallmentValue();
        final BigDecimal rateFactorMinus1 = emiCalculationResult.getNextRepaymentPeriodRateFactorMinus1();
        final Money calculatedInterest = outstandingBalanceAsPerRest.multipliedBy(rateFactorMinus1);
        final Money calculatedPrincipal = equalMonthlyInstallmentValue.minus(calculatedInterest);
        return new PrincipalInterest(
                adjustCalculatedPrincipalWithRemainingBalanceInLastPeriod(calculatedPrincipal, outstandingBalanceAsPerRest,
                        actualPeriodNumber, actualNoOfRepayments),
                calculatedInterest, Money.zero(equalMonthlyInstallmentValue.getCurrency()));
    }

    Money adjustCalculatedPrincipalWithRemainingBalanceInLastPeriod(final Money calculatedPrincipal,
            final Money outstandingBalanceAsPerRest, final Integer actualPeriodNumber, final Integer actualNoOfRepayments) {
        final boolean isLastRepaymentPeriod = Objects.equals(actualPeriodNumber, actualNoOfRepayments);
        if (isLastRepaymentPeriod) {
            final Money remainingAmount = outstandingBalanceAsPerRest.minus(calculatedPrincipal);
            return calculatedPrincipal.plus(remainingAmount);
        }
        return calculatedPrincipal;
    }
}
