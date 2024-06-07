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
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EMICalculator {

    /**
     * Calculate EMI parts and return an EMI calculation result object with repayment installment rate factors
     *
     * @param repaymentPeriodDays
     *            List of day gaps between periods (zero interest period values should be 0)
     * @param principal
     * @param interestRate
     * @param daysInYear
     * @param mc
     * @return
     */
    public EMICalculationResult calculateEMI(final List<Integer> repaymentPeriodDays, final BigDecimal principal,
            final BigDecimal interestRate, final Integer daysInYear, final MathContext mc) {
        final List<BigDecimal> rateFactorList = getRateFactorList(repaymentPeriodDays, interestRate, daysInYear, mc);
        final BigDecimal rateFactorN = calculateRateFactorN(rateFactorList, mc);
        final BigDecimal fnResult = calculateFnResult(rateFactorList, mc);

        final BigDecimal emiValue = calculateEMIValue(rateFactorN, principal, fnResult, mc);
        final List<BigDecimal> rateFactorMinus1List = getRateFactorMinus1List(rateFactorList, mc);

        return new EMICalculationResult(emiValue, rateFactorMinus1List);
    }

    /**
     * Calculate rate factors from repayment periods
     *
     * @param repaymentPeriodDays
     * @param interestRate
     * @param daysInYear
     * @param mc
     * @return
     */
    List<BigDecimal> getRateFactorList(final List<Integer> repaymentPeriodDays, final BigDecimal interestRate, final Integer daysInYear,
            final MathContext mc) {
        return repaymentPeriodDays.stream().map(daysInPeriod -> rateFactor(interestRate, daysInPeriod, daysInYear, mc)).toList();
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
     * To calculate the monthly payment, we first need to calculate something called the Rate Factor. We're going to be
     * using simple interest. The Rate Factor for simple interest is calculated by the following formula:
     *
     *
     * R = 1 + (r * d / y)
     *
     * @param interestRate
     *            (r)
     * @param daysInPeriod
     *            (d)
     * @param daysInYear
     *            (y)
     */
    BigDecimal rateFactor(final BigDecimal interestRate, final Integer daysInPeriod, final Integer daysInYear, final MathContext mc) {
        final BigDecimal daysPeriod = BigDecimal.valueOf(daysInPeriod);
        final BigDecimal daysYear = BigDecimal.valueOf(daysInYear);

        return BigDecimal.ONE.add(interestRate.multiply(daysPeriod.divide(daysYear, mc), mc), mc);
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
