/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.stereotype.Component;

/**
 * A wrapper for dealing with side-effect free functionality related to a
 * {@link SavingsAccount}'s {@link SavingsAccountTransaction}'s.
 */
@Component
public final class SavingsAccountTransactionSummaryWrapper {

    public BigDecimal calculateTotalDeposits(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isDeposit() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawals(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawal() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalInterestPosted(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isInterestPostingAndNotReversed() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawalFees(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawalFeeAndNotReversed() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalAnnualFees(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isAnnualFeeAndNotReversed() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalFeesCharge(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isFeeChargeAndNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalFeesChargeWaived(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWaiveFeeChargeAndNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalPenaltyCharge(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isPenaltyChargeAndNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalPenaltyChargeWaived(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (final SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWaivePenaltyChargeAndNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

}