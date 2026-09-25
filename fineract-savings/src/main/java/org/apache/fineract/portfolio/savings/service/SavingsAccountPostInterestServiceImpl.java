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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;

/**
 * Default implementation of {@link SavingsAccountPostInterestService}. The body of this {@code postInterest} method was
 * extracted from {@code SavingsAccount.postInterest}; behaviour is intentionally unchanged. Account state is
 * read/written through the public API of the {@link SavingsAccount} entity, while the stateless
 * {@link SavingsAccountTransactionSummaryWrapper} collaborator is injected here (rather than read off the entity's
 * transient field) to keep that dependency out of the domain entity.
 */
@RequiredArgsConstructor
public class SavingsAccountPostInterestServiceImpl implements SavingsAccountPostInterestService {

    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;

    @Override
    public void postInterest(final SavingsAccount account, final MathContext mc, final LocalDate postingDate,
            final boolean isInterestTransfer, final boolean isSavingsInterestPostingAtCurrentPeriodEnd,
            final Integer financialYearBeginningMonth, final LocalDate postInterestOnDate, final boolean backdatedTxnsAllowedTill,
            final boolean postReversals) {
        // Resolve the effective up-to date polymorphically: identity for regular savings / fixed deposits posted
        // through the
        // generic path, maturity-capped for recurring deposits (mirrors the former SavingsAccount.postInterest
        // overloads).
        final LocalDate interestPostingUpToDate = account.interestPostingUpToDate(postingDate);
        final List<PostingPeriod> postingPeriods = account.calculateInterestUsing(mc, interestPostingUpToDate, isInterestTransfer,
                isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth, postInterestOnDate, backdatedTxnsAllowedTill,
                postReversals);
        if (postingPeriods.isEmpty()) {
            return;
        }

        Money interestPostedToDate = Money.zero(account.getCurrency());

        if (backdatedTxnsAllowedTill) {
            interestPostedToDate = Money.of(account.getCurrency(), account.getSummary().getTotalInterestPosted());
        }

        boolean recalucateDailyBalanceDetails = false;
        boolean applyWithHoldTax = account.isWithHoldTaxApplicableForInterestPosting();
        final List<SavingsAccountTransaction> withholdTransactions = new ArrayList<>();

        if (backdatedTxnsAllowedTill) {
            withholdTransactions.addAll(account.findWithHoldSavingsTransactionsWithPivotConfig());
        } else {
            withholdTransactions.addAll(account.findWithHoldTransactions());
        }

        for (final PostingPeriod interestPostingPeriod : postingPeriods) {
            final LocalDate interestPostingTransactionDate = interestPostingPeriod.dateOfPostingTransaction();
            final Money interestEarnedToBePostedForPeriod = interestPostingPeriod.getInterestEarned();

            if (!DateUtils.isAfter(interestPostingTransactionDate, interestPostingUpToDate)) {
                interestPostedToDate = interestPostedToDate.plus(interestEarnedToBePostedForPeriod);

                SavingsAccountTransaction postingTransaction = null;
                if (backdatedTxnsAllowedTill) {
                    postingTransaction = account.findInterestPostingSavingsTransactionWithPivotConfig(interestPostingTransactionDate);
                } else {
                    postingTransaction = account.findInterestPostingTransactionFor(interestPostingTransactionDate);
                }
                // A period that earned nothing has nothing to post: a zero amount transaction would carry no
                // information and would only show up as noise on the account.
                final boolean hasInterestToPost = !interestEarnedToBePostedForPeriod.isZero();

                if (postingTransaction == null) {
                    if (hasInterestToPost) {
                        addPostingTransaction(account, interestPostingPeriod, interestPostingTransactionDate,
                                interestEarnedToBePostedForPeriod, backdatedTxnsAllowedTill);
                        if (applyWithHoldTax) {
                            account.createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(), interestPostingTransactionDate,
                                    backdatedTxnsAllowedTill);
                        }
                        recalucateDailyBalanceDetails = true;
                    }
                } else {
                    boolean correctionRequired = false;
                    if (postingTransaction.isInterestPostingAndNotReversed()) {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod);
                    } else {
                        correctionRequired = postingTransaction.hasNotAmount(interestEarnedToBePostedForPeriod.negated());
                    }
                    if (correctionRequired) {
                        boolean applyWithHoldTaxForOldTransaction = false;
                        postingTransaction.reverse();
                        SavingsAccountTransaction reversal = null;
                        if (postReversals) {
                            reversal = SavingsAccountTransaction.reversal(postingTransaction);
                        }
                        final SavingsAccountTransaction withholdTransaction = account.findTransactionFor(interestPostingTransactionDate,
                                withholdTransactions);
                        if (withholdTransaction != null) {
                            withholdTransaction.reverse();
                            applyWithHoldTaxForOldTransaction = true;
                        }
                        // The period no longer earns anything, so the reversal above is the whole correction.
                        if (hasInterestToPost) {
                            addPostingTransaction(account, interestPostingPeriod, interestPostingTransactionDate,
                                    interestEarnedToBePostedForPeriod, backdatedTxnsAllowedTill);
                            if (applyWithHoldTaxForOldTransaction) {
                                account.createWithHoldTransaction(interestEarnedToBePostedForPeriod.getAmount(),
                                        interestPostingTransactionDate, backdatedTxnsAllowedTill);
                            }
                        }
                        if (reversal != null) {
                            addTransaction(account, reversal, backdatedTxnsAllowedTill);
                        }
                        recalucateDailyBalanceDetails = true;
                    }
                }
            }
        }

        if (recalucateDailyBalanceDetails) {
            // no openingBalance concept supported yet but probably will to
            // allow
            // for migrations.
            Money openingAccountBalance = Money.zero(account.getCurrency());

            if (backdatedTxnsAllowedTill) {
                if (account.getSummary().getLastInterestCalculationDate() == null) {
                    openingAccountBalance = Money.zero(account.getCurrency());
                } else {
                    openingAccountBalance = Money.of(account.getCurrency(), account.getSummary().getRunningBalanceOnPivotDate());
                }
            }

            // update existing transactions so derived balance fields are
            // correct.
            account.recalculateDailyBalances(openingAccountBalance, interestPostingUpToDate, backdatedTxnsAllowedTill, postReversals);
        }

        if (!backdatedTxnsAllowedTill) {
            account.getSummary().updateSummary(account.getCurrency(), this.savingsAccountTransactionSummaryWrapper,
                    account.getTransactions());
        } else {
            account.getSummary().updateSummaryWithPivotConfig(account.getCurrency(), this.savingsAccountTransactionSummaryWrapper, null,
                    account.getSavingsAccountTransactionsWithPivotConfig());
        }
    }

    private void addPostingTransaction(final SavingsAccount account, final PostingPeriod interestPostingPeriod,
            final LocalDate interestPostingTransactionDate, final Money interestEarned, final boolean backdatedTxnsAllowedTill) {
        final SavingsAccountTransaction newPostingTransaction;
        if (interestEarned.isGreaterThanZero()) {
            newPostingTransaction = SavingsAccountTransaction.interestPosting(account, account.office(), interestPostingTransactionDate,
                    interestEarned, interestPostingPeriod.isUserPosting());
        } else {
            newPostingTransaction = SavingsAccountTransaction.overdraftInterest(account, account.office(), interestPostingTransactionDate,
                    interestEarned.negated(), interestPostingPeriod.isUserPosting());
        }
        addTransaction(account, newPostingTransaction, backdatedTxnsAllowedTill);
    }

    private void addTransaction(final SavingsAccount account, final SavingsAccountTransaction transaction,
            final boolean backdatedTxnsAllowedTill) {
        if (backdatedTxnsAllowedTill) {
            account.addTransactionToExisting(transaction);
        } else {
            account.addTransaction(transaction);
        }
    }
}
