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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleParams;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;

@Component
public final class ProgressiveEMICalculator implements EMICalculator {

    private static final BigDecimal DIVISOR_100 = new BigDecimal("100");
    private static final BigDecimal ONE_WEEK_IN_DAYS = BigDecimal.valueOf(7);

    /**
     * Calculate Equal Monthly Installment value and Rate Factor -1 values for calculate Interest
     *
     * @param scheduleParams
     *            Loan Schedule Params
     *
     * @param loanProductRelatedDetail
     *            Loan Product Related Detail from LoanTermApplication
     *
     * @param expectedRepaymentPeriods
     *            Expected Repayment Periods
     *
     * @param mc
     * @return
     */
    @Override
    public EMICalculationResult calculateEMIValueAndRateFactors(final LoanScheduleParams scheduleParams,
            final LoanProductRelatedDetail loanProductRelatedDetail, final List<? extends LoanScheduleModelPeriod> expectedRepaymentPeriods,
            final MathContext mc) {
        // TODO: decide which outstanding balance method needed in the future
        final BigDecimal nominalInterestRatePerPeriod = calcNominalInterestRatePerPeriod(
                loanProductRelatedDetail.getNominalInterestRatePerPeriod(), mc);
        final BigDecimal outstandingBalance = scheduleParams.getOutstandingBalanceAsPerRest().getAmount().compareTo(BigDecimal.ZERO) > 0
                ? scheduleParams.getOutstandingBalanceAsPerRest().getAmount()
                : loanProductRelatedDetail.getPrincipal().getAmount();
        final DaysInYearType daysInYearType = DaysInYearType.fromInt(loanProductRelatedDetail.getDaysInYearType());
        final DaysInMonthType daysInMonthType = DaysInMonthType.fromInt(loanProductRelatedDetail.getDaysInMonthType());
        final PeriodFrequencyType repaymentFrequency = loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
        final BigDecimal repaymentEvery = BigDecimal.valueOf(loanProductRelatedDetail.getRepayEvery());

        final List<BigDecimal> rateFactorList = getRateFactorList(expectedRepaymentPeriods, nominalInterestRatePerPeriod, daysInYearType,
                daysInMonthType, repaymentFrequency, repaymentEvery, mc);

        return calculateEMI(rateFactorList, outstandingBalance, mc);
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
     *
     * @param rateFactorList
     * @param principal
     * @param mc
     * @return
     */
    EMICalculationResult calculateEMI(final List<BigDecimal> rateFactorList, final BigDecimal principal, final MathContext mc) {
        final BigDecimal rateFactorN = MathUtil.stripTrailingZeros(calculateRateFactorN(rateFactorList, mc));
        final BigDecimal fnResult = MathUtil.stripTrailingZeros(calculateFnResult(rateFactorList, mc));

        final BigDecimal emiValue = MathUtil.stripTrailingZeros(calculateEMIValue(rateFactorN, principal, fnResult, mc));
        final List<BigDecimal> rateFactorMinus1List = getRateFactorMinus1List(rateFactorList, mc);

        return new EMICalculationResult(emiValue, rateFactorMinus1List);
    }

    /**
     * Rate factor -1 values
     *
     * @param rateFactors
     * @param mc
     * @return
     */
    List<BigDecimal> getRateFactorMinus1List(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().map(it -> it.subtract(BigDecimal.ONE, mc)).toList();
    }

    /**
     * Calculate Rate Factor Product from rate factors
     *
     * @param rateFactors
     * @param mc
     * @return
     */
    BigDecimal calculateRateFactorN(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().reduce(BigDecimal.ONE, (BigDecimal acc, BigDecimal value) -> acc.multiply(value, mc));
    }

    /**
     * Summarize Fn values
     *
     * @param rateFactors
     * @param mc
     * @return
     */
    BigDecimal calculateFnResult(final List<BigDecimal> rateFactors, final MathContext mc) {
        return rateFactors.stream().skip(1).reduce(BigDecimal.ONE,
                (BigDecimal previousValue, BigDecimal rateFactor) -> fnValue(previousValue, rateFactor, mc));
    }

    /**
     * Calculate the EMI (Equal Monthly Installment) value
     *
     * @param rateFactorN
     * @param principal
     * @param fnResult
     * @param mc
     * @return
     */
    BigDecimal calculateEMIValue(final BigDecimal rateFactorN, final BigDecimal principal, final BigDecimal fnResult,
            final MathContext mc) {
        return rateFactorN.multiply(principal, mc).divide(fnResult, mc);
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
     * @param interestRate
     * @param repaymentEvery
     * @param cumulatedPeriodRatio
     * @param actualDaysInPeriod
     * @param calculatedDaysInPeriod
     * @param mc
     * @return
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
}
