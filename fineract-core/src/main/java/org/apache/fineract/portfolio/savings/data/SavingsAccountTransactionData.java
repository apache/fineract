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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.TransactionEntryType;
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
@Getter
public final class SavingsAccountTransactionData implements Serializable {

    private Long id;
    private final SavingsAccountTransactionEnumData transactionType;
    private final TransactionEntryType entryType;

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
    private final LocalDate submittedOnDate;
    private final boolean interestedPostedAsOn;
    private final String submittedByUsername;
    private final String note;
    private final boolean isManualTransaction;
    private final Boolean isReversal;
    private final Long originalTransactionId;
    private final Boolean lienTransaction;
    private final Long releaseTransactionId;
    private final String reasonForBlock;
    private final Set<SavingsAccountChargesPaidByData> chargesPaidByData = new HashSet<>();

    // templates
    private final Collection<PaymentTypeData> paymentTypeOptions;

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
    private final transient List<TaxDetailsData> taxDetails = new ArrayList<>();
    private Integer balanceNumberOfDays;
    private BigDecimal overdraftAmount;
    private transient Long modifiedId;
    private transient String refNo;

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate transactionDate,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final String submittedByUsername, final String note,
            final Boolean isReversal, final Long originalTransactionId, boolean isManualTransaction, final Boolean lienTransaction,
            final Long releaseTransactionId, final String reasonForBlock) {
        this.id = id;
        this.transactionType = transactionType;
        TransactionEntryType entryType = null;
        if (transactionType != null) {
            entryType = transactionType.getEntryType();
            entryType = entryType != null && Boolean.TRUE.equals(isReversal) ? entryType.getReversal() : entryType;
        }
        this.entryType = entryType;

        // duplicated fields
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = transactionDate;
        this.amount = amount;

        this.paymentDetailData = paymentDetailData;
        this.currency = currency;
        this.outstandingChargeAmount = outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.transfer = transfer;
        this.paymentTypeOptions = paymentTypeOptions;
        this.submittedOnDate = submittedOnDate;

        this.interestedPostedAsOn = interestedPostedAsOn;
        this.submittedByUsername = submittedByUsername;
        this.note = note;
        this.isManualTransaction = isManualTransaction;
        this.isReversal = isReversal;
        this.originalTransactionId = originalTransactionId;
        this.lienTransaction = lienTransaction;
        this.releaseTransactionId = releaseTransactionId;
        this.reasonForBlock = reasonForBlock;
    }

    private static SavingsAccountTransactionData createData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long accountId, final String accountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,
            final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final String submittedByUsername, final String note,
            final Boolean lienTransaction) {
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, accountId, accountNo, date, currency, amount,
                outstandingChargeAmount, runningBalance, reversed, transfer, paymentTypeOptions, submittedOnDate, interestedPostedAsOn,
                submittedByUsername, note, null, null, false, lienTransaction, null, null);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final LocalDate submittedOnDate, final boolean interestedPostedAsOn,
            final String submittedByUsername, final String note, final Boolean isReversal, final Long originalTransactionId,
            final Boolean lienTransaction, final Long releaseTransactionId, final String reasonForBlock) {
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, transfer, null, submittedOnDate, interestedPostedAsOn,
                submittedByUsername, note, isReversal, originalTransactionId, false, lienTransaction, releaseTransactionId, reasonForBlock);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final AccountTransferData transfer, final boolean interestedPostedAsOn,
            final String submittedByUsername, final String note, final LocalDate submittedOnDate) {
        return createData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency, amount,
                outstandingChargeAmount, runningBalance, reversed, transfer, null, submittedOnDate, interestedPostedAsOn,
                submittedByUsername, note, false);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance,
            final boolean reversed, final LocalDate submittedOnDate, final boolean interestedPostedAsOn, final BigDecimal cumulativeBalance,
            final LocalDate balanceEndDate) {
        SavingsAccountTransactionData data = createData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, null, null, submittedOnDate, interestedPostedAsOn, null, null,
                false);
        data.transactionDate = date;
        data.cumulativeBalance = cumulativeBalance;
        data.balanceEndDate = balanceEndDate;
        return data;
    }

    public static SavingsAccountTransactionData create(final Long id) {
        return createData(id, null, null, null, null, null, null, null, null, null, false, null, null, null, false, null, null, false);
    }

    public static SavingsAccountTransactionData withWithDrawalTransactionDetails(
            final SavingsAccountTransactionData savingsAccountTransactionData) {
        final LocalDate currentDate = DateUtils.getBusinessLocalDate();
        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                .transactionType(SavingsAccountTransactionType.WITHDRAWAL.getValue());
        return createData(savingsAccountTransactionData.getId(), transactionType, savingsAccountTransactionData.getPaymentDetailData(),
                savingsAccountTransactionData.getAccountId(), savingsAccountTransactionData.getAccountNo(), currentDate,
                savingsAccountTransactionData.getCurrency(), savingsAccountTransactionData.getAmount(),
                savingsAccountTransactionData.getOutstandingChargeAmount(), savingsAccountTransactionData.getRunningBalance(),
                savingsAccountTransactionData.isReversed(), savingsAccountTransactionData.getTransfer(),
                savingsAccountTransactionData.getPaymentTypeOptions(), savingsAccountTransactionData.getSubmittedOnDate(),
                savingsAccountTransactionData.isInterestedPostedAsOn(), savingsAccountTransactionData.getSubmittedByUsername(),
                savingsAccountTransactionData.getNote(), savingsAccountTransactionData.getLienTransaction());
    }

    public static SavingsAccountTransactionData template(final Long savingsId, final String savingsAccountNo,
            final LocalDate defaultLocalDate, final CurrencyData currency) {
        return createData(null, null, null, savingsId, savingsAccountNo, defaultLocalDate, currency, null, null, null, false, null, null,
                defaultLocalDate, false, null, null, false);
    }

    public static SavingsAccountTransactionData templateOnTop(final SavingsAccountTransactionData savingsAccountTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return createData(savingsAccountTransactionData.getId(), savingsAccountTransactionData.getTransactionType(),
                savingsAccountTransactionData.getPaymentDetailData(), savingsAccountTransactionData.getAccountId(),
                savingsAccountTransactionData.getAccountNo(), savingsAccountTransactionData.getDate(),
                savingsAccountTransactionData.getCurrency(), savingsAccountTransactionData.getAmount(),
                savingsAccountTransactionData.getOutstandingChargeAmount(), savingsAccountTransactionData.getRunningBalance(),
                savingsAccountTransactionData.isReversed(), savingsAccountTransactionData.getTransfer(), paymentTypeOptions,
                savingsAccountTransactionData.getSubmittedOnDate(), savingsAccountTransactionData.isInterestedPostedAsOn(),
                savingsAccountTransactionData.getSubmittedByUsername(), savingsAccountTransactionData.getNote(),
                savingsAccountTransactionData.getLienTransaction());
    }

    private static SavingsAccountTransactionData createImport(final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsAccountId, final String accountNumber,
            final LocalDate transactionDate, final BigDecimal transactionAmount, final boolean reversed, final LocalDate submittedOnDate,
            boolean isManualTransaction, final Boolean lienTransaction) {
        SavingsAccountTransactionData data = new SavingsAccountTransactionData(null, transactionType, paymentDetailData, savingsAccountId,
                accountNumber, transactionDate, null, transactionAmount, null, null, reversed, null, null, submittedOnDate, false, null,
                null, null, null, isManualTransaction, lienTransaction, null, null);
        // duplicated import fields
        data.savingsAccountId = savingsAccountId;
        data.accountNumber = accountNumber;
        data.transactionDate = transactionDate;
        data.transactionAmount = transactionAmount;
        return data;
    }

    public static SavingsAccountTransactionData copyTransaction(SavingsAccountTransactionData accountTransaction) {
        return createImport(accountTransaction.getTransactionType(), accountTransaction.getPaymentDetailData(),
                accountTransaction.getSavingsAccountId(), null, accountTransaction.getTransactionDate(), accountTransaction.getAmount(),
                accountTransaction.isReversed(), accountTransaction.getSubmittedOnDate(), accountTransaction.isManualTransaction(),
                accountTransaction.getLienTransaction());
    }

    public static SavingsAccountTransactionData importInstance(BigDecimal transactionAmount, LocalDate transactionDate, Long paymentTypeId,
            String accountNumber, String checkNumber, String routingCode, String receiptNumber, String bankNumber, Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex, String locale, String dateFormat) {
        SavingsAccountTransactionData data = createImport(transactionType, null, savingsAccountId, accountNumber, transactionDate,
                transactionAmount, false, transactionDate, false, false);
        data.rowIndex = rowIndex;
        data.paymentTypeId = paymentTypeId;
        data.checkNumber = checkNumber;
        data.routingCode = routingCode;
        data.receiptNumber = receiptNumber;
        data.bankNumber = bankNumber;
        data.locale = locale;
        data.dateFormat = dateFormat;
        return data;
    }

    private static SavingsAccountTransactionData createImport(SavingsAccountTransactionEnumData transactionType, Long savingsAccountId,
            LocalDate transactionDate, BigDecimal transactionAmount, final LocalDate submittedOnDate, boolean isManualTransaction) {
        // import transaction
        return createImport(transactionType, null, savingsAccountId, null, transactionDate, transactionAmount, false, submittedOnDate,
                isManualTransaction, false);
    }

    public static SavingsAccountTransactionData interestPosting(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.INTEREST_POSTING;
        SavingsAccountTransactionEnumData transactionType = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        return createImport(transactionType, savingsAccount.getId(), date, amount.getAmount(), submittedOnDate, isManualTransaction);
    }

    public static SavingsAccountTransactionData overdraftInterest(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final boolean isManualTransaction) {
        final LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
        final SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.OVERDRAFT_INTEREST;
        SavingsAccountTransactionEnumData transactionType = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        return createImport(transactionType, savingsAccount.getId(), date, amount.getAmount(), submittedOnDate, isManualTransaction);
    }

    public static SavingsAccountTransactionData withHoldTax(final SavingsAccountData savingsAccount, final LocalDate date,
            final Money amount, final Map<TaxComponentData, BigDecimal> taxDetails) {
        final LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
        SavingsAccountTransactionType savingsAccountTransactionType = SavingsAccountTransactionType.WITHHOLD_TAX;
        SavingsAccountTransactionEnumData transactionType = new SavingsAccountTransactionEnumData(
                savingsAccountTransactionType.getValue().longValue(), savingsAccountTransactionType.getCode(),
                savingsAccountTransactionType.getValue().toString());
        SavingsAccountTransactionData accountTransaction = createImport(transactionType, savingsAccount.getId(), date, amount.getAmount(),
                submittedOnDate, false);
        accountTransaction.addTaxDetails(taxDetails);
        return accountTransaction;
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

    public boolean isCredit() {
        return transactionType.isCredit() && isNotReversed() && !isReversalTransaction();
    }

    public boolean isDebit() {
        return transactionType.isDebit() && isNotReversed() && !isReversalTransaction();
    }

    public boolean isWithdrawalFeeAndNotReversed() {
        return transactionType.isFeeDeduction() && isNotReversed();
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
        return this.transactionType.isAmountHold();
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

    public Money getRunningBalance(final MonetaryCurrency currency) {
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

    public void setBalanceNumberOfDays(final Integer balanceNumberOfDays) {
        this.balanceNumberOfDays = balanceNumberOfDays;
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

        return EndOfDayBalance.from(getTransactionDate(), openingBalance, endOfDayBalance, this.balanceNumberOfDays);
    }

    public boolean isChargeTransactionAndNotReversed() {
        return this.transactionType.isChargeTransaction() && isNotReversed();
    }

    public boolean occursOn(final LocalDate occursOnDate) {
        return DateUtils.isEqual(occursOnDate, getTransactionDate());
    }

    public EndOfDayBalance toEndOfDayBalanceBoundedBy(final Money openingBalance, final LocalDateInterval boundedBy,
            final boolean isAllowOverdraft) {

        final MonetaryCurrency currency = openingBalance.getCurrency();
        Money endOfDayBalance = openingBalance.copy();

        int numberOfDaysOfBalance = this.balanceNumberOfDays;

        LocalDate balanceStartDate = getTransactionDate();
        LocalDate balanceEndDate = getEndOfBalanceLocalDate();

        if (DateUtils.isAfter(boundedBy.startDate(), balanceStartDate)) {
            balanceStartDate = boundedBy.startDate();
            final LocalDateInterval spanOfBalance = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            numberOfDaysOfBalance = spanOfBalance.daysInPeriodInclusiveOfEndDate();
        } else {
            if (isDeposit() || isDividendPayoutAndNotReversed()) {
                endOfDayBalance = endOfDayBalance.plus(getAmount());
            } else if (isWithdrawal() || isChargeTransactionAndNotReversed()) {
                if (endOfDayBalance.isGreaterThanZero() || isAllowOverdraft) {
                    endOfDayBalance = endOfDayBalance.minus(getAmount());
                } else {
                    endOfDayBalance = Money.of(currency, this.runningBalance);
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

    public void reverse() {
        this.reversed = true;
    }

    public boolean fallsWithin(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceLocalDate());
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

    public boolean isDeposit() {
        return this.transactionType.isDeposit();
    }

    public boolean isChargeTransaction() {
        return this.transactionType.isChargeTransaction();
    }

    public Set<SavingsAccountChargesPaidByData> getSavingsAccountChargesPaid() {
        return this.chargesPaidByData;
    }

    public void updateCumulativeBalanceAndDates(final MonetaryCurrency currency, final LocalDate endOfBalanceDate) {
        // balance end date should not be before transaction date
        if (endOfBalanceDate != null && DateUtils.isBefore(endOfBalanceDate, this.transactionDate)) {
            this.balanceEndDate = this.transactionDate;
        } else if (endOfBalanceDate != null) {
            this.balanceEndDate = endOfBalanceDate;
        } else {
            this.balanceEndDate = null;
        }
        this.balanceNumberOfDays = LocalDateInterval.create(getTransactionDate(), endOfBalanceDate).daysInPeriodInclusiveOfEndDate();
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
        return isPayCharge() && chargePaidBy != null && chargePaidBy.isFeeCharge();
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
        return isPayCharge() && chargePaidBy != null && chargePaidBy.isPenaltyCharge();
    }

    public boolean isWaiveFeeChargeAndNotReversed() {
        return isWaiveFeeCharge() && isNotReversed();
    }

    public boolean isWaiveFeeCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return isWaiveCharge() && chargePaidBy != null && chargePaidBy.isFeeCharge();
    }

    public boolean isWaiveCharge() {
        return SavingsAccountTransactionType.fromInt(this.transactionType.getId().intValue()).isWaiveCharge();
    }

    public boolean isWaivePenaltyChargeAndNotReversed() {
        return isWaivePenaltyCharge() && isNotReversed();
    }

    public boolean isWaivePenaltyCharge() {
        final SavingsAccountChargesPaidByData chargePaidBy = getSavingsAccountChargePaidBy();
        return isWaiveCharge() && chargePaidBy != null && chargePaidBy.isPenaltyCharge();
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

    public void addTaxDetails(final Map<TaxComponentData, BigDecimal> taxDetails) {
        if (taxDetails != null) {
            List<TaxDetailsData> thisTaxDetails = getTaxDetails();
            for (Map.Entry<TaxComponentData, BigDecimal> mapEntry : taxDetails.entrySet()) {
                thisTaxDetails.add(new TaxDetailsData(mapEntry.getKey(), mapEntry.getValue()));
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
        thisTransactionData.put("reversed", isReversed());
        thisTransactionData.put("date", getTransactionDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("overdraftAmount", this.overdraftAmount);

        if (this.paymentDetailData != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetailData.getPaymentType().getId());
        }

        // Sending data in a map, though in savings we currently expect a transaction to always repay a single charge
        // (or may repay a part of a single charge too)
        if (!this.chargesPaidByData.isEmpty()) {
            final List<Map<String, Object>> savingsChargesPaidData = new ArrayList<>();
            for (final SavingsAccountChargesPaidByData chargePaidBy : this.chargesPaidByData) {
                final Map<String, Object> savingChargePaidData = new LinkedHashMap<>();
                savingChargePaidData.put("chargeId", chargePaidBy.getSavingsAccountChargeData());
                savingChargePaidData.put("isPenalty", chargePaidBy.getSavingsAccountChargeData().isPenalty());
                savingChargePaidData.put("savingsChargeId", chargePaidBy.getSavingsAccountChargeData().getId());
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
                if (taxDetails.getTaxComponent().getCreditAccount() != null) {
                    taxDetailsData.put("creditAccountId", taxDetails.getTaxComponent().getCreditAccount().getId());
                }
                taxData.add(taxDetailsData);
            }
            thisTransactionData.put("taxDetails", taxData);
        }

        return thisTransactionData;
    }

    public boolean isWithdrawal() {
        return this.transactionType.isWithdrawal();
    }

    public boolean isInterestPosting() {
        return this.transactionType.isInterestPosting() || this.transactionType.isOverDraftInterestPosting();
    }

    public boolean isWithHoldTaxAndNotReversed() {
        return SavingsAccountTransactionType.fromInt(this.transactionType.getId().intValue()).isWithHoldTax() && isNotReversed();
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public boolean spansAnyPortionOf(final LocalDateInterval periodInterval) {
        final LocalDateInterval balanceInterval = LocalDateInterval.create(getTransactionDate(), getEndOfBalanceLocalDate());
        return balanceInterval.containsPortionOf(periodInterval);
    }

    public void setId(final Long id) {
        this.id = id;
        this.modifiedId = id;
    }

    public boolean isReversalTransaction() {
        return Boolean.TRUE.equals(this.isReversal);
    }

    public boolean isManualTransaction() {
        return isManualTransaction;
    }

    public boolean isIsManualTransaction() {
        return isManualTransaction;
    }

    public TransactionEntryType getEntryType() {
        return entryType;
    }
}
