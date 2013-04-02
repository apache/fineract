package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * {@link SavingsAccountSummary} encapsulates all the summary details of a
 * {@link SavingsAccount}.
 */
@Embeddable
public final class SavingsAccountSummary {

    @Column(name = "total_deposits_derived", scale = 6, precision = 19)
    private BigDecimal totalDeposits;

    @Column(name = "total_withdrawals_derived", scale = 6, precision = 19)
    private BigDecimal totalWithdrawals;

    @SuppressWarnings("unused")
    @Column(name = "total_interest_earned_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestEarned;

    @Column(name = "total_interest_posted_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestPosted;

    @Column(name = "account_balance_derived", scale = 6, precision = 19)
    private BigDecimal accountBalance = BigDecimal.ZERO;

    protected SavingsAccountSummary() {
        //
    }

    public void updateSummary(final MonetaryCurrency currency, final SavingsAccountTransactionSummaryWrapper wrapper,
            final List<SavingsAccountTransaction> transactions) {

        this.totalDeposits = wrapper.calculateTotalDeposits(currency, transactions);
        this.totalWithdrawals = wrapper.calculateTotalWithdrawals(currency, transactions);
        this.totalInterestPosted = wrapper.calculateTotalInterestPosted(currency, transactions);

        this.accountBalance = Money.of(currency, this.totalDeposits).plus(this.totalInterestPosted).minus(this.totalWithdrawals)
                .getAmount();
    }

    public void updateFromInterestPeriodSummaries(final MonetaryCurrency currency,
            final List<InterestCompoundingPeriodSummary> compoundingPeriods) {

        // get the max compounded interest
        final BigDecimal compoundedInterestToDate = compoundingPeriods.get(compoundingPeriods.size() - 1).compoundedInterest();
        final Money maxIntrestCompoundedToDate = Money.of(currency, compoundedInterestToDate);

        BigDecimal totalInterestUnrounded = BigDecimal.ZERO;
        for (InterestCompoundingPeriodSummary interestPeriodSummary : compoundingPeriods) {
            totalInterestUnrounded = totalInterestUnrounded.add(interestPeriodSummary.interestUnrounded());
        }

        this.totalInterestEarned = maxIntrestCompoundedToDate.getAmountDefaultedToNullIfZero();
    }

    public boolean isLessThanOrEqualToAccountBalance(final Money amount) {
        final Money accountBalance = getAccountBalance(amount.getCurrency());
        return accountBalance.isGreaterThanOrEqualTo(amount);
    }

    public Money getAccountBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.accountBalance);
    }
}