/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.math.MathContext;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class InterestCompoundingPeriodSummary {

    private final LocalDateInterval periodInterval;
    @SuppressWarnings("unused")
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;
    private final BigDecimal interestEarnedUnrounded;
    private final BigDecimal cumulativeCompoundedInterestToDate;

    public static InterestCompoundingPeriodSummary create(final LocalDateInterval periodInterval, final BigDecimal openingBalance,
            final BigDecimal closingBalance, final BigDecimal interestEarnedUnrounded, final BigDecimal cumulativeCompoundedInterestToDate) {
        return new InterestCompoundingPeriodSummary(periodInterval, openingBalance, closingBalance, interestEarnedUnrounded,
                cumulativeCompoundedInterestToDate);
    }

    public InterestCompoundingPeriodSummary(final LocalDateInterval periodInterval, final BigDecimal openingBalance,
            final BigDecimal closingBalance, final BigDecimal interestEarnedUnrounded, final BigDecimal cumulativeCompoundedInterestToDate) {
        this.periodInterval = periodInterval;
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.interestEarnedUnrounded = interestEarnedUnrounded;
        this.cumulativeCompoundedInterestToDate = cumulativeCompoundedInterestToDate;
    }

    public Money closingBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.closingBalance);
    }

    public BigDecimal closingBalance() {
        return this.closingBalance;
    }

    public BigDecimal closingBalanceWithUnroundedInterest(final MathContext mc) {
        return this.closingBalance.add(interestEarnedUnrounded, mc);
    }

    public BigDecimal compoundedInterest() {
        return cumulativeCompoundedInterestToDate;
    }

    public BigDecimal interestUnrounded() {
        return this.interestEarnedUnrounded;
    }

    public boolean fallsBefore(final LocalDate dateToCheck) {
        return this.periodInterval.fallsBefore(dateToCheck);
    }
}