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

import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountInterestCalculator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;

/**
 * Default implementation of {@link SavingsAccountInterestCalculationService}. The body of
 * {@code calculateInterestUsing} was extracted from {@code SavingsAccount.calculateInterestUsing}; behaviour is
 * intentionally unchanged. The posting periods themselves come from {@link SavingsAccountInterestCalculator}; this
 * service recalculates the daily balances before and updates the account summary after, reading and writing the account
 * through its public API.
 */
public class SavingsAccountInterestCalculationServiceImpl implements SavingsAccountInterestCalculationService {

    private final SavingsAccountInterestCalculator interestCalculator;

    public SavingsAccountInterestCalculationServiceImpl(final SavingsHelper savingsHelper) {
        this.interestCalculator = new SavingsAccountInterestCalculator(savingsHelper);
    }

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
        final Money openingAccountBalance = backdatedTxnsAllowedTill
                ? Money.of(currency, account.getSummary().getRunningBalanceOnPivotDate())
                : Money.zero(currency);

        // update existing transactions so derived balance fields are correct.
        account.recalculateDailyBalances(openingAccountBalance, interestCalculationUpToDate, backdatedTxnsAllowedTill, postReversals);

        List<PostingPeriod> allPostingPeriods = new ArrayList<>();
        if (account.hasInterestCalculation() || account.hasOverdraftInterestCalculation()) {
            allPostingPeriods = this.interestCalculator.calculatePostingPeriods(account, mc, interestCalculationUpToDate,
                    isInterestTransfer, isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate,
                    backdatedTxnsAllowedTill);
            account.getSummary().updateFromInterestPeriodSummaries(currency, allPostingPeriods);
        }

        if (backdatedTxnsAllowedTill) {
            account.getSummary().updateSummaryWithPivotConfig(SavingsAccountTransactionSummaryWrapper.calculatePivotSummaryTotals(currency,
                    account.getSavingsAccountTransactionsWithPivotConfig(), account.getSummary().getRunningBalanceOnPivotDate()));
        } else {
            account.getSummary()
                    .updateSummary(SavingsAccountTransactionSummaryWrapper.calculateSummaryTotals(currency, account.getTransactions()));
        }

        return allPostingPeriods;
    }
}
