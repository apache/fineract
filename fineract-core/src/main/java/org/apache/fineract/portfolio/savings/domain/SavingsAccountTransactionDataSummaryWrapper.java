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
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.springframework.stereotype.Component;

/**
 * A wrapper for dealing with side-effect free functionality related to a {@link SavingsAccount}'s
 * {@link SavingsAccountTransaction}'s.
 */
@Component
public final class SavingsAccountTransactionDataSummaryWrapper {

    public BigDecimal calculateTotalDeposits(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isDepositAndNotReversed() || transaction.isDividendPayoutAndNotReversed()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawals(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isWithdrawal() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalInterestPosted(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawalFees(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isWithdrawalFeeAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalAnnualFees(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isAnnualFeeAndNotReversed() && transaction.isNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalFeesCharge(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isFeeChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalFeesChargeWaived(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isWaiveFeeChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalPenaltyCharge(final CurrencyData currency, final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isPenaltyChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalPenaltyChargeWaived(final CurrencyData currency,
            final List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isWaivePenaltyChargeAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalOverdraftInterest(CurrencyData currency, List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isOverdraftInterestAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithholdTaxWithdrawal(CurrencyData currency, List<SavingsAccountTransactionData> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransactionData transaction : transactions) {
            if (transaction.isWithHoldTaxAndNotReversed() && !transaction.isReversalTransaction()) {
                total = total.plus(transaction.getAmount());
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }
}
