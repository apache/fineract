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

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a savings account transaction.
 */
@SuppressWarnings("unused")
public class SavingsAccountTransactionData {

    private final Long id;
    private final SavingsAccountTransactionEnumData transactionType;
    private final Long accountId;
    private final String accountNo;
    private final LocalDate date;
    private final CurrencyData currency;
    private final PaymentDetailData paymentDetailData;
    private final BigDecimal amount;
    private final BigDecimal outstandingChargeAmount;
    private final BigDecimal runningBalance;
    private final boolean reversed;
    private final AccountTransferData transfer;
    private final LocalDate submittedOnDate;
    private final boolean interestedPostedAsOn;
    private final String submittedByUsername;
    private final String note ;
    
    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    //import fields
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

    public static SavingsAccountTransactionData importInstance(BigDecimal transactionAmount,LocalDate transactionDate,
            Long paymentTypeId,String accountNumber, String checkNumber, String routingCode,
            String receiptNumber, String bankNumber,Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex,String locale,String dateFormat){
        return new SavingsAccountTransactionData(transactionAmount, transactionDate, paymentTypeId, accountNumber,
                checkNumber, routingCode, receiptNumber, bankNumber, savingsAccountId, transactionType, rowIndex,locale,dateFormat);
    }

    private SavingsAccountTransactionData(BigDecimal transactionAmount,LocalDate transactionDate,
            Long paymentTypeId,String accountNumber, String checkNumber, String routingCode,
            String receiptNumber, String bankNumber,Long savingsAccountId,
            SavingsAccountTransactionEnumData transactionType, Integer rowIndex,String locale,String dateFormat){
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
        this.savingsAccountId=savingsAccountId;
        this.dateFormat= dateFormat;
        this.locale= locale;
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
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public SavingsAccountTransactionEnumData getTransactionType() {
        return transactionType;
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount,final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer, final boolean interestedPostedAsOn, final String submittedByUsername, final String note) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount,runningBalance, reversed, transfer, paymentTypeOptions, interestedPostedAsOn, submittedByUsername, note);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount,
            final BigDecimal runningBalance, final boolean reversed, final AccountTransferData transfer, final LocalDate submittedOnDate,
            final boolean interestedPostedAsOn, final String submittedByUsername, final String note) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, outstandingChargeAmount, runningBalance, reversed, transfer, paymentTypeOptions, submittedOnDate,
                interestedPostedAsOn, submittedByUsername, note);
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
        final String note = null ;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, defaultLocalDate,
                currency, amount, outstandingChargeAmount, runningBalance, reversed, null, null, interestedPostedAsOn, submittedByUsername, note);
    }

    public static SavingsAccountTransactionData templateOnTop(final SavingsAccountTransactionData savingsAccountTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, savingsAccountTransactionData.transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, savingsAccountTransactionData.date, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.amount,savingsAccountTransactionData.outstandingChargeAmount, savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.reversed,
                savingsAccountTransactionData.transfer, paymentTypeOptions, savingsAccountTransactionData.interestedPostedAsOn, 
                savingsAccountTransactionData.submittedByUsername, savingsAccountTransactionData.note);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal outstandingChargeAmount,
            final BigDecimal runningBalance, final boolean reversed, final AccountTransferData transfer,
            final Collection<PaymentTypeData> paymentTypeOptions, final boolean interestedPostedAsOn, final String submittedByUsername, final String note) {

        this(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency, amount, outstandingChargeAmount,
                runningBalance, reversed, transfer, paymentTypeOptions, null, interestedPostedAsOn, submittedByUsername, note);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount,final BigDecimal outstandingChargeAmount, final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions, final LocalDate submittedOnDate,
            final boolean interestedPostedAsOn, final String submittedByUsername, final String note) {
        this.id = id;
        this.transactionType = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.outstandingChargeAmount= outstandingChargeAmount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.transfer = transfer;
        this.paymentTypeOptions = paymentTypeOptions;
        this.submittedOnDate = submittedOnDate;
        this.interestedPostedAsOn = interestedPostedAsOn;
        this.submittedByUsername = submittedByUsername ;
        this.note = note ;
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
                savingsAccountTransactionData.interestedPostedAsOn,savingsAccountTransactionData.submittedByUsername, savingsAccountTransactionData.note);
    }
}