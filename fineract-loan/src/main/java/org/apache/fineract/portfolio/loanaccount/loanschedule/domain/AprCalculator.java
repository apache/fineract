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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AprCalculator {

    private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator;

    public BigDecimal calculateFrom(final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal interestRatePerPeriod,
            final Integer numberOfRepayments, final Integer repaymentEvery, final PeriodFrequencyType repaymentPeriodFrequencyType,
            final DaysInYearType daysInYearType) {
        BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
        switch (interestPeriodFrequencyType) {
            case DAYS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(getDaysInYear(daysInYearType)));
            break;
            case WEEKS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(52));
            break;
            case MONTHS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(12));
            break;
            case YEARS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(1));
            break;
            case WHOLE_TERM:
                final BigDecimal ratePerPeriod = interestRatePerPeriod.divide(BigDecimal.valueOf(numberOfRepayments * repaymentEvery), 8,
                        MoneyHelper.getRoundingMode());

                switch (repaymentPeriodFrequencyType) {
                    case DAYS:
                        defaultAnnualNominalInterestRate = ratePerPeriod.multiply(BigDecimal.valueOf(getDaysInYear(daysInYearType)));
                    break;
                    case WEEKS:
                        defaultAnnualNominalInterestRate = ratePerPeriod.multiply(BigDecimal.valueOf(52));
                    break;
                    case MONTHS:
                        defaultAnnualNominalInterestRate = ratePerPeriod.multiply(BigDecimal.valueOf(12));
                    break;
                    case YEARS:
                        defaultAnnualNominalInterestRate = ratePerPeriod.multiply(BigDecimal.valueOf(1));
                    break;
                    case WHOLE_TERM:
                    break;
                    case INVALID:
                    break;
                }
            break;
            case INVALID:
            break;
        }

        return defaultAnnualNominalInterestRate;
    }

    /**
     * Helper method to get the number of days in a year, handling ACTUAL appropriately.
     *
     * When daysInYearType is ACTUAL, this delegates to the PaymentPeriodsInOneYearCalculator (consistent with how
     * Fineract handles ACTUAL elsewhere). For other types (DAYS_360, DAYS_364, DAYS_365), it returns the configured
     * value.
     *
     * @param daysInYearType
     *            the days in year type configuration
     * @return the number of days in a year
     */
    private int getDaysInYear(final DaysInYearType daysInYearType) {
        // When ACTUAL, delegate to calculator (consistent with LoanApplicationTerms.calculatePeriodsInOneYear)
        if (daysInYearType == DaysInYearType.ACTUAL) {
            return paymentPeriodsInOneYearCalculator.calculate(PeriodFrequencyType.DAYS);
        }
        // For DAYS_360, DAYS_364, DAYS_365: use configured value
        return daysInYearType.getValue();
    }

}
