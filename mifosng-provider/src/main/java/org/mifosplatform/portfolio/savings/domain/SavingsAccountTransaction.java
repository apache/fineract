/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.mifosplatform.portfolio.savings.SavingsAccountTransactionType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.domain.interest.EndOfDayBalance;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.CollectionUtils;

/**
 * All monetary transactions against a savings account are modelled through this
 * entity.
 */
@Entity
@Table(name = "m_savings_account_transaction")
public final class SavingsAccountTransaction extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

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

    @Column(name = "cumulative_balance_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal cumulativeBalance;

    @Temporal(TemporalType.DATE)
    @Column(name = "balance_end_date_derived", nullable = false)
    private Date balanceEndDate;

    @Column(name = "balance_number_of_days_derived", nullable = false)
    private Integer balanceNumberOfDays;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccountTransaction", orphanRemoval = true)
    private final Set<SavingsAccountChargePaidBy> savingsAccountChargesPaid = new HashSet<>();

    @Column(name = "overdraft_amount_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private final Date createdDate;

    @ManyToOne
    @JoinColumn(name = "appuser_id", nullable = true)
    private AppUser appUser;

    protected SavingsAccountTransaction() {
        this.dateOf = null;
        this.typeOf = null;
        this.createdDate = null;
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.DEPOSIT.getValue(), date,
                createdDate, amount, isReversed, appUser);
    }

    public static SavingsAccountTransaction withdrawal(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.WITHDRAWAL.getValue(),
                date, createdDate, amount, isReversed, appUser);
    }

    public static SavingsAccountTransaction interestPosting(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.INTEREST_POSTING.getValue(), date,
                amount, isReversed, null);
    }

    public static SavingsAccountTransaction withdrawalFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue(), date, amount,
                isReversed, appUser);
    }

    public static SavingsAccountTransaction annualFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.ANNUAL_FEE.getValue(), date, amount,
                isReversed, appUser);
    }

    public static SavingsAccountTransaction charge(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.PAY_CHARGE.getValue(), date, amount,
                isReversed, appUser);
    }

    public static SavingsAccountTransaction waiver(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WAIVE_CHARGES.getValue(), date, amount,
                isReversed, appUser);
    }

    public static SavingsAccountTransaction initiateTransfer(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date, final AppUser appUser) {
        final boolean isReversed = false;
        final PaymentDetail paymentDetail = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.INITIATE_TRANSFER.getValue(), date, new Date(), savingsAccount.getSummary()
                        .getAccountBalance(), isReversed, appUser);
    }

    public static SavingsAccountTransaction approveTransfer(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final AppUser appUser) {
        final boolean isReversed = false;
        final PaymentDetail paymentDetail = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.APPROVE_TRANSFER.getValue(), date, new Date(), savingsAccount.getSummary()
                        .getAccountBalance(), isReversed, appUser);
    }

    public static SavingsAccountTransaction withdrawTransfer(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date, final AppUser appUser) {
        final boolean isReversed = false;
        final PaymentDetail paymentDetail = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue(), date, new Date(), savingsAccount.getSummary()
                        .getAccountBalance(), isReversed, appUser);
    }

    public static SavingsAccountTransaction copyTransaction(SavingsAccountTransaction accountTransaction) {
        return new SavingsAccountTransaction(accountTransaction.savingsAccount, accountTransaction.office,
                accountTransaction.paymentDetail, accountTransaction.typeOf, accountTransaction.transactionLocalDate(),
                accountTransaction.createdDate, accountTransaction.amount, accountTransaction.reversed, accountTransaction.appUser);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final Integer typeOf,
            final LocalDate transactionLocalDate, final Money amount, final boolean isReversed, final AppUser appUser) {
        this(savingsAccount, office, null, typeOf, transactionLocalDate, new Date(), amount, isReversed, appUser);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final Date createdDate, final Money amount,
            final boolean isReversed, final AppUser appUser) {
        this(savingsAccount, office, paymentDetail, typeOf, transactionLocalDate, createdDate, amount.getAmount(), isReversed, appUser);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final Date createdDate, final BigDecimal amount,
            final boolean isReversed, final AppUser appUser) {
        this.savingsAccount = savingsAccount;
        this.office = office;
        this.typeOf = typeOf;
        this.dateOf = transactionLocalDate.toDate();
        this.amount = amount;
        this.reversed = isReversed;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.appUser = appUser;
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

    public Money getRunningBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.runningBalance);
    }

    public boolean isDeposit() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isDeposit();
    }

    public boolean isDepositAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isDeposit() && isNotReversed();
    }

    public boolean isWithdrawal() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawal();
    }

    public boolean isPostInterestCalculationRequired() {
        return this.isDeposit() || this.isChargeTransaction();
    }

    public boolean isInterestPostingAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isInterestPosting() && isNotReversed();
    }

    public boolean isWithdrawalFeeAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawalFee() && isNotReversed();
    }

    public boolean isWithdrawalFee() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawalFee();
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

    public boolean isTransferInitiation() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isTransferInitiation();
    }

    public boolean isTransferApproval() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isTransferApproval();
    }

    public boolean isTransferRejection() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isTransferRejection();
    }

    public boolean isTransferWithdrawal() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isTransferWithdrawal();
    }

    public boolean isTransferRelatedTransaction() {
        return isTransferInitiation() || isTransferApproval() || isTransferRejection() || isTransferWithdrawal();
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
        // balance end date should not be before transaction date
        if (endOfBalanceDate != null && endOfBalanceDate.isBefore(this.transactionLocalDate())) {
            this.balanceEndDate = this.transactionLocalDate().toDate();
        } else if (endOfBalanceDate != null) {
            this.balanceEndDate = endOfBalanceDate.toDate();
        } else {
            this.balanceEndDate = null;
        }
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
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", getId());
        thisTransactionData.put("officeId", this.office.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", Boolean.valueOf(isReversed()));
        thisTransactionData.put("date", getTransactionLocalDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("overdraftAmount", this.overdraftAmount);

        if (this.paymentDetail != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetail.getPaymentType().getId());
        }

        /***
         * Sending data in a map, though in savings we currently expect a
         * transaction to always repay a single charge (or may repay a part of a
         * single charge too)
         ***/
        if (!this.savingsAccountChargesPaid.isEmpty()) {
            final List<Map<String, Object>> savingsChargesPaidData = new ArrayList<>();
            for (final SavingsAccountChargePaidBy chargePaidBy : this.savingsAccountChargesPaid) {
                final Map<String, Object> savingChargePaidData = new LinkedHashMap<>();
                savingChargePaidData.put("chargeId", chargePaidBy.getSavingsAccountCharge().getCharge().getId());
                savingChargePaidData.put("isPenalty", chargePaidBy.getSavingsAccountCharge().getCharge().isPenalty());
                savingChargePaidData.put("savingsChargeId", chargePaidBy.getSavingsAccountCharge().getId());
                savingChargePaidData.put("amount", chargePaidBy.getAmount());

                savingsChargesPaidData.add(savingChargePaidData);
            }
            thisTransactionData.put("savingsChargesPaid", savingsChargesPaidData);
        }

        return thisTransactionData;
    }

    public boolean isAfter(final LocalDate transactionDate) {
        return getTransactionLocalDate().isAfter(transactionDate);
    }

    public EndOfDayBalance toEndOfDayBalance(final LocalDateInterval periodInterval, final MonetaryCurrency currency) {

        final Money endOfDayBalance = Money.of(currency, this.runningBalance);
        final Money openingBalance = endOfDayBalance;

        LocalDate balanceDate = periodInterval.startDate();

        int numberOfDays = periodInterval.daysInPeriodInclusiveOfEndDate();
        if (periodInterval.contains(getTransactionLocalDate())) {
            balanceDate = getTransactionLocalDate();
            final LocalDateInterval newInterval = LocalDateInterval.create(getTransactionLocalDate(), periodInterval.endDate());
            numberOfDays = newInterval.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceDate, openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance, final LocalDate nextTransactionDate) {

        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
            endOfDayBalance = openingBalance.minus(getAmount(currency));
        }

        int numberOfDays = LocalDateInterval.create(getTransactionLocalDate(), nextTransactionDate).daysInPeriodInclusiveOfEndDate();
        if (!openingBalance.isEqualTo(endOfDayBalance) && numberOfDays > 1) {
            numberOfDays = numberOfDays - 1;
        }
        return EndOfDayBalance.from(getTransactionLocalDate(), openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance) {
        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {

            if (openingBalance.isGreaterThanZero()) {
                endOfDayBalance = openingBalance.minus(getAmount(currency));
            } else {
                endOfDayBalance = Money.of(currency, this.runningBalance);
            }
        }

        return EndOfDayBalance.from(getTransactionLocalDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalanceBoundedBy(final Money openingBalance, final LocalDateInterval boundedBy) {

        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();

        int numberOfDaysOfBalance = this.balanceNumberOfDays;

        LocalDate balanceStartDate = getTransactionLocalDate();
        LocalDate balanceEndDate = getEndOfBalanceLocalDate();

        if (boundedBy.startDate().isAfter(balanceStartDate)) {
            balanceStartDate = boundedBy.startDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        } else {
            if (isDeposit()) {
                // endOfDayBalance = openingBalance.plus(getAmount(currency));
                // if (endOfDayBalance.isLessThanZero()) {
                endOfDayBalance = endOfDayBalance.plus(getAmount(currency));
                // }
            } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
                // endOfDayBalance = openingBalance.minus(getAmount(currency));
                if (endOfDayBalance.isGreaterThanZero()) {
                    endOfDayBalance = endOfDayBalance.minus(getAmount(currency));
                } else {
                    endOfDayBalance = Money.of(currency, this.runningBalance);
                }
            }
        }

        if (balanceEndDate.isAfter(boundedBy.endDate())) {
            balanceEndDate = boundedBy.endDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceStartDate, openingBalance, endOfDayBalance, numberOfDaysOfBalance);
    }

    public boolean isBalanceInExistencesForOneDayOrMore() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays.intValue() >= 1;
    }

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return periodInterval.contains(balanceInterval);
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public boolean isIdentifiedBy(final Long transactionId) {
        return getId().equals(transactionId);
    }

    public boolean isCredit() {
        return isDeposit() || isInterestPostingAndNotReversed();
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFeeAndNotReversed() || isAnnualFeeAndNotReversed() || isPayCharge();
    }

    public boolean isPayCharge() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isPayCharge();
    }

    public boolean isChargeTransaction() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isChargeTransaction();
    }

    public boolean isChargeTransactionAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isChargeTransaction() && isNotReversed();
    }

    public boolean isWaiveCharge() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWaiveCharge();
    }

    private boolean canOverriteSavingAccountRules() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return (isChargeTransaction() && chargePaidBy != null) ? chargePaidBy.canOverriteSavingAccountRules() : false;
    }

    public boolean canProcessBalanceCheck() {
        return isDebit() && !canOverriteSavingAccountRules();
    }

    public boolean isFeeCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return (isPayCharge() && chargePaidBy != null) ? chargePaidBy.isFeeCharge() : false;
    }

    public boolean isPenaltyCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return (isPayCharge() && chargePaidBy != null) ? chargePaidBy.isPenaltyCharge() : false;
    }

    public boolean isFeeChargeAndNotReversed() {
        return isFeeCharge() && isNotReversed();
    }

    public boolean isPenaltyChargeAndNotReversed() {
        return isPenaltyCharge() && isNotReversed();
    }

    public boolean isWaiveFeeCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return (isWaiveCharge() && chargePaidBy != null) ? chargePaidBy.isFeeCharge() : false;
    }

    public boolean isWaivePenaltyCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return (isWaiveCharge() && chargePaidBy != null) ? chargePaidBy.isPenaltyCharge() : false;
    }

    public boolean isWaiveFeeChargeAndNotReversed() {
        return isWaiveFeeCharge() && isNotReversed();
    }

    public boolean isWaivePenaltyChargeAndNotReversed() {
        return isWaivePenaltyCharge() && isNotReversed();
    }

    private SavingsAccountChargePaidBy getSavingsAccountChargePaidBy() {
        if (!CollectionUtils.isEmpty(this.savingsAccountChargesPaid)) { return this.savingsAccountChargesPaid.iterator().next(); }
        return null;
    }

    public Set<SavingsAccountChargePaidBy> getSavingsAccountChargesPaid() {
        return this.savingsAccountChargesPaid;
    }

    public void updateOverdraftAmount(BigDecimal overdraftAmount) {
        this.overdraftAmount = overdraftAmount;
    }

    public Money getOverdraftAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.overdraftAmount);
    }

    public Date createdDate() {
        return this.createdDate;
    }

    public boolean isPaymentForCurrentCharge(final SavingsAccountCharge savingsAccountCharge) {

        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        final boolean isChargePaidForCurrentCharge;
        if (chargePaidBy == null) {
            isChargePaidForCurrentCharge = false;
        } else if (chargePaidBy.getSavingsAccountCharge().equals(savingsAccountCharge)) {
            isChargePaidForCurrentCharge = true;
        } else {
            isChargePaidForCurrentCharge = false;
        }

        return isChargePaidForCurrentCharge;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }
}