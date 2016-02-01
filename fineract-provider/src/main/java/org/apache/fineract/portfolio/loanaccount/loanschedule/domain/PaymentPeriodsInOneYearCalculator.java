/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public interface PaymentPeriodsInOneYearCalculator {

    Integer calculate(PeriodFrequencyType repaymentFrequencyType);

    double calculatePortionOfRepaymentPeriodInterestChargingGrace(LocalDate repaymentPeriodStartDate, LocalDate scheduledDueDate,
            LocalDate interestChargedFromLocalDate, PeriodFrequencyType repaymentPeriodFrequencyType, Integer repaidEvery);

}