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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.domain.interest.EndOfDayBalance;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.util.CollectionUtils;

/**
 * All monetary transactions against a savings account are modelled through this entity.
 */
@Entity
@Table(name = "m_savings_account_transaction")
public final class SavingsAccountTransaction extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_id", referencedColumnName = "id", nullable = false)
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "payment_detail_id", nullable = true)
    private PaymentDetail paymentDetail;

    @Column(name = "transaction_type_enum", nullable = false)
    private Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private Date dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "running_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal runningBalance;

    @Column(name = "cumulative_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal cumulativeBalance;

    @Temporal(TemporalType.DATE)
    @Column(name = "balance_end_date_derived", nullable = true)
    private Date balanceEndDate;

    @Column(name = "balance_number_of_days_derived", nullable = true)
    private Integer balanceNumberOfDays;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccountTransaction", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SavingsAccountChargePaidBy> savingsAccountChargesPaid = new HashSet<>();

    @Column(name = "overdraft_amount_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "appuser_id", nullable = true)
    private AppUser appUser;

    @Column(name = "is_manual", length = 1, nullable = true)
    private boolean isManualTransaction;

    @Column(name = "is_loan_disbursement", length = 1, nullable = true)
    private boolean isLoanDisbursement;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "savingsAccountTransaction")
    private List<SavingsAccountTransactionTaxDetails> taxDetails = new ArrayList<>();

    @Column(name = "release_id_of_hold_amount", length = 20)
    private Long releaseIdOfHoldAmountTransaction;

    @Column(name = "reason_for_block", nullable = true)
    private String reasonForBlock;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "savings_account_transaction_id", referencedColumnName = "id")
    private List<Note> notes = new ArrayList<>();

    @Column(name = "is_reversal", nullable = false)
    private boolean reversalTransaction;

    @Column(name = "original_transaction_id")
    private Long originalTxnId;

    @Column(name = "is_lien_transaction")
    private Boolean lienTransaction;

    SavingsAccountTransaction() {
        this.dateOf = null;
        this.typeOf = null;
        this.createdDate = null;
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.DEPOSIT.getValue(), date,
                createdDate, amount, isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser,
            final SavingsAccountTransactionType savingsAccountTransactionType) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, savingsAccountTransactionType.getValue(), date,
                createdDate, amount, isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction withdrawal(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.WITHDRAWAL.getValue(),
                date, createdDate, amount, isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction interestPosting(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.INTEREST_POSTING.getValue(), date,
                amount, isReversed, null, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction overdraftInterest(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date, final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue(), date,
                amount, isReversed, null, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction withdrawalFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue(), date, amount,
                isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction annualFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.ANNUAL_FEE.getValue(), date, amount,
                isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction charge(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.PAY_CHARGE.getValue(), date, amount,
                isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction from(final Integer transactionTypeEnum, final LocalDate transactionDate,
            final BigDecimal amount, final boolean isReversed, final BigDecimal runningBalance, final BigDecimal cumulativeBalance,
            final LocalDate balanceEndDate, final Integer balanceNumberOfDays, final BigDecimal overdraftAmount,
            final LocalDate createdDate, final boolean isManualTransaction, final Long releaseIdOfHoldAmountTransaction) {
        return new SavingsAccountTransaction(transactionTypeEnum, transactionDate, amount, isReversed, runningBalance, cumulativeBalance,
                balanceEndDate, balanceNumberOfDays, overdraftAmount, createdDate, isManualTransaction, releaseIdOfHoldAmountTransaction);
    }

    public static SavingsAccountTransaction waiver(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WAIVE_CHARGES.getValue(), date, amount,
                isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction initiateTransfer(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.INITIATE_TRANSFER.getValue(), date, new Date(),
                savingsAccount.getSummary().getAccountBalance(), isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction approveTransfer(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.APPROVE_TRANSFER.getValue(), date, new Date(),
                savingsAccount.getSummary().getAccountBalance(), isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction withdrawTransfer(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final AppUser appUser) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue(), date, new Date(),
                savingsAccount.getSummary().getAccountBalance(), isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction withHoldTax(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final Map<TaxComponent, BigDecimal> taxDetails) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        SavingsAccountTransaction accountTransaction = new SavingsAccountTransaction(savingsAccount, office,
                SavingsAccountTransactionType.WITHHOLD_TAX.getValue(), date, amount, isReversed, null, isManualTransaction,
                lienTransaction);
        updateTaxDetails(taxDetails, accountTransaction);
        return accountTransaction;
    }

    public static SavingsAccountTransaction escheat(final SavingsAccount savingsAccount, final LocalDate date, final AppUser appUser,
            final boolean accountTransaction) {
        final boolean isReversed = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, savingsAccount.office(), paymentDetail,
                SavingsAccountTransactionType.ESCHEAT.getValue(), date, new Date(), savingsAccount.getSummary().getAccountBalance(),
                isReversed, appUser, accountTransaction, lienTransaction);
    }

    public static void updateTaxDetails(final Map<TaxComponent, BigDecimal> taxDetails,
            final SavingsAccountTransaction accountTransaction) {
        if (taxDetails != null) {
            for (Map.Entry<TaxComponent, BigDecimal> mapEntry : taxDetails.entrySet()) {
                accountTransaction.getTaxDetails()
                        .add(new SavingsAccountTransactionTaxDetails(accountTransaction, mapEntry.getKey(), mapEntry.getValue()));
            }
        }
    }

    public static SavingsAccountTransaction copyTransaction(SavingsAccountTransaction accountTransaction) {
        return new SavingsAccountTransaction(accountTransaction.savingsAccount, accountTransaction.office, accountTransaction.paymentDetail,
                accountTransaction.typeOf, accountTransaction.transactionLocalDate(), accountTransaction.createdDate,
                accountTransaction.amount, accountTransaction.reversed, accountTransaction.appUser, accountTransaction.isManualTransaction,
                accountTransaction.lienTransaction);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final Integer typeOf,
            final LocalDate transactionLocalDate, final Money amount, final boolean isReversed, final AppUser appUser,
            final boolean isManualTransaction, final Boolean lienTransaction) {
        this(savingsAccount, office, null, typeOf, transactionLocalDate, new Date(), amount, isReversed, appUser, isManualTransaction,
                lienTransaction);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final Date createdDate, final Money amount,
            final boolean isReversed, final AppUser appUser, final boolean isManualTransaction, final Boolean lienTransaction) {
        this(savingsAccount, office, paymentDetail, typeOf, transactionLocalDate, createdDate, amount.getAmount(), isReversed, appUser,
                isManualTransaction, lienTransaction);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final Date createdDate, final BigDecimal amount,
            final boolean isReversed, final AppUser appUser, final boolean isManualTransaction, final Boolean lienTransaction) {
        this.savingsAccount = savingsAccount;
        this.office = office;
        this.typeOf = typeOf;
        this.dateOf = Date.from(transactionLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.amount = amount;
        this.reversed = isReversed;
        this.paymentDetail = paymentDetail;
        this.createdDate = createdDate;
        this.appUser = appUser;
        this.isManualTransaction = isManualTransaction;
        this.lienTransaction = lienTransaction;
    }

    private SavingsAccountTransaction(final Integer transactionTypeEnum, final LocalDate transactionDate, final BigDecimal amount,
            final boolean isReversed, final BigDecimal runningBalance, final BigDecimal cumulativeBalance, final LocalDate balanceEndDate,
            final Integer balanceNumberOfDays, final BigDecimal overdraftAmount, final LocalDate createdDate,
            final boolean isManualTransaction, final Long releaseIdOfHoldAmountTransaction) {
        this.typeOf = transactionTypeEnum;
        this.dateOf = Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.amount = amount;
        this.reversed = isReversed;
        this.runningBalance = runningBalance;
        this.cumulativeBalance = cumulativeBalance;
        this.balanceEndDate = Date.from(balanceEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.balanceNumberOfDays = balanceNumberOfDays;
        this.overdraftAmount = overdraftAmount;
        this.createdDate = Date.from(createdDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.isManualTransaction = isManualTransaction;
        this.releaseIdOfHoldAmountTransaction = releaseIdOfHoldAmountTransaction;
    }

    public static SavingsAccountTransaction holdAmount(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, Date createdDate, final AppUser appUser,
            final Boolean lienTransaction) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.AMOUNT_HOLD.getValue(),
                date, createdDate, amount, isReversed, appUser, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransaction releaseAmount(SavingsAccountTransaction accountTransaction, LocalDate transactionDate,
            Date createdDate, final AppUser appUser) {
        return new SavingsAccountTransaction(accountTransaction.savingsAccount, accountTransaction.office, accountTransaction.paymentDetail,
                SavingsAccountTransactionType.AMOUNT_RELEASE.getValue(), transactionDate, createdDate, accountTransaction.amount,
                accountTransaction.reversed, appUser, accountTransaction.isManualTransaction, accountTransaction.lienTransaction);
    }

    public static SavingsAccountTransaction reversal(SavingsAccountTransaction accountTransaction) {
        SavingsAccountTransaction sat = copyTransaction(accountTransaction);
        sat.reversed = false;
        sat.setReversalTransaction(true);
        sat.originalTxnId = accountTransaction.getId();
        return sat;

    }

    public LocalDate transactionLocalDate() {
        return LocalDate.ofInstant(this.dateOf.toInstant(), DateUtils.getDateTimeZoneOfTenant());
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

    public boolean isDividendPayout() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isDividendPayout();
    }

    public boolean isDividendPayoutAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isDividendPayout() && isNotReversed();
    }

    public boolean isWithdrawal() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithdrawal();
    }

    public boolean isPostInterestCalculationRequired() {
        return this.isDeposit() || this.isWithdrawal() || this.isChargeTransaction() || this.isDividendPayout() || this.isInterestPosting();
    }

    public boolean isInterestPostingAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isInterestPosting() && isNotReversed();
    }

    public boolean isInterestPosting() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isInterestPosting()
                || SavingsAccountTransactionType.fromInt(this.typeOf).isOverDraftInterestPosting();
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

    public void setLoanDisbursement(boolean isLoanDisbursement) {
        this.isLoanDisbursement = isLoanDisbursement;
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
            this.balanceEndDate = Date.from(this.transactionLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (endOfBalanceDate != null) {
            this.balanceEndDate = Date.from(endOfBalanceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.balanceEndDate = null;
        }
        this.balanceNumberOfDays = LocalDateInterval.create(getTransactionLocalDate(), endOfBalanceDate).daysInPeriodInclusiveOfEndDate();
        this.cumulativeBalance = Money.of(currency, this.runningBalance).multipliedBy(this.balanceNumberOfDays).getAmount();
    }

    public LocalDate getTransactionLocalDate() {
        return LocalDate.ofInstant(this.dateOf.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public Date getLastTransactionDate() {
        return this.dateOf;
    }

    public LocalDate getEndOfBalanceLocalDate() {
        return balanceEndDate == null ? null : LocalDate.ofInstant(balanceEndDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
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
         * Sending data in a map, though in savings we currently expect a transaction to always repay a single charge
         * (or may repay a part of a single charge too)
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

        if (!this.taxDetails.isEmpty()) {
            final List<Map<String, Object>> taxData = new ArrayList<>();
            for (final SavingsAccountTransactionTaxDetails taxDetails : this.taxDetails) {
                final Map<String, Object> taxDetailsData = new HashMap<>();
                taxDetailsData.put("amount", taxDetails.getAmount());
                if (taxDetails.getTaxComponent().getCreditAcount() != null) {
                    taxDetailsData.put("creditAccountId", taxDetails.getTaxComponent().getCreditAcount().getId());
                }
                taxData.add(taxDetailsData);
            }
            thisTransactionData.put("taxDetails", taxData);
        }

        return thisTransactionData;
    }

    public boolean isAfter(final LocalDate transactionDate) {
        return getTransactionLocalDate().isAfter(transactionDate);
    }

    public boolean isManualTransaction() {
        return this.isManualTransaction;
    }

    public void setPostInterestAsOn(boolean isManualTransaction) {
        this.isManualTransaction = isManualTransaction;
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
        if (isDeposit() || isDividendPayoutAndNotReversed()) {
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
        if (isDeposit() || isDividendPayoutAndNotReversed()) {
            endOfDayBalance = openingBalance.plus(getAmount(currency));
        } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {

            if (openingBalance.isGreaterThanZero() || this.savingsAccount.allowOverdraft()) {
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
            if (isDeposit() || isDividendPayoutAndNotReversed()) {
                // endOfDayBalance = openingBalance.plus(getAmount(currency));
                // if (endOfDayBalance.isLessThanZero()) {
                endOfDayBalance = endOfDayBalance.plus(getAmount(currency));
                // }
            } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
                // endOfDayBalance = openingBalance.minus(getAmount(currency));
                if (endOfDayBalance.isGreaterThanZero() || this.savingsAccount.allowOverdraft()) {
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
        return isDeposit() || isInterestPostingAndNotReversed() || isDividendPayoutAndNotReversed();
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFeeAndNotReversed() || isAnnualFeeAndNotReversed() || isPayCharge()
                || isOverdraftInterestAndNotReversed() || isWithHoldTaxAndNotReversed();
    }

    public boolean isWithHoldTaxAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isWithHoldTax() && isNotReversed();
    }

    public boolean isOverdraftInterestAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isIncomeFromInterest() && isNotReversed();
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

    public boolean isAmountOnHold() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isAmountOnHold();
    }

    public boolean isAmountRelease() {
        return SavingsAccountTransactionType.fromInt(this.typeOf).isAmountRelease();
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
        if (!CollectionUtils.isEmpty(this.savingsAccountChargesPaid)) {
            return this.savingsAccountChargesPaid.iterator().next();
        }
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

    public List<SavingsAccountTransactionTaxDetails> getTaxDetails() {
        return this.taxDetails;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void updateAmount(final Money amount) {
        this.amount = amount.getAmount();
    }

    public Integer getTypeOf() {
        return this.typeOf;
    }

    public SavingsAccount getSavingsAccount() {
        return this.savingsAccount;
    }

    public void setSavingsAccount(SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
    }

    public Date getDateOf() {
        return this.dateOf;
    }

    public PaymentDetail getPaymentDetail() {
        return this.paymentDetail;
    }

    public void updateReleaseId(Long releaseId) {
        this.releaseIdOfHoldAmountTransaction = releaseId;
    }

    public void updateReason(String reasonForBlock) {
        this.reasonForBlock = reasonForBlock;
    }

    public Long getReleaseIdOfHoldAmountTransaction() {
        return this.releaseIdOfHoldAmountTransaction;
    }

    public boolean isAmountOnHoldNotReleased() {
        return (isAmountOnHold() && getReleaseIdOfHoldAmountTransaction() == null);
    }

    public Long getOfficeId() {
        return this.office.getId();
    }

    public Date getBalanceEndDate() {
        return this.balanceEndDate;
    }

    public BigDecimal getCumulativeBalance() {
        return this.cumulativeBalance;
    }

    public Integer getBalanceNumberOfDays() {
        return this.balanceNumberOfDays;
    }

    public Long getAppUserId() {
        return this.appUser.getId();
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public boolean getIsManualTransaction() {
        return this.isManualTransaction;
    }

    public void setReversalTransaction(boolean reversalTransaction) {
        this.reversalTransaction = reversalTransaction;
    }

    public boolean isReversalTransaction() {
        return reversalTransaction;
    }
}
