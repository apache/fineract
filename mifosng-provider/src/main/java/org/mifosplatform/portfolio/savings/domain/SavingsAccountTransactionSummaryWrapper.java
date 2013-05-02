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
        for (SavingsAccountTransaction transaction : transactions) {
            if (transaction.isDeposit() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawals(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawal() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalInterestPosted(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (SavingsAccountTransaction transaction : transactions) {
            if (transaction.isInterestPosting() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalWithdrawalFees(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (SavingsAccountTransaction transaction : transactions) {
            if (transaction.isWithdrawalFee() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }

    public BigDecimal calculateTotalAnnualFees(final MonetaryCurrency currency, final List<SavingsAccountTransaction> transactions) {
        Money total = Money.zero(currency);
        for (SavingsAccountTransaction transaction : transactions) {
            if (transaction.isAnnualFee() && transaction.isNotReversed()) {
                total = total.plus(transaction.getAmount(currency));
            }
        }
        return total.getAmountDefaultedToNullIfZero();
    }
}