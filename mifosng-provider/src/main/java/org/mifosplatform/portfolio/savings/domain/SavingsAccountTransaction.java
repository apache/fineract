/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * All monetary transactions against a savings account are modelled through this
 * entity.
 */
@Entity
@Table(name = "m_savings_account_transaction")
public final class SavingsAccountTransaction extends AbstractPersistable<Long> {

    @SuppressWarnings("unused")
    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @Column(name = "transaction_type_enum", nullable = false)
    private final Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private final Date dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "running_balance_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal runningBalance;

    @SuppressWarnings("unused")
    @Column(name = "cumulative_balance_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal cumulativeBalance;

    @Temporal(TemporalType.DATE)
    @Column(name = "balance_end_date_derived", nullable = false)
    private Date balanceEndDate;

    @Column(name = "balance_number_of_days_derived", nullable = false)
    private Integer balanceNumberOfDays;

    protected SavingsAccountTransaction() {
        this.dateOf = null;
        this.typeOf = null;
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, SavingsAccountTransactionType.DEPOSIT.getValue(), date, amount, isReversed);
    }

    public static SavingsAccountTransaction withdrawal(final SavingsAccount savingsAccount, final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, SavingsAccountTransactionType.WITHDRAWAL.getValue(), date, amount, isReversed);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Integer typeOf, final LocalDate transactionLocalDate,
            final Money amount, final boolean isReversed) {
        this.savingsAccount = savingsAccount;
        this.typeOf = typeOf;
        this.dateOf = transactionLocalDate.toDate();
        this.amount = amount.getAmount();
        this.reversed = isReversed;
    }

    public LocalDate localDate() {
        return new LocalDate(this.dateOf);
    }

    public void reverse() {
        this.reversed = true;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public boolean isDeposit() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isDeposit();
    }

    public boolean isWithdrawal() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawal();
    }

    public boolean isInterestPosting() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isInterestPosting();
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public void zeroBalanceFields() {
        this.runningBalance = null;
        this.cumulativeBalance = null;
        this.balanceEndDate = null;
        this.balanceNumberOfDays = null;
    }

    public void updateRunningBalance(final Money balance) {
        this.runningBalance = balance.getAmount();
    }

    public void updateCumulativeBalanceAndDates(final MonetaryCurrency currency, final LocalDate endOfBalanceDate) {
        this.balanceEndDate = endOfBalanceDate.toDate();
        this.balanceNumberOfDays = InterestPeriodInterval.create(getTransactionLocalDate(), endOfBalanceDate)
                .daysInPeriodInclusiveOfEndDate();
        this.cumulativeBalance = Money.of(currency, this.runningBalance).multipliedBy(this.balanceNumberOfDays).getAmount();
    }

    private LocalDate getTransactionLocalDate() {
        return new LocalDate(this.dateOf);
    }

    private LocalDate getEndOfBalanceLocalDate() {
        LocalDate endDate = null;
        if (this.balanceEndDate != null) {
            endDate = new LocalDate(this.balanceEndDate);
        }
        return endDate;
    }

    public boolean isAcceptableForDailyBalance(final InterestPeriodInterval interestPeriodInterval) {
        return isNotReversed() && interestPeriodInterval.contains(getTransactionLocalDate()) && isABalanceForAtLeastOneDay();
    }

    private boolean isABalanceForAtLeastOneDay() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays > 0;
    }

    public SavingsAccountDailyBalance toDailyBalance(final LocalDate periodEnd) {

        LocalDate balanceValidTo = getEndOfBalanceLocalDate();
        if (periodEnd.isBefore(getEndOfBalanceLocalDate())) {
            balanceValidTo = periodEnd;
        }

        final InterestPeriodInterval interestPeriodInterval = InterestPeriodInterval.create(getTransactionLocalDate(), balanceValidTo);
        return SavingsAccountDailyBalance.createFrom(interestPeriodInterval, this.runningBalance);
    }
}