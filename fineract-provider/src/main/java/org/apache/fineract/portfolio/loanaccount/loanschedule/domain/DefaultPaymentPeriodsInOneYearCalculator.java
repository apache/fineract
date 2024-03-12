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
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultPaymentPeriodsInOneYearCalculator implements PaymentPeriodsInOneYearCalculator {

    @Override
    public Integer calculate(final PeriodFrequencyType repaymentFrequencyType,final PeriodFrequencyType interestRateFrequencyMethod) {

        Integer paymentPeriodsInOneYear = 0;
        switch (repaymentFrequencyType) {
            case DAYS:
                paymentPeriodsInOneYear = interestRateFrequencyMethod.isMonthly() ? Integer.valueOf(360) : Integer.valueOf(365);
            break;
            case WEEKS:
                paymentPeriodsInOneYear = interestRateFrequencyMethod.isMonthly() ? Integer.valueOf(48) : Integer.valueOf(52);
            break;
            case MONTHS:
                paymentPeriodsInOneYear = 12;
            break;
            case YEARS:
                paymentPeriodsInOneYear = 1;
            break;
            case INVALID:
                paymentPeriodsInOneYear = 0;
            break;
            case WHOLE_TERM:
                log.error("TODO Implement repaymentFrequencyType for WHOLE_TERM");
            break;
        }
        return paymentPeriodsInOneYear;
    }

    @Override
    public BigDecimal calculatePortionOfRepaymentPeriodInterestChargingGrace(final LocalDate repaymentPeriodStartDate,
            final LocalDate scheduledDueDate, final LocalDate interestChargedFromLocalDate,
            final PeriodFrequencyType repaymentPeriodFrequencyType, final int repaidEvery, MathContext mc) {

        BigDecimal periodFraction = BigDecimal.ZERO;

        final LocalDateInterval repaymentPeriod = new LocalDateInterval(repaymentPeriodStartDate, scheduledDueDate);

        if (interestChargedFromLocalDate != null && repaymentPeriod.fallsBefore(interestChargedFromLocalDate.plusDays(1))) {
            periodFraction = BigDecimal.ONE;
        } else if (interestChargedFromLocalDate != null && repaymentPeriod.contains(interestChargedFromLocalDate)) {

            final int numberOfDaysInterestCalculationGraceInPeriod = Math
                    .toIntExact(ChronoUnit.DAYS.between(repaymentPeriodStartDate, interestChargedFromLocalDate));
            periodFraction = calculateRepaymentPeriodFraction(repaymentPeriodFrequencyType, repaidEvery,
                    numberOfDaysInterestCalculationGraceInPeriod, mc);
        }

        return periodFraction;
    }

    private BigDecimal calculateRepaymentPeriodFraction(final PeriodFrequencyType repaymentPeriodFrequencyType, final int repaidEvery,
            final int numberOfDaysInterestCalculationGrace, final MathContext mc) {

        BigDecimal repayEveryBD = BigDecimal.valueOf(repaidEvery);
        BigDecimal noDaysInterestCalculationGrace = BigDecimal.valueOf(numberOfDaysInterestCalculationGrace);
        return switch (repaymentPeriodFrequencyType) {
            case DAYS -> noDaysInterestCalculationGrace.multiply(repayEveryBD, mc);
            case WEEKS -> noDaysInterestCalculationGrace.divide(BigDecimal.valueOf(7), mc).multiply(repayEveryBD, mc);
            case MONTHS -> noDaysInterestCalculationGrace.divide(BigDecimal.valueOf(30), mc).multiply(repayEveryBD, mc);
            case YEARS -> noDaysInterestCalculationGrace.divide(BigDecimal.valueOf(365), mc).multiply(repayEveryBD, mc);
            case WHOLE_TERM -> {
                log.error("TODO Implement repaymentPeriodFrequencyType for WHOLE_TERM");
                yield BigDecimal.ZERO;
            }
            default -> BigDecimal.ZERO;
        };
    }
}
