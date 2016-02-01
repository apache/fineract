/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

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