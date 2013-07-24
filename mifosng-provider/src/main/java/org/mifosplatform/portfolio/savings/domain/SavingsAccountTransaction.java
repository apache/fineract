/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.SavingsAccountTransactionType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.domain.interest.EndOfDayBalance;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
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

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_detail_id", nullable = true)
    private PaymentDetail paymentDetail;

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

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final PaymentDetail paymentDetail,
            final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, paymentDetail, SavingsAccountTransactionType.DEPOSIT.getValue(), date, amount,
                isReversed);
    }

    public static SavingsAccountTransaction withdrawal(final SavingsAccount savingsAccount, final PaymentDetail paymentDetail,
            final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, paymentDetail, SavingsAccountTransactionType.WITHDRAWAL.getValue(), date,
                amount, isReversed);
    }

    public static SavingsAccountTransaction interestPosting(final SavingsAccount savingsAccount, final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, SavingsAccountTransactionType.INTEREST_POSTING.getValue(), date, amount,
                isReversed);
    }

    public static SavingsAccountTransaction fee(final SavingsAccount savingsAccount, final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue(), date, amount,
                isReversed);
    }

    public static SavingsAccountTransaction annualFee(final SavingsAccount savingsAccount, final LocalDate date, final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, SavingsAccountTransactionType.ANNUAL_FEE.getValue(), date, amount, isReversed);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Integer typeOf, final LocalDate transactionLocalDate,
            final Money amount, final boolean isReversed) {
        this(savingsAccount, null, typeOf, transactionLocalDate, amount, isReversed);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final PaymentDetail paymentDetail, final Integer typeOf,
            final LocalDate transactionLocalDate, final Money amount, final boolean isReversed) {
        this.savingsAccount = savingsAccount;
        this.typeOf = typeOf;
        this.dateOf = transactionLocalDate.toDate();
        this.amount = amount.getAmount();
        this.reversed = isReversed;
        this.paymentDetail = paymentDetail;

    }

    public LocalDate transactionLocalDate() {
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

    public boolean isInterestPostingAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isInterestPosting() && isNotReversed();
    }

    public boolean isWithdrawalFeeAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawalFee() && isNotReversed();
    }

    public boolean isAnnualFeeAndNotReversed() {
        return isAnnualFee() && isNotReversed();
    }

    public boolean isAnnualFee() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isAnnualFee();
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public boolean occursOn(final LocalDate occursOnDate) {
        return getTransactionLocalDate().isEqual(occursOnDate);
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
        this.balanceNumberOfDays = LocalDateInterval.create(getTransactionLocalDate(), endOfBalanceDate).daysInPeriodInclusiveOfEndDate();
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

    public boolean isAcceptableForDailyBalance(final LocalDateInterval interestPeriodInterval) {
        return isNotReversed() && interestPeriodInterval.contains(getTransactionLocalDate()) && isABalanceForAtLeastOneDay();
    }

    private boolean isABalanceForAtLeastOneDay() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays > 0;
    }

    public boolean hasNotAmount(final Money amountToCheck) {
        final Money transactionAmount = getAmount(amountToCheck.getCurrency());
        return transactionAmount.isNotEqualTo(amountToCheck);
    }

    public Map<String, Object> toMapData(final CurrencyData currencyData) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<String, Object>();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", this.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", Boolean.valueOf(this.isReversed()));
        thisTransactionData.put("date", this.getTransactionLocalDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);

        if (this.paymentDetail != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetail.getPaymentType().getId());
        }

        return thisTransactionData;
    }

    public boolean isAfter(final LocalDate transactionDate) {
        return this.getTransactionLocalDate().isAfter(transactionDate);
    }

    public EndOfDayBalance toEndOfDayBalance(final LocalDateInterval periodInterval, final MonetaryCurrency currency) {

        Money endOfDayBalance = Money.of(currency, this.runningBalance);
        Money openingBalance = endOfDayBalance;

        LocalDate balanceDate = periodInterval.startDate();

        int numberOfDays = periodInterval.daysInPeriodInclusiveOfEndDate();
        if (periodInterval.contains(getTransactionLocalDate())) {
            balanceDate = getTransactionLocalDate();
            LocalDateInterval newInterval = LocalDateInterval.create(getTransactionLocalDate(), periodInterval.endDate());
            numberOfDays = newInterval.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceDate, openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance, final LocalDate nextTransactionDate) {

        MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isWithdrawalFeeAndNotReversed()) {
            endOfDayBalance = openingBalance.minus(getAmount(currency));
        }

        int numberOfDays = LocalDateInterval.create(getTransactionLocalDate(), nextTransactionDate).daysInPeriodInclusiveOfEndDate();
        if (!openingBalance.isEqualTo(endOfDayBalance) && numberOfDays > 1) {
            numberOfDays = numberOfDays - 1;
        }
        return EndOfDayBalance.from(getTransactionLocalDate(), openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance) {
        MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isWithdrawalFeeAndNotReversed()) {
            endOfDayBalance = openingBalance.minus(getAmount(currency));
        }

        return EndOfDayBalance.from(getTransactionLocalDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalanceBoundedBy(final Money openingBalance, final LocalDateInterval boundedBy) {

        MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();

        int numberOfDaysOfBalance = this.balanceNumberOfDays;

        LocalDate balanceStartDate = getTransactionLocalDate();
        LocalDate balanceEndDate = getEndOfBalanceLocalDate();

        if (boundedBy.startDate().isAfter(balanceStartDate)) {
            balanceStartDate = boundedBy.startDate();
            LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        } else {
            if (isDeposit()) {
                endOfDayBalance = openingBalance.plus(getAmount(currency));
            } else if (isWithdrawal() || isWithdrawalFeeAndNotReversed()) {
                endOfDayBalance = openingBalance.minus(getAmount(currency));
            }
        }

        if (balanceEndDate.isAfter(boundedBy.endDate())) {
            balanceEndDate = boundedBy.endDate();
            LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceStartDate, openingBalance, endOfDayBalance, numberOfDaysOfBalance);
    }

    public boolean isBalanceInExistencesForOneDayOrMore() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays.intValue() >= 1;
    }

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return periodInterval.contains(balanceInterval);
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public boolean isIdentifiedBy(final Long transactionId) {
        return this.getId().equals(transactionId);
    }

    public boolean isCredit() {
        return isDeposit() || isInterestPostingAndNotReversed();
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFeeAndNotReversed() || isAnnualFeeAndNotReversed();
    }
}