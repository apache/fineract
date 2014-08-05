/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public interface CompoundingPeriod {

    BigDecimal calculateInterest(SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            SavingsInterestCalculationType interestCalculationType, BigDecimal interestFromPreviousPostingPeriod,
            BigDecimal interestRateAsFraction, long daysInYear, BigDecimal minBalanceForInterestCalculation);
}