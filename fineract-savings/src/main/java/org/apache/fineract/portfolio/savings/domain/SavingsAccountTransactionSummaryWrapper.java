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
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

/**
 * A utility for dealing with side-effect free functionality related to a {@link SavingsAccount}'s
 * {@link SavingsAccountTransaction}'s. All methods are static as they are pure functions over the supplied
 * transactions. It is meant to be called by services, which hand the results to the entity; entities must not call it.
 */
public final class SavingsAccountTransactionSummaryWrapper {

    private SavingsAccountTransactionSummaryWrapper() {}

    /**
     * Calculates every total of a {@link SavingsAccountSummary} and the resulting account balance from the given
     * transactions.
     */
    public static SavingsAccountSummaryTotals calculateSummaryTotals(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        final BigDecimal totalDeposits = calculateTotalDeposits(currency, transactions);
        final BigDecimal totalWithdrawals = calculateTotalWithdrawals(currency, transactions);
        final BigDecimal totalInterestPosted = calculateTotalInterestPosted(currency, transactions);
        final BigDecimal totalWithdrawalFees = calculateTotalWithdrawalFees(currency, transactions);
        final BigDecimal totalAnnualFees = calculateTotalAnnualFees(currency, transactions);
        final BigDecimal totalFeeCharge = calculateTotalFeesCharge(currency, transactions);
        final BigDecimal totalPenaltyCharge = calculateTotalPenaltyCharge(currency, transactions);
        final BigDecimal totalFeeChargesWaived = calculateTotalFeesChargeWaived(currency, transactions);
        final BigDecimal totalPenaltyChargesWaived = calculateTotalPenaltyChargeWaived(currency, transactions);
        final BigDecimal totalOverdraftInterestDerived = calculateTotalOverdraftInterest(currency, transactions);
        final BigDecimal totalWithholdTax = calculateTotalWithholdTaxWithdrawal(currency, transactions);

        final BigDecimal accountBalance = Money.of(currency, totalDeposits).plus(totalInterestPosted).minus(totalWithdrawals)
                .minus(totalWithdrawalFees).minus(totalAnnualFees).minus(totalFeeCharge).minus(totalPenaltyCharge)
                .minus(totalOverdraftInterestDerived).minus(totalWithholdTax).getAmount();

        return new SavingsAccountSummaryTotals(totalDeposits, totalWithdrawals, totalInterestPosted, totalWithdrawalFees, totalAnnualFees,
                totalFeeCharge, totalPenaltyCharge, totalFeeChargesWaived, totalPenaltyChargesWaived, totalOverdraftInterestDerived,
                totalWithholdTax, accountBalance, findInterestPostedTillDate(transactions));
    }

    /**
     * Calculates the {@link SavingsAccountSummary} totals refreshed after an interest posting when backdated
     * transactions are allowed, starting from the running balance on the pivot date.
     */
    public static SavingsAccountPivotSummaryTotals calculatePivotSummaryTotals(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> savingsAccountTransactions, final BigDecimal runningBalanceOnPivotDate) {
        final BigDecimal totalDeposits = calculateTotalDeposits(currency, savingsAccountTransactions);
        final BigDecimal totalWithdrawals = calculateTotalWithdrawals(currency, savingsAccountTransactions);

        Money interestTotal = Money.zero(currency);
        Money withHoldTaxTotal = Money.zero(currency);
        Money overdraftInterestTotal = Money.zero(currency);
        for (int i = savingsAccountTransactions.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction savingsAccountTransaction = savingsAccountTransactions.get(i);
            if (savingsAccountTransaction.isInterestPostingAndNotReversed() && savingsAccountTransaction.isNotReversed()
                    && !savingsAccountTransaction.isReversalTransaction()) {
                interestTotal = interestTotal.plus(savingsAccountTransaction.getAmount(currency));
            }
            if (savingsAccountTransaction.isOverdraftInterestAndNotReversed() && !savingsAccountTransaction.isReversalTransaction()) {
                overdraftInterestTotal = overdraftInterestTotal.plus(savingsAccountTransaction.getAmount());
            }
            if (savingsAccountTransaction.isWithHoldTaxAndNotReversed() && !savingsAccountTransaction.isReversalTransaction()) {
                withHoldTaxTotal = withHoldTaxTotal.plus(savingsAccountTransaction.getAmount(currency));
            }
        }
        final BigDecimal totalInterestPosted = interestTotal.getAmountDefaultedToNullIfZero();
        final BigDecimal totalOverdraftInterestDerived = overdraftInterestTotal.getAmountDefaultedToNullIfZero();
        final BigDecimal totalWithholdTax = withHoldTaxTotal.getAmountDefaultedToNullIfZero();

        final BigDecimal accountBalance = Money.of(currency, runningBalanceOnPivotDate).plus(Money.of(currency, totalDeposits))
                .plus(totalInterestPosted).minus(totalWithdrawals).minus(totalWithholdTax).minus(totalOverdraftInterestDerived).getAmount();

        return new SavingsAccountPivotSummaryTotals(totalDeposits, totalWithdrawals, totalInterestPosted, totalOverdraftInterestDerived,
                totalWithholdTax, accountBalance, findInterestPostedTillDate(savingsAccountTransactions));
    }

    /**
     * Returns the date of the latest non-reversed interest or overdraft interest posting, or {@code null} when there is
     * none.
     */
    private static LocalDate findInterestPostedTillDate(final List<SavingsAccountTransaction> transactions) {
        for (int i = transactions.size() - 1; i >= 0; i--) {
            final SavingsAccountTransaction transaction = transactions.get(i);
            if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && !transaction.isReversalTransaction()) {
                return transaction.getTransactionDate();
            }
        }
        return null;
    }

    public static BigDecimal calculateTotalDeposits(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if ((transaction.isDepositAndNotReversed() || transaction.isDividendPayoutAndNotReversed())
                    && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalWithdrawals(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawal() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalInterestPosted(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalWithdrawalFees(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawalFeeAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalAnnualFees(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isAnnualFeeAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalFeesCharge(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isFeeChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalFeesChargeWaived(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWaiveFeeChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalPenaltyCharge(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isPenaltyChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalPenaltyChargeWaived(final MonetaryCurrency currency,
            final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWaivePenaltyChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalOverdraftInterest(MonetaryCurrency currency, List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isOverdraftInterestAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public static BigDecimal calculateTotalWithholdTaxWithdrawal(MonetaryCurrency currency, List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithHoldTaxAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

}
