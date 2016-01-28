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

import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class DefaultPaymentPeriodsInOneYearCalculator implements PaymentPeriodsInOneYearCalculator {

    @Override
    public Integer calculate(final PeriodFrequencyType repaymentFrequencyType) {

        Integer paymentPeriodsInOneYear = Integer.valueOf(0);
        switch (repaymentFrequencyType) {
            case DAYS:
                paymentPeriodsInOneYear = Integer.valueOf(365);
            break;
            case WEEKS:
                paymentPeriodsInOneYear = Integer.valueOf(52);
            break;
            case MONTHS:
                paymentPeriodsInOneYear = Integer.valueOf(12);
            break;
            case YEARS:
                paymentPeriodsInOneYear = Integer.valueOf(1);
            break;
            case INVALID:
                paymentPeriodsInOneYear = Integer.valueOf(0);
            break;
        }
        return paymentPeriodsInOneYear;
    }

    @Override
    public double calculatePortionOfRepaymentPeriodInterestChargingGrace(final LocalDate repaymentPeriodStartDate,
            final LocalDate scheduledDueDate, final LocalDate interestChargedFromLocalDate,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer repaidEvery) {

        Double periodFraction = Double.valueOf("0.0");

        final LocalDateInterval repaymentPeriod = new LocalDateInterval(repaymentPeriodStartDate, scheduledDueDate);

        if (interestChargedFromLocalDate != null && repaymentPeriod.fallsBefore(interestChargedFromLocalDate.plusDays(1))) {
            periodFraction = Double.valueOf("1.0");
        } else if (interestChargedFromLocalDate != null && repaymentPeriod.contains(interestChargedFromLocalDate)) {

            final int numberOfDaysInterestCalculationGraceInPeriod = Days.daysBetween(repaymentPeriodStartDate,
                    interestChargedFromLocalDate).getDays();
            periodFraction = calculateRepaymentPeriodFraction(repaymentPeriodFrequencyType, repaidEvery,
                    numberOfDaysInterestCalculationGraceInPeriod);
        }

        return periodFraction;
    }

    private double calculateRepaymentPeriodFraction(final PeriodFrequencyType repaymentPeriodFrequencyType, final Integer every,
            final Integer numberOfDaysInterestCalculationGrace) {

        Double fraction = Double.valueOf("0");
        switch (repaymentPeriodFrequencyType) {
            case DAYS:
                fraction = numberOfDaysInterestCalculationGrace.doubleValue() * every.doubleValue();
            break;
            case WEEKS:
                fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("7.0") * every.doubleValue());
            break;
            case MONTHS:
                fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("30.0") * every.doubleValue());
            break;
            case YEARS:
                fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("365.0") * every.doubleValue());
            break;
            case INVALID:
                fraction = Double.valueOf("0");
            break;
        }
        return fraction;
    }
}