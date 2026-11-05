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
package org.apache.fineract.portfolio.savings.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;

/**
 * Builds the interest posting periods of a savings account and calculates the interest earned in each of them, from the
 * account's end of day balances. It doesn't change the account: the caller recalculates the daily balances before and
 * updates the account summary after.
 */
public class SavingsAccountInterestCalculator {

    public List<PostingPeriod> calculatePostingPeriods(final SavingsAccount account, final MathContext mc,
            final LocalDate upToInterestCalculationDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill) {
        final MonetaryCurrency currency = account.getCurrency();
        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        // 1. default to calculate interest based on entire history OR
        // 2. determine latest 'posting period' and find interest credited to that
        // period

        // A generate list of EndOfDayBalances (not including interest postings)
        final SavingsPostingInterestPeriodType postingPeriodType = SavingsPostingInterestPeriodType
                .fromInt(account.getInterestPostingPeriodType());

        final SavingsCompoundingInterestPeriodType compoundingPeriodType = SavingsCompoundingInterestPeriodType
                .fromInt(account.getInterestCompoundingPeriodType());

        final SavingsInterestCalculationDaysInYearType daysInYearType = SavingsInterestCalculationDaysInYearType
                .fromInt(account.getInterestCalculationDaysInYearType());
        List<LocalDate> postedAsOnDates = null;
        if (backdatedTxnsAllowedTill) {
            postedAsOnDates = account.getManualPostingDatesWithPivotConfig();
        } else {
            postedAsOnDates = account.getManualPostingDates();
        }
        if (isSavingsInterestPostingAtCurrentPeriodEnd) {
            // With this configuration manual postings are dated on their period end, not on the day after it
            postedAsOnDates.replaceAll(postedAsOnDate -> postedAsOnDate.plusDays(1));
        }
        if (postInterestOnDate != null) {
            postedAsOnDates.add(postInterestOnDate);
        }
        final List<LocalDateInterval> postingPeriodIntervals = account.savingsHelper.determineInterestPostingPeriods(
                account.getStartInterestCalculationDate(), upToInterestCalculationDate, postingPeriodType, financialYearBeginningMonth,
                postedAsOnDates);

        Money periodStartingBalance;
        if (account.startInterestCalculationDate != null
                && !account.getStartInterestCalculationDate().equals(account.getActivationDate())) {
            LocalDate startInterestCalculationDate = account.startInterestCalculationDate;
            SavingsAccountTransaction transaction = null;
            if (backdatedTxnsAllowedTill) {
                transaction = account.findLastFilteredTransactionWithPivotConfig(startInterestCalculationDate);
            } else {
                transaction = account.findLastTransaction(startInterestCalculationDate);
            }

            if (transaction == null) {
                periodStartingBalance = Money.zero(currency);
            } else {
                periodStartingBalance = Money.of(currency, account.getSummary().getRunningBalanceOnPivotDate());
            }
        } else {
            periodStartingBalance = Money.zero(currency);
        }

        final SavingsInterestCalculationType interestCalculationType = SavingsInterestCalculationType
                .fromInt(account.getInterestCalculationType());
        final BigDecimal interestRateAsFraction = account.getEffectiveInterestRateAsFraction(mc, upToInterestCalculationDate);
        final BigDecimal overdraftInterestRateAsFraction = getEffectiveOverdraftInterestRateAsFraction(account, mc);
        final Collection<Long> interestPostTransactions = account.savingsHelper.fetchPostInterestTransactionIds(account.getId());
        final Money minBalanceForInterestCalculation = Money.of(currency, account.minBalanceForInterestCalculation());
        final Money minOverdraftForInterestCalculation = Money.of(currency, account.getMinOverdraftForInterestCalculation());

        for (final LocalDateInterval periodInterval : postingPeriodIntervals) {

            boolean isUserPosting = false;
            if (postedAsOnDates.contains(periodInterval.endDate().plusDays(1))) {
                isUserPosting = true;
            }

            PostingPeriod postingPeriod = null;
            List<SavingsAccountTransaction> orderedNonInterestPostingTransactions = null;
            if (backdatedTxnsAllowedTill) {
                orderedNonInterestPostingTransactions = account.retreiveOrderedNonInterestPostingSavingsTransactionsWithPivotConfig();
            } else {
                orderedNonInterestPostingTransactions = account.retreiveOrderedNonInterestPostingTransactions();
            }

            List<SavingsAccountTransactionDetailsForPostingPeriod> savingsAccountTransactionDetailsForPostingPeriod = account
                    .toSavingsAccountTransactionDetailsForPostingPeriodList(orderedNonInterestPostingTransactions);

            postingPeriod = PostingPeriod.createFrom(periodInterval, periodStartingBalance,
                    savingsAccountTransactionDetailsForPostingPeriod, currency, compoundingPeriodType, interestCalculationType,
                    interestRateAsFraction, daysInYearType.getValue(), upToInterestCalculationDate, interestPostTransactions,
                    isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd,
                    overdraftInterestRateAsFraction, minOverdraftForInterestCalculation, isUserPosting, financialYearBeginningMonth);

            periodStartingBalance = postingPeriod.closingBalance();

            allPostingPeriods.add(postingPeriod);
        }

        account.savingsHelper.calculateInterestForAllPostingPeriods(currency, allPostingPeriods, account.getLockedInUntilDate(),
                account.isTransferInterestToOtherAccount());

        return allPostingPeriods;
    }

    private BigDecimal getEffectiveOverdraftInterestRateAsFraction(final SavingsAccount account, final MathContext mc) {
        return account.getNominalAnnualInterestRateOverdraft().divide(BigDecimal.valueOf(100L), mc);
    }
}
