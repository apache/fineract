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
package org.apache.fineract.portfolio.savings.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargesPaidByData;
import org.apache.fineract.portfolio.savings.domain.interest.EndOfDayBalance;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxDetailsData;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object representing a savings account transaction.
 */
@SuppressWarnings("unused")
public final class SavingsAccountTransactionData implements Serializable {

    private Long id;
    private final SavingsAccountTransactionEnumData transactionType;
    private final Long accountId;
    private final String accountNo;
    private final LocalDate date;
    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;
    private final BigDecimal amount;
    private final BigDecimal outstandingChargeAmount;
    private BigDecimal runningBalance;
    private boolean reversed;
    private final AccountTransferData transfer;
    private Date submittedOnDate;
    private final boolean interestedPostedAsOn;
    private final String submittedByUsername;
    private final String note;
    private final boolean isManualTransaction;
    private final Boolean isReversal;
    private final Long originalTransactionId;
    private final Boolean lienTransaction;
    private final Long releaseTransactionId;
    private final String reasonForBlock;
    private Set<SavingsAccountChargesPaidByData> chargesPaidByData = new HashSet<>();

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    // import fields
    private transient Integer rowIndex;
    private transient Long savingsAccountId;
    private String dateFormat;
    private String locale;
    private LocalDate transactionDate;
    private BigDecimal transactionAmount;
    private Long paymentTypeId;
    private String accountNumber;
    private String checkNumber;
    private String routingCode;
    private String receiptNumber;
    private String bankNumber;
    private BigDecimal cumulativeBalance;
    private LocalDate balanceEndDate;
    private transient List<TaxDetailsData> taxDetails = new ArrayList<>();
    private Integer balanceNumberOfDays;
    private BigDecimal overdraftAmount;
    private transient Long modifiedId;
    private transient String refNo;

    public static SavingsAccountTransactionData importInstance(BigDecimal transactionAmount, LocalDate transactionDate, Long paymentTypeId,
            String accountNumber, String checkNumber, String routingCode, String receiptNumber, String bankNumber, Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex, String locale, String dateFormat) {
        return new SavingsAccountTransactionData(transactionAmount, transactionDate, paymentTypeId, accountNumber, checkNumber, routingCode,
                receiptNumber, bankNumber, savingsAccountId, transactionType, rowIndex, locale, dateFormat, false);
    }

    public static SavingsAccountTransactionData interestPosting(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.INTEREST_POSTING;
        SavingsAccountTransactionEnumData savingsAccountTransactionEnumData = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        return new SavingsAccountTransactionData(amount.getAmount(), date, savingsAccount.getId(), savingsAccountTransactionEnumData,
                isReversed, null, isManualTransaction, lienTransaction);
    }

    public static SavingsAccountTransactionData overdraftInterest(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final boolean isReversed = false;
        final Boolean lienTransaction = false;
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.OVERDRAFT_INTEREST;
        SavingsAccountTransactionEnumData savingsAccountTransactionEnumData = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        return new SavingsAccountTransactionData(amount.getAmount(), date, savingsAccount.getId(), savingsAccountTransactionEnumData,
                isReversed, null, isManualTransaction, lienTransaction);
    }

    public List<TaxDetailsData> getTaxDetails() {
        return this.taxDetails;
    }

    public boolean isInterestPostingAndNotReversed() {
        return this.transactionType.isInterestPosting() && isNotReversed();
    }

    public void setTaxDetails(final TaxDetailsData taxDetails) {
        this.taxDetails.add(taxDetails);
    }

    public boolean isOverdraftInterestAndNotReversed() {
        return this.transactionType.isIncomeFromInterest() && isNotReversed();
    }

    public boolean isDebit() {
        return isWithdrawal() || isWithdrawalFeeAndNotReversed() || isAnnualFeeAndNotReversed() || isPayCharge()
                || isOverdraftInterestAndNotReversed() || isWithHoldTaxAndNotReversed();
    }

    public boolean isWithdrawalFeeAndNotReversed() {
        return this.transactionType.isWithdrawalFee() && isNotReversed();
    }

    public boolean isPayCharge() {
        return this.transactionType.isPayCharge();
    }

    public void updateRunningBalance(final Money balance) {
        this.runningBalance = balance.getAmount();
    }

    public void updateOverdraftAmount(BigDecimal overdraftAmount) {
        this.overdraftAmount = overdraftAmount;
    }

    public boolean isAmountOnHold() {
        return this.transactionType.isAmountOnHold();
    }

    public boolean isAnnualFeeAndNotReversed() {
        return isAnnualFee() && isNotReversed();
    }

    public boolean isAnnualFee() {
        return this.transactionType.isAnnualFee();
    }

    public Money getRunningBalance(final CurrencyData currency) {
        return Money.of(currency, this.runningBalance);
    }

    public boolean isDepositAndNotReversed() {
        return this.transactionType.isDeposit() && isNotReversed();
    }

    public boolean isDividendPayoutAndNotReversed() {
        return this.transactionType.isDividendPayout() && isNotReversed();
    }

    public void setRefNo(final String uuid) {
        this.refNo = uuid;
    }

    public String getRefNo() {
        return this.refNo;
    }

    public void setBalanceNumberOfDays(final Integer balanceNumberOfDays) {
        this.balanceNumberOfDays = balanceNumberOfDays;
    }

    public Integer getBalanceNumberOfDays() {
        return this.balanceNumberOfDays;
    }

    public EndOfDayBalance toEndOfDayBalance(final Money openingBalance) {
        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();
        if (isDeposit() || isDividendPayoutAndNotReversed()) {
            endOfDayBalance = openingBalance.plus(getAmount());
        } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {

            if (openingBalance.isGreaterThanZero()) {
                endOfDayBalance = openingBalance.minus(getAmount());
            } else {
                endOfDayBalance = Money.of(currency, this.runningBalance);
            }
        }

        return EndOfDayBalance.from(getTransactionLocalDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public boolean isChargeTransactionAndNotReversed() {
        return this.transactionType.isChargeTransaction() && isNotReversed();
    }

    public LocalDate getLastTransactionDate() {
        return this.transactionDate;
    }

    public boolean occursOn(final LocalDate occursOnDate) {
        return getTransactionLocalDate().isEqual(occursOnDate);
    }

    public LocalDate getTransactionLocalDate() {
        return this.transactionDate;
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
                endOfDayBalance = endOfDayBalance.plus(getAmount());
            } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
                if (endOfDayBalance.isGreaterThanZero()) {
                    endOfDayBalance = endOfDayBalance.minus(getAmount());
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

    public void reverse() {
        this.reversed = true;
    }

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return periodInterval.contains(balanceInterval);
    }

    public LocalDate getEndOfBalanceLocalDate() {
        return this.balanceEndDate == null ? null : this.balanceEndDate;
    }

    public void zeroBalanceFields() {
        this.runningBalance = null;
        this.cumulativeBalance = null;
        this.balanceEndDate = null;
        this.balanceNumberOfDays = null;
    }

    public boolean isAmountRelease() {
        return this.transactionType.isAmountRelease();
    }

    public boolean isCredit() {
        return isDeposit() || isInterestPostingAndNotReversed() || isDividendPayoutAndNotReversed();
    }

    public boolean isDeposit() {
        return this.transactionType.isDeposit();
    }

    public static SavingsAccountTransactionData copyTransaction(SavingsAccountTransactionData accountTransaction) {
        return new SavingsAccountTransactionData(accountTransaction.getSavingsAccountId(), null, accountTransaction.getPaymentDetailData(),
                accountTransaction.getTransactionType(), accountTransaction.getTransactionLocalDate(),
                accountTransaction.getSubmittedOnDate(), accountTransaction.getAmount(), accountTransaction.isReversed(), null,
                accountTransaction.isManualTransaction(), accountTransaction.lienTransaction);
    }

    private SavingsAccountTransactionData(final Long savingsId, final Long officeId, final PaymentDetailData paymentDetailData,
            final SavingsAccountTransactionEnumData savingsAccountTransactionType, final LocalDate transactionDate, final Date createdDate,
            final BigDecimal amount, final boolean isReversed, final Long userId, final boolean isManualTransaction,
            final Boolean lienTransaction) {
        this.savingsAccountId = savingsId;
        this.paymentDetailData = paymentDetailData;
        this.transactionType = savingsAccountTransactionType;
        this.transactionDate = transactionDate;
        this.submittedOnDate = createdDate;
        this.amount = amount;
        this.isManualTransaction = isManualTransaction;
        this.lienTransaction = lienTransaction;
        this.id = null;
        this.accountId = null;
        this.accountNo = null;
        this.date = null;
        this.currency = null;
        this.outstandingChargeAmount = null;
        this.runningBalance = null;
        this.reversed = isReversed;
        this.transfer = null;
        this.interestedPostedAsOn = false;
        this.rowIndex = null;
        this.dateFormat = null;
        this.locale = null;
        this.transactionAmount = null;
        this.paymentTypeId = null;
        this.accountNumber = null;
        this.checkNumber = null;
        this.routingCode = null;
        this.receiptNumber = null;
        this.bankNumber = null;
        this.paymentTypeOptions = null;
        this.submittedByUsername = null;
        this.note = null;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    public boolean isChargeTransaction() {
        return this.transactionType.isChargeTransaction();
    }

    public Set<SavingsAccountChargesPaidByData> getSavingsAccountChargesPaid() {
        return this.chargesPaidByData;
    }

    public void updateCumulativeBalanceAndDates(final MonetaryCurrency currency, final LocalDate endOfBalanceDate) {
        // balance end date should not be before transaction date
        if (endOfBalanceDate != null && endOfBalanceDate.isBefore(this.transactionDate)) {
            this.balanceEndDate = this.transactionDate;
        } else if (endOfBalanceDate != null) {
            this.balanceEndDate = endOfBalanceDate;
        } else {
            this.balanceEndDate = null;
        }
        this.balanceNumberOfDays = LocalDateInterval.create(getTransactionLocalDate(), endOfBalanceDate).daysInPeriodInclusiveOfEndDate();
        this.cumulativeBalance = Money.of(currency, this.runningBalance).multipliedBy(this.balanceNumberOfDays).getAmount();
    }

    public boolean hasNotAmount(final Money amountToCheck) {
        final Money transactionAmount = getAmount(amountToCheck.getCurrency());
        return transactionAmount.isNotEqualTo(amountToCheck);
    }

    public boolean isFeeChargeAndNotReversed() {
        return isFeeCharge() && isNotReversed();
    }

    public boolean isFeeCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return (isPayCharge() && chargePaidBy != null) ? chargePaidBy.isFeeCharge() : false;
    }

    public void setChargesPaidByData(final SavingsAccountChargesPaidByData savingsAccountChargesPaidByData) {
        this.chargesPaidByData.add(savingsAccountChargesPaidByData);
    }

    public void setOverdraftAmount(final BigDecimal overdraftAmount) {
        this.overdraftAmount = overdraftAmount;
    }

    public boolean isPenaltyChargeAndNotReversed() {
        return isPenaltyCharge() && isNotReversed();
    }

    public boolean isPenaltyCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return (isPayCharge() && chargePaidBy != null) ? chargePaidBy.isPenaltyCharge() : false;
    }

    public boolean isWaiveFeeChargeAndNotReversed() {
        return isWaiveFeeCharge() && isNotReversed();
    }

    public boolean isWaiveFeeCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return (isWaiveCharge() && chargePaidBy != null) ? chargePaidBy.isFeeCharge() : false;
    }

    public boolean isWaiveCharge() {
        return SavingsAccountTransactionType.fromInt(this.transactionType.getId().intValue()).isWaiveCharge();
    }

    public boolean isWaivePenaltyChargeAndNotReversed() {
        return isWaivePenaltyCharge() && isNotReversed();
    }

    public boolean isWaivePenaltyCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return (isWaiveCharge() && chargePaidBy != null) ? chargePaidBy.isPenaltyCharge() : false;
    }

    private SavingsAccountChargesPaidByData getSavingsAccountChargePaidBy() {
        if (!CollectionUtils.isEmpty(this.chargesPaidByData)) {
            return this.chargesPaidByData.iterator().next();
        }
        return null;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public static SavingsAccountTransactionData withHoldTax(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final Map<TaxComponentData, BigDecimal> taxDetails) {
        final boolean isReversed = false;
        final boolean isManualTransaction = false;
        final Boolean lienTransaction = false;
        SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.WITHHOLD_TAX;
        SavingsAccountTransactionEnumData transactionType = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        SavingsAccountTransactionData accountTransaction = new SavingsAccountTransactionData(amount.getAmount(), date,
                savingsAccount.getId(), transactionType, isReversed, null, isManualTransaction, lienTransaction);
        updateTaxDetails(taxDetails, accountTransaction);
        return accountTransaction;
    }

    public static void updateTaxDetails(final Map<TaxComponentData, BigDecimal> taxDetails,
            final SavingsAccountTransactionData accountTransaction) {
        if (taxDetails != null) {
            for (Map.Entry<TaxComponentData, BigDecimal> mapEntry : taxDetails.entrySet()) {
                accountTransaction.getTaxDetails().add(new TaxDetailsData(mapEntry.getKey(), mapEntry.getValue()));
            }
        }
    }

    public Map<String, Object> toMapData(final CurrencyData currencyData, final Long officeId) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                .transactionType(this.transactionType.getId().intValue());

        thisTransactionData.put("id", getId());
        thisTransactionData.put("officeId", officeId);
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", Boolean.valueOf(isReversed()));
        thisTransactionData.put("date", getTransactionLocalDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("overdraftAmount", this.overdraftAmount);

        if (this.paymentDetailData != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetailData.getPaymentType().getId());
        }

        /***
         * Sending data in a map, though in savings we currently expect a transaction to always repay a single charge
         * (or may repay a part of a single charge too)
         ***/
        if (!this.chargesPaidByData.isEmpty()) {
            final List<Map<String, Object>> savingsChargesPaidData = new ArrayList<>();
            for (final SavingsAccountChargesPaidByData chargePaidBy : this.chargesPaidByData) {
                final Map<String, Object> savingChargePaidData = new LinkedHashMap<>();
                savingChargePaidData.put("chargeId", chargePaidBy.getSavingsAccountCharge());
                savingChargePaidData.put("isPenalty", chargePaidBy.getSavingsAccountCharge().isPenalty());
                savingChargePaidData.put("savingsChargeId", chargePaidBy.getSavingsAccountCharge().getId());
                savingChargePaidData.put("amount", chargePaidBy.getAmount());

                savingsChargesPaidData.add(savingChargePaidData);
            }
            thisTransactionData.put("savingsChargesPaid", savingsChargesPaidData);
        }

        if (this.taxDetails != null && !this.taxDetails.isEmpty()) {
            final List<Map<String, Object>> taxData = new ArrayList<>();
            for (final TaxDetailsData taxDetails : this.taxDetails) {
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

    private SavingsAccountTransactionData(BigDecimal transactionAmount, LocalDate transactionDate, Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, boolean isReversed, String locale, boolean isManualTransaction,
            final Boolean lienTransaction) {
        this.id = null;
        this.transactionType = transactionType;
        this.accountId = null;
        this.accountNo = null;
        this.date = transactionDate;
        this.currency = null;
        this.paymentDetailData = null;
        this.amount = transactionAmount;
        this.outstandingChargeAmount = null;
        this.runningBalance = null;
        this.reversed = isReversed;
        this.transfer = null;
        this.submittedOnDate = Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.interestedPostedAsOn = false;
        this.rowIndex = null;
        this.savingsAccountId = savingsAccountId;
        this.dateFormat = null;
        this.locale = locale;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentTypeId = null;
        this.accountNumber = null;
        this.checkNumber = null;
        this.routingCode = null;
        this.receiptNumber = null;
        this.bankNumber = null;
        this.paymentTypeOptions = null;
        this.submittedByUsername = null;
        this.note = null;
        this.isManualTransaction = isManualTransaction;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    private SavingsAccountTransactionData(BigDecimal transactionAmount, LocalDate transactionDate, Long paymentTypeId, String accountNumber,
            String checkNumber, String routingCode, String receiptNumber, String bankNumber, Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex, String locale, String dateFormat,
            final Boolean lienTransaction) {
        this.id = null;
        this.transactionType = transactionType;
        this.accountId = null;
        this.accountNo = null;
        this.date = null;
        this.currency = null;
        this.paymentDetailData = null;
        this.amount = null;
        this.outstandingChargeAmount = null;
        this.runningBalance = null;
        this.reversed = false;
        this.transfer = null;
        this.submittedOnDate = null;
        this.interestedPostedAsOn = false;
        this.rowIndex = rowIndex;
        this.savingsAccountId = savingsAccountId;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentTypeId = paymentTypeId;
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
        this.paymentTypeOptions = null;
        this.submittedByUsername = null;
        this.note = null;
        this.isManualTransaction = false;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    private SavingsAccountTransactionData(Integer id, BigDecimal transactionAmount, LocalDate transactionDate, Long paymentTypeId,
            String accountNumber, String checkNumber, String routingCode, String receiptNumber, String bankNumber, Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex, String locale, String dateFormat,
            BigDecimal cumulativeBalance, final Boolean lienTransaction) {
        this.id = null;
        this.transactionType = transactionType;
        this.accountId = null;
        this.accountNo = null;
        this.date = null;
        this.currency = null;
        this.paymentDetailData = null;
        this.amount = null;
        this.outstandingChargeAmount = null;
        this.runningBalance = null;
        this.reversed = false;
        this.submittedOnDate = null;
        this.interestedPostedAsOn = false;
        this.rowIndex = rowIndex;
        this.savingsAccountId = savingsAccountId;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.paymentTypeId = paymentTypeId;
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
        this.routingCode = routingCode;
        this.receiptNumber = receiptNumber;
        this.bankNumber = bankNumber;
        this.paymentTypeOptions = null;
        this.submittedByUsername = null;
        this.note = null;
        this.cumulativeBalance = cumulativeBalance;
        this.transfer = null;
        this.isManualTransaction = false;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public PaymentDetailData getPaymentDetailData() {
        return this.paymentDetailData;
    }

    public Long getId() {
        return this.id;
    }

    public SavingsAccountTransactionEnumData getTransactionType() {
        return transactionType;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public boolean isWithdrawal() {
        return this.transactionType.isWithdrawal();
    }

    public boolean isInterestPosting() {
        return this.transactionType.isInterestPosting() || this.transactionType.isOverDraftInterestPosting();
    }

    public boolean isManualTransaction() {
        return this.isManualTransaction;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public BigDecimal getRunningBalance() {
        return this.runningBalance;
    }

    public BigDecimal getCumulativeBalance() {
        return this.cumulativeBalance;
    }

    public LocalDate getBalanceEndDate() {
        return this.balanceEndDate;
    }

    public Date getSubmittedOnDate() {
        return this.submittedOnDate;
    }

    public boolean isWithHoldTaxAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.transactionType.getId().intValue()).isWithHoldTax() && isNotReversed();
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public BigDecimal getOverdraftAmount() {
        return this.overdraftAmount;
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionLocalDate(), getEndOfBalanceLocalDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final BigDecimal cumulativeBalance,
            final LocalDate balanceEndDate) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, submittedOnDate, paymentTypeOptions, interestedPostedAsOn,
                cumulativeBalance, balanceEndDate, false);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final LocalDate submittedOnDate, final Collection<PaymentTypeData> paymentTypeOptions,
            final boolean interestedPostedAsOn, final BigDecimal cumulativeBalance, final LocalDate balanceEndDate,
            final Boolean lienTransaction) {

        this(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency, amount, outstandingChargeAmount,
                runningBalance, reversed, paymentTypeOptions, submittedOnDate, interestedPostedAsOn, cumulativeBalance, balanceEndDate,
                lienTransaction);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final Collection<PaymentTypeData> paymentTypeOptions, final LocalDate submittedOnDate,
            final boolean interestedPostedAsOn, final BigDecimal cumulativeBalance, final LocalDate balanceEndDate,
            final Boolean lienTransaction) {
        this.id = id;
        this.transactionDate = date;
        this.transactionType = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.outstandingChargeAmount = outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.paymentTypeOptions = paymentTypeOptions;
        this.submittedOnDate = Date.from(submittedOnDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.interestedPostedAsOn = interestedPostedAsOn;
        this.cumulativeBalance = cumulativeBalance;
        this.transfer = null;
        this.submittedByUsername = null;
        this.note = null;
        this.balanceEndDate = balanceEndDate;
        this.isManualTransaction = false;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final boolean interestedPostedAsOn,
            final String submittedByUsername, final String note) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, transfer, paymentTypeOptions, interestedPostedAsOn,
                submittedByUsername, note, false);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final LocalDate submittedOnDate, final boolean interestedPostedAsOn,
            final String submittedByUsername, final String note, final Boolean isReversal, final Long originalTransactionId,
            final Boolean lienTransaction, final Long releaseTransactionId, final String reasonForBlock) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, transfer, paymentTypeOptions, submittedOnDate,
                interestedPostedAsOn, submittedByUsername, note, isReversal, originalTransactionId, lienTransaction, releaseTransactionId,
                reasonForBlock);
    }

    public static SavingsAccountTransactionData create(final Long id) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, null, null, null, null, null, null, null, null, null, false, null, paymentTypeOptions,
                null, false, null, null, null, null, false, null, null);
    }

    public static SavingsAccountTransactionData template(final Long savingsId, final String savingsAccountNo,
            final LocalDate defaultLocalDate, final CurrencyData currency) {
        final Long id = null;
        final SavingsAccountTransactionEnumData transactionType = null;
        final BigDecimal amount = null;
        final BigDecimal outstandingChargeAmount = null;
        final BigDecimal runningBalance = null;
        final boolean reversed = false;
        final PaymentDetailData paymentDetailData = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final boolean interestedPostedAsOn = false;
        final String submittedByUsername = null;
        final String note = null;
        final Boolean lienTransaction = false;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, defaultLocalDate,
                currency, amount, outstandingChargeAmount, runningBalance, reversed, null, null, interestedPostedAsOn, submittedByUsername,
                note, lienTransaction);
    }

    public static SavingsAccountTransactionData templateOnTop(final SavingsAccountTransactionData savingsAccountTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, savingsAccountTransactionData.transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, savingsAccountTransactionData.date, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.amount, savingsAccountTransactionData.outstandingChargeAmount,
                savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.reversed,
                savingsAccountTransactionData.transfer, paymentTypeOptions, savingsAccountTransactionData.interestedPostedAsOn,
                savingsAccountTransactionData.submittedByUsername, savingsAccountTransactionData.note,
                savingsAccountTransactionData.lienTransaction);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,
            final boolean interestedPostedAsOn, final String submittedByUsername, final String note, final Boolean lienTransaction) {

        this(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency, amount, outstandingChargeAmount,
                runningBalance, reversed, transfer, paymentTypeOptions, date, interestedPostedAsOn, submittedByUsername, note, null, null,
                lienTransaction, null, null);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final String submittedByUsername, final String note,
            final Boolean isReversal, final Long originalTransactionId, final Boolean lienTransaction, final Long releaseTransactionId,
            final String reasonForBlock) {
        this.id = id;
        this.transactionType = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.outstandingChargeAmount = outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.transfer = transfer;
        this.paymentTypeOptions = paymentTypeOptions;
        if (submittedOnDate != null) {
            this.submittedOnDate = Date.from(submittedOnDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.submittedOnDate = null;
        }

        this.interestedPostedAsOn = interestedPostedAsOn;
        this.submittedByUsername = submittedByUsername;
        this.note = note;
        this.isManualTransaction = false;
        this.isReversal = isReversal;
        this.originalTransactionId = originalTransactionId;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = releaseTransactionId;
        this.reasonForBlock = reasonForBlock;
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final BigDecimal cumulativeBalance,
            final Boolean lienTransaction) {
        this.id = id;
        this.transactionType = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.outstandingChargeAmount = outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.transfer = transfer;
        this.paymentTypeOptions = paymentTypeOptions;
        if (submittedOnDate != null) {
            this.submittedOnDate = Date.from(submittedOnDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.submittedOnDate = null;
        }

        this.interestedPostedAsOn = interestedPostedAsOn;
        this.submittedByUsername = null;
        this.note = null;
        this.cumulativeBalance = cumulativeBalance;
        this.isManualTransaction = false;
        this.isReversal = null;
        this.originalTransactionId = null;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = null;
        this.reasonForBlock = null;
    }

    public static SavingsAccountTransactionData withWithDrawalTransactionDetails(
            final SavingsAccountTransactionData savingsAccountTransactionData) {

        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                .transactionType(SavingsAccountTransactionType.WITHDRAWAL.getValue());

        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, currentDate, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.amount, savingsAccountTransactionData.outstandingChargeAmount,
                savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.reversed,
                savingsAccountTransactionData.transfer, savingsAccountTransactionData.paymentTypeOptions,
                savingsAccountTransactionData.interestedPostedAsOn, savingsAccountTransactionData.submittedByUsername,
                savingsAccountTransactionData.note, savingsAccountTransactionData.lienTransaction);
    }

    public void setId(final Long id) {
        this.id = id;
        this.modifiedId = id;
    }
}
