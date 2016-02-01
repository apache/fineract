/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
public class AprCalculator {

    public BigDecimal calculateFrom(final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal interestRatePerPeriod) {
        BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
        switch (interestPeriodFrequencyType) {
            case DAYS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(365));
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
            case INVALID:
            break;
        }

        return defaultAnnualNominalInterestRate;
    }
}