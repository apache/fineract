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

import static org.apache.fineract.infrastructure.core.service.DateUtils.getSystemZoneId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.domain.interest.EndOfDayBalance;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.springframework.util.CollectionUtils;

/**
 * All monetary transactions against a savings account are modelled through this entity.
 */
@Entity
@Table(name = "m_savings_account_transaction")
public final class SavingsAccountTransaction extends AbstractAuditableWithUTCDateTimeCustom {

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

    @Column(name = "transaction_date", nullable = false)
    private LocalDate dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "running_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal runningBalance;

    @Column(name = "cumulative_balance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal cumulativeBalance;

    @Column(name = "balance_end_date_derived", nullable = true)
    private LocalDate balanceEndDate;

    @Column(name = "balance_number_of_days_derived", nullable = true)
    private Integer balanceNumberOfDays;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingsAccountTransaction", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SavingsAccountChargePaidBy> savingsAccountChargesPaid = new HashSet<>();

    @Column(name = "overdraft_amount_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal overdraftAmount;

    @Deprecated
    @Column(name = "created_date", nullable = true)
    private LocalDateTime createdDateToRemove;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

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

    @Column(name = "is_reversal", nullable = false)
    private boolean reversalTransaction;

    @Column(name = "original_transaction_id")
    private Long originalTxnId;

    @Column(name = "is_lien_transaction")
    private Boolean lienTransaction;

    @Column(name = "ref_no", nullable = true)
    private String refNo;

    SavingsAccountTransaction() {}

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final BigDecimal amount, final boolean isReversed,
            final boolean isManualTransaction, final Boolean lienTransaction, final String refNo) {
        this.savingsAccount = savingsAccount;
        this.office = office;
        this.typeOf = typeOf;
        this.dateOf = transactionLocalDate;
        this.amount = amount;
        this.reversed = isReversed;
        this.paymentDetail = paymentDetail;
        this.createdDateToRemove = null; // #audit backward compatibility deprecated
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
        this.isManualTransaction = isManualTransaction;
        this.lienTransaction = lienTransaction;
        this.refNo = refNo;
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final Integer typeOf,
            final LocalDate transactionLocalDate, final Money amount, final boolean isReversed, final boolean isManualTransaction,
            final Boolean lienTransaction, final String refNo) {
        this(savingsAccount, office, null, typeOf, transactionLocalDate, amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    private SavingsAccountTransaction(final SavingsAccount savingsAccount, final Office office, final PaymentDetail paymentDetail,
            final Integer typeOf, final LocalDate transactionLocalDate, final Money amount, final boolean isReversed,
            final boolean isManualTransaction, final Boolean lienTransaction, final String refNo) {
        this(savingsAccount, office, paymentDetail, typeOf, transactionLocalDate, amount.getAmount(), isReversed, isManualTransaction,
                lienTransaction, refNo);
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, final String refNo) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.DEPOSIT.getValue(), date,
                amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction deposit(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount,
            final SavingsAccountTransactionType savingsAccountTransactionType, final String refNo) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, savingsAccountTransactionType.getValue(), date, amount,
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction withdrawal(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, final String refNo) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.WITHDRAWAL.getValue(),
                date, amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction interestPosting(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.INTEREST_POSTING.getValue(), date,
                amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction overdraftInterest(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date, final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue(), date,
                amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction withdrawalFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final String refNo) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WITHDRAWAL_FEE.getValue(), date, amount,
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction annualFee(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.ANNUAL_FEE.getValue(), date, amount,
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction charge(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.PAY_CHARGE.getValue(), date, amount,
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction waiver(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, SavingsAccountTransactionType.WAIVE_CHARGES.getValue(), date, amount,
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction initiateTransfer(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.INITIATE_TRANSFER.getValue(), date, savingsAccount.getSummary().getAccountBalance(),
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction approveTransfer(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.APPROVE_TRANSFER.getValue(), date, savingsAccount.getSummary().getAccountBalance(),
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction withdrawTransfer(final SavingsAccount savingsAccount, final Office office,
            final LocalDate date) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail,
                SavingsAccountTransactionType.WITHDRAW_TRANSFER.getValue(), date, savingsAccount.getSummary().getAccountBalance(),
                isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction withHoldTax(final SavingsAccount savingsAccount, final Office office, final LocalDate date,
            final Money amount, final Map<TaxComponent, BigDecimal> taxDetails) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        final String refNo = null;
        SavingsAccountTransaction accountTransaction = new SavingsAccountTransaction(savingsAccount, office,
                SavingsAccountTransactionType.WITHHOLD_TAX.getValue(), date, amount, isReversed, isManualTransaction, lienTransaction,
                refNo);
        updateTaxDetails(taxDetails, accountTransaction);
        return accountTransaction;
    }

    public static SavingsAccountTransaction escheat(final SavingsAccount savingsAccount, final LocalDate date,
            final boolean accountTransaction) {
        final boolean isReversed = false;
        final PaymentDetail paymentDetail = null;
        final Boolean lienTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, savingsAccount.office(), paymentDetail,
                SavingsAccountTransactionType.ESCHEAT.getValue(), date, savingsAccount.getSummary().getAccountBalance(), isReversed,
                accountTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction copyTransaction(SavingsAccountTransaction accountTransaction) {
        return new SavingsAccountTransaction(accountTransaction.savingsAccount, accountTransaction.office, accountTransaction.paymentDetail,
                accountTransaction.typeOf, accountTransaction.getTransactionDate(), accountTransaction.amount, accountTransaction.reversed,
                accountTransaction.isManualTransaction, accountTransaction.lienTransaction, accountTransaction.refNo);
    }

    public static SavingsAccountTransaction holdAmount(final SavingsAccount savingsAccount, final Office office,
            final PaymentDetail paymentDetail, final LocalDate date, final Money amount, final Boolean lienTransaction) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final String refNo = null;
        return new SavingsAccountTransaction(savingsAccount, office, paymentDetail, SavingsAccountTransactionType.AMOUNT_HOLD.getValue(),
                date, amount, isReversed, isManualTransaction, lienTransaction, refNo);
    }

    public static SavingsAccountTransaction releaseAmount(SavingsAccountTransaction accountTransaction, LocalDate transactionDate) {
        return new SavingsAccountTransaction(accountTransaction.savingsAccount, accountTransaction.office, accountTransaction.paymentDetail,
                SavingsAccountTransactionType.AMOUNT_RELEASE.getValue(), transactionDate, accountTransaction.amount,
                accountTransaction.reversed, accountTransaction.isManualTransaction, accountTransaction.lienTransaction,
                accountTransaction.refNo);
    }

    public static SavingsAccountTransaction reversal(SavingsAccountTransaction accountTransaction) {
        SavingsAccountTransaction sat = copyTransaction(accountTransaction);
        sat.reversed = false;
        sat.setReversalTransaction(true);
        sat.originalTxnId = accountTransaction.getId();
        return sat;
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

    public SavingsAccount getSavingsAccount() {
        return this.savingsAccount;
    }

    public LocalDate getTransactionDate() {
        return this.dateOf;
    }

    public LocalDate getEndOfBalanceDate() {
        return balanceEndDate;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public void reverse() {
        this.reversed = true;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public void setAmount(final Money amount) {
        this.amount = amount == null ? null : amount.getAmount();
    }

    public BigDecimal getRunningBalance() {
        return this.runningBalance;
    }

    public Money getRunningBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.runningBalance);
    }

    public void setRunningBalance(Money balance) {
        this.runningBalance = balance == null ? null : balance.getAmount();
    }

    public boolean isManualTransaction() {
        return this.isManualTransaction;
    }

    public Set<SavingsAccountChargePaidBy> getSavingsAccountChargesPaid() {
        return this.savingsAccountChargesPaid;
    }

    public BigDecimal getOverdraftAmount() {
        return this.overdraftAmount;
    }

    public Money getOverdraftAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.overdraftAmount);
    }

    void setOverdraftAmount(Money overdraftAmount) {
        this.overdraftAmount = overdraftAmount == null ? null : overdraftAmount.getAmount();
    }

    public List<SavingsAccountTransactionTaxDetails> getTaxDetails() {
        return this.taxDetails;
    }

    public Integer getTypeOf() {
        return this.typeOf;
    }

    public SavingsAccountTransactionType getTransactionType() {
        return SavingsAccountTransactionType.fromInt(this.typeOf);
    }

    public LocalDate getDateOf() {
        return this.dateOf;
    }

    public String getRefNo() {
        return this.refNo;
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

    public MonetaryCurrency getCurrency() {
        return savingsAccount == null ? null : savingsAccount.getCurrency();
    }

    public Long getOfficeId() {
        return this.office.getId();
    }

    public LocalDate getBalanceEndDate() {
        return this.balanceEndDate;
    }

    public BigDecimal getCumulativeBalance() {
        return this.cumulativeBalance;
    }

    public Integer getBalanceNumberOfDays() {
        return this.balanceNumberOfDays;
    }

    public LocalDate getSubmittedOnDate() {
        return submittedOnDate;
    }

    public boolean isReversalTransaction() {
        return reversalTransaction;
    }

    void setReversalTransaction(boolean reversalTransaction) {
        this.reversalTransaction = reversalTransaction;
    }

    public boolean isDeposit() {
        return getTransactionType().isDeposit();
    }

    public boolean isDepositAndNotReversed() {
        return getTransactionType().isDeposit() && isNotReversed();
    }

    public boolean isDividendPayout() {
        return getTransactionType().isDividendPayout();
    }

    public boolean isDividendPayoutAndNotReversed() {
        return getTransactionType().isDividendPayout() && isNotReversed();
    }

    public boolean isWithdrawal() {
        return getTransactionType().isWithdrawal();
    }

    public boolean isPostInterestCalculationRequired() {
        return this.isDeposit() || this.isWithdrawal() || this.isChargeTransaction() || this.isDividendPayout() || this.isInterestPosting();
    }

    public boolean isInterestPostingAndNotReversed() {
        return getTransactionType().isInterestPosting() && isNotReversed();
    }

    public boolean isInterestPosting() {
        return getTransactionType().isInterestPosting() || getTransactionType().isOverDraftInterestPosting();
    }

    public boolean isWithdrawalFeeAndNotReversed() {
        return getTransactionType().isWithdrawalFee() && isNotReversed();
    }

    public boolean isWithdrawalFee() {
        return getTransactionType().isWithdrawalFee();
    }

    public boolean isAnnualFeeAndNotReversed() {
        return isAnnualFee() && isNotReversed();
    }

    public boolean isAnnualFee() {
        return getTransactionType().isAnnualFee();
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public boolean isTransferInitiation() {
        return getTransactionType().isTransferInitiation();
    }

    public boolean isTransferApproval() {
        return getTransactionType().isTransferApproval();
    }

    public boolean isTransferRejection() {
        return getTransactionType().isTransferRejection();
    }

    public boolean isTransferWithdrawal() {
        return getTransactionType().isTransferWithdrawal();
    }

    public boolean isTransferRelatedTransaction() {
        return isTransferInitiation() || isTransferApproval() || isTransferRejection() || isTransferWithdrawal();
    }

    public void zeroBalanceFields() {
        this.runningBalance = null;
        this.cumulativeBalance = null;
        this.balanceEndDate = null;
        this.balanceNumberOfDays = null;
    }

    public void updateCumulativeBalanceAndDates(final MonetaryCurrency currency, final LocalDate endOfBalanceDate) {
        // balance end date should not be before transaction date
        if (endOfBalanceDate != null && DateUtils.isBefore(endOfBalanceDate, this.getTransactionDate())) {
            this.balanceEndDate = this.getTransactionDate();
        } else {
            this.balanceEndDate = endOfBalanceDate;
        }
        this.balanceNumberOfDays = LocalDateInterval.create(getTransactionDate(), endOfBalanceDate).daysInPeriodInclusiveOfEndDate();
        this.cumulativeBalance = Money.of(currency, this.runningBalance).multipliedBy(this.balanceNumberOfDays).getAmount();
    }

    public boolean isAcceptableForDailyBalance(final LocalDateInterval interestPeriodInterval) {
        return isNotReversed() && interestPeriodInterval.contains(getTransactionDate()) && isABalanceForAtLeastOneDay();
    }

    private boolean isABalanceForAtLeastOneDay() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays > 0;
    }

    public boolean hasNotAmount(final Money amountToCheck) {
        final Money transactionAmount = getAmount(amountToCheck.getCurrency());
        return transactionAmount.isNotEqualTo(amountToCheck);
    }

    public Map<String, Object> toMapData(final String currencyCode) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", getId());
        thisTransactionData.put("officeId", this.office.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", isReversed());
        thisTransactionData.put("date", getTransactionDate());
        thisTransactionData.put("currencyCode", currencyCode);
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

    public boolean isBefore(final LocalDate date) {
        return DateUtils.isBefore(getTransactionDate(), date);
    }

    public boolean isAfter(final LocalDate date) {
        return DateUtils.isAfter(getTransactionDate(), date);
    }

    public boolean occursOn(final LocalDate date) {
        return DateUtils.isEqual(getTransactionDate(), date);
    }

    public EndOfDayBalance toEndOfDayBalance(final LocalDateInterval periodInterval, final MonetaryCurrency currency) {
        final Money endOfDayBalance = Money.of(currency, this.runningBalance);
        final Money openingBalance = endOfDayBalance;

        LocalDate balanceDate = periodInterval.startDate();

        int numberOfDays = periodInterval.daysInPeriodInclusiveOfEndDate();
        if (periodInterval.contains(getTransactionDate())) {
            balanceDate = getTransactionDate();
            final LocalDateInterval newInterval = LocalDateInterval.create(getTransactionDate(), periodInterval.endDate());
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

        int numberOfDays = LocalDateInterval.create(getTransactionDate(), nextTransactionDate).daysInPeriodInclusiveOfEndDate();
        if (!openingBalance.isEqualTo(endOfDayBalance) && numberOfDays > 1) {
            numberOfDays = numberOfDays - 1;
        }
        return EndOfDayBalance.from(getTransactionDate(), openingBalance, endOfDayBalance, numberOfDays);
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
                endOfDayBalance = getRunningBalance(currency);
            }
        }

        return EndOfDayBalance.from(getTransactionDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public EndOfDayBalance toEndOfDayBalanceBoundedBy(final Money openingBalance, final LocalDateInterval boundedBy) {
        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();

        int numberOfDaysOfBalance = this.balanceNumberOfDays;

        LocalDate balanceStartDate = getTransactionDate();
        LocalDate balanceEndDate = getEndOfBalanceDate();

        if (DateUtils.isBefore(balanceStartDate, boundedBy.startDate())) {
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
                    endOfDayBalance = getRunningBalance(currency);
                }
            }
        }

        if (DateUtils.isAfter(balanceEndDate, boundedBy.endDate())) {
            balanceEndDate = boundedBy.endDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        }

        return EndOfDayBalance.from(balanceStartDate, openingBalance, endOfDayBalance, numberOfDaysOfBalance);
    }

    public boolean isBalanceInExistencesForOneDayOrMore() {
        return this.balanceNumberOfDays != null && this.balanceNumberOfDays >= 1;
    }

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceDate());
        return periodInterval.contains(balanceInterval);
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public boolean isIdentifiedBy(final Long transactionId) {
        return getId().equals(transactionId);
    }

    public boolean isCredit() {
        return isCreditType() && !isReversed() && !isReversalTransaction();
    }

    public boolean isCreditType() {
        return getTransactionType().isCredit();
    }

    public boolean isDebit() {
        return isDebitType() && !isReversed() && !isReversalTransaction();
    }

    public boolean isDebitType() {
        return getTransactionType().isDebit();
    }

    public boolean isWithHoldTaxAndNotReversed() {
        return getTransactionType().isWithHoldTax() && isNotReversed();
    }

    public boolean isOverdraftInterestAndNotReversed() {
        return getTransactionType().isIncomeFromInterest() && isNotReversed();
    }

    public boolean isPayCharge() {
        return getTransactionType().isPayCharge();
    }

    public boolean isChargeTransaction() {
        return getTransactionType().isChargeTransaction();
    }

    public boolean isChargeTransactionAndNotReversed() {
        return getTransactionType().isChargeTransaction() && isNotReversed();
    }

    public boolean isWaiveCharge() {
        return getTransactionType().isWaiveCharge();
    }

    public boolean isAmountOnHold() {
        return getTransactionType().isAmountOnHold();
    }

    public boolean isAmountRelease() {
        return getTransactionType().isAmountRelease();
    }

    private boolean canOverriteSavingAccountRules() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return isChargeTransaction() && chargePaidBy != null && chargePaidBy.canOverriteSavingAccountRules();
    }

    public boolean canProcessBalanceCheck() {
        return isDebit() && !canOverriteSavingAccountRules();
    }

    public boolean isFeeCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return isPayCharge() && chargePaidBy != null && chargePaidBy.isFeeCharge();
    }

    public boolean isPenaltyCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return isPayCharge() && chargePaidBy != null && chargePaidBy.isPenaltyCharge();
    }

    public boolean isFeeChargeAndNotReversed() {
        return isFeeCharge() && isNotReversed();
    }

    public boolean isPenaltyChargeAndNotReversed() {
        return isPenaltyCharge() && isNotReversed();
    }

    public boolean isWaiveFeeCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return isWaiveCharge() && chargePaidBy != null && chargePaidBy.isFeeCharge();
    }

    public boolean isWaivePenaltyCharge() {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return isWaiveCharge() && chargePaidBy != null && chargePaidBy.isPenaltyCharge();
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

    public boolean isPaymentForCurrentCharge(final SavingsAccountCharge savingsAccountCharge) {
        final SavingsAccountChargePaidBy chargePaidBy = getSavingsAccountChargePaidBy();
        return chargePaidBy != null && chargePaidBy.getSavingsAccountCharge().equals(savingsAccountCharge);
    }

    @Override
    public Optional<OffsetDateTime> getCreatedDate() {
        // #audit backward compatibility keep system datetime
        return Optional.ofNullable(super.getCreatedDate()
                .orElse(createdDateToRemove == null ? null : createdDateToRemove.atZone(getSystemZoneId()).toOffsetDateTime()));
    }

    public boolean isAmountOnHoldNotReleased() {
        return (isAmountOnHold() && getReleaseIdOfHoldAmountTransaction() == null);
    }

    public SavingsAccountTransactionDetailsForPostingPeriod toSavingsAccountTransactionDetailsForPostingPeriod(MonetaryCurrency currency,
            boolean isAllowOverDraft) {
        return new SavingsAccountTransactionDetailsForPostingPeriod(getId(), this.dateOf, this.balanceEndDate, this.runningBalance,
                this.amount, currency, this.balanceNumberOfDays, isDeposit(), isWithdrawal(), isAllowOverDraft,
                isChargeTransactionAndNotReversed(), isDividendPayoutAndNotReversed());
    }
}
