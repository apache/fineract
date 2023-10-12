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
package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

@Getter
@RequiredArgsConstructor
public class SavingsAccountTransactionDetailsForPostingPeriod {

    private final Long id;
    private final LocalDate transactionDate;
    private final LocalDate endOfBalanceDate;
    private final BigDecimal runningBalance;
    private final BigDecimal amount;
    private final MonetaryCurrency currency;
    private final Integer balanceNumberOfDays;
    private final boolean isDeposit;
    private final boolean isWithdrawal;
    private final boolean isAllowOverdraft;
    private final boolean isChargeTransactionAndNotReversed;
    private final boolean isDividendPayoutAndNotReversed;

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceDate());
        return periodInterval.contains(balanceInterval);
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public boolean occursOn(final LocalDate occursOnDate) {
        return DateUtils.isEqual(occursOnDate, getTransactionDate());
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance) {
        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit() || isDividendPayoutAndNotReversed()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {

            if (openingBalance.isGreaterThanZero() || isAllowOverdraft()) {
                endOfDayBalance = openingBalance.minus(getAmount(currency));
            } else {
                endOfDayBalance = Money.of(currency, this.runningBalance);
            }
        }

        return EndOfDayBalance.from(getTransactionDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalance(final LocalDateInterval periodInterval, final MonetaryCurrency currency) {

        final Money endOfDayBalance = Money.of(currency, this.runningBalance);
        final Money openingBalance = endOfDayBalance;

        LocalDate balanceDate = periodInterval.startDate();

        int numberOfDays = periodInterval.daysInPeriodInclusiveOfEndDate();
        if (periodInterval.contains(getTransactionDate())) {
            balanceDate = getTransactionDate();
            final LocalDateInterval newInterval = LocalDateInterval.create(getTransactionDate(), periodInterval.endDate());
            numberOfDays = newInterval.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceDate, openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalanceBoundedBy(final Money openingBalance, final LocalDateInterval boundedBy) {
        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();

        int numberOfDaysOfBalance = this.balanceNumberOfDays;

        LocalDate balanceStartDate = getTransactionDate();
        LocalDate balanceEndDate = getEndOfBalanceDate();

        if (DateUtils.isAfter(boundedBy.startDate(), balanceStartDate)) {
            balanceStartDate = boundedBy.startDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        } else {
            if (isDeposit() || isDividendPayoutAndNotReversed()) {
                // endOfDayBalance = openingBalance.plus(getAmount(currency));
                // if (endOfDayBalance.isLessThanZero()) {
                endOfDayBalance = endOfDayBalance.plus(getAmount(currency));
                // }
            } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
                // endOfDayBalance = openingBalance.minus(getAmount(currency));
                if (endOfDayBalance.isGreaterThanZero() || isAllowOverdraft()) {
                    endOfDayBalance = endOfDayBalance.minus(getAmount(currency));
                } else {
                    endOfDayBalance = Money.of(currency, this.runningBalance);
                }
            }
        }

        if (DateUtils.isAfter(balanceEndDate, boundedBy.endDate())) {
            balanceEndDate = boundedBy.endDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceStartDate, openingBalance, endOfDayBalance, numberOfDaysOfBalance);
    }

    private Money getAmount(MonetaryCurrency currency) {
        return Money.of(currency, getAmount());
    }
}
