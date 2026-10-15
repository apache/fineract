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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsInterestCalculationUtil;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;

/**
 * Default implementation of {@link SavingsAccountInterestCalculationService}. The body of
 * {@code calculateInterestUsing} was extracted from {@code SavingsAccount.calculateInterestUsing}; behaviour is
 * intentionally unchanged. Account state is read and written through the public API of the {@link SavingsAccount}
 * entity.
 */
@RequiredArgsConstructor
public class SavingsAccountInterestCalculationServiceImpl implements SavingsAccountInterestCalculationService {

    private final SavingsHelper savingsHelper;

    /**
     * All interest calculation based on END-OF-DAY-BALANCE.
     *
     * Interest calculation is performed on-the-fly over all account transactions.
     *
     *
     * 1. Calculate Interest From Beginning Of Account 1a. determine the 'crediting' periods that exist for this savings
     * acccount 1b. determine the 'compounding' periods that exist within each 'crediting' period calculate the amount
     * of interest due at the end of each 'crediting' period check if an existing 'interest posting' transaction exists
     * for date and matches the amount posted
     */
    @Override
    public List<PostingPeriod> calculateInterestUsing(final SavingsAccount account, final MathContext mc,
            final LocalDate upToInterestCalculationDate, final boolean isInterestTransfer,
            final boolean isSavingsInterestPostingAtCurrentPeriodEnd, final Integer financialYearBeginningMonth,
            final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill, final boolean postReversals) {
        final LocalDate interestCalculationUpToDate = account.interestCalculationUpToDate(upToInterestCalculationDate);
        final MonetaryCurrency currency = account.getCurrency();

        // no openingBalance concept supported yet but probably will to allow for
        // migrations.
        // Check global configurations and 'pivot' date is null
        Money openingAccountBalance = backdatedTxnsAllowedTill ? Money.of(currency, account.getSummary().getRunningBalanceOnPivotDate())
                : Money.zero(currency);

        // update existing transactions so derived balance fields are correct.
        account.recalculateDailyBalances(openingAccountBalance, interestCalculationUpToDate, backdatedTxnsAllowedTill, postReversals);

        final List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        if (account.hasInterestCalculation() || account.hasOverdraftInterestCalculation()) {
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
            if (postInterestOnDate != null) {
                postedAsOnDates.add(postInterestOnDate);
            }
            final LocalDate startInterestCalculationDate = account.getStartInterestCalculationDate();
            final List<LocalDateInterval> postingPeriodIntervals = SavingsInterestCalculationUtil.determineInterestPostingPeriods(
                    startInterestCalculationDate, interestCalculationUpToDate, postingPeriodType, financialYearBeginningMonth,
                    postedAsOnDates);

            Money periodStartingBalance;
            if (startInterestCalculationDate != null && !startInterestCalculationDate.equals(account.getActivationDate())) {
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
            final BigDecimal interestRateAsFraction = account.getEffectiveInterestRateAsFraction(mc, interestCalculationUpToDate);
            final BigDecimal overdraftInterestRateAsFraction = account.getEffectiveOverdraftInterestRateAsFraction(mc);
            final Collection<Long> interestPostTransactions = this.savingsHelper.fetchPostInterestTransactionIds(account.getId());
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
                        interestRateAsFraction, daysInYearType.getValue(), interestCalculationUpToDate, interestPostTransactions,
                        isInterestTransfer, minBalanceForInterestCalculation, isSavingsInterestPostingAtCurrentPeriodEnd,
                        overdraftInterestRateAsFraction, minOverdraftForInterestCalculation, isUserPosting, financialYearBeginningMonth);

                periodStartingBalance = postingPeriod.closingBalance();

                allPostingPeriods.add(postingPeriod);
            }

            SavingsInterestCalculationUtil.calculateInterestForAllPostingPeriods(currency, allPostingPeriods,
                    account.getLockedInUntilDate(), account.isTransferInterestToOtherAccount());

            account.getSummary().updateFromInterestPeriodSummaries(currency, allPostingPeriods);
        }

        if (backdatedTxnsAllowedTill) {
            account.getSummary().updateSummaryWithPivotConfig(currency, null, account.getSavingsAccountTransactionsWithPivotConfig());
        } else {
            account.getSummary().updateSummary(currency, account.getTransactions());
        }

        return allPostingPeriods;
    }
}
