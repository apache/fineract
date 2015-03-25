/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.savings.SavingsAccountTransactionType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

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
    private final BigDecimal runningBalance;
    private final boolean reversed;
    private final AccountTransferData transfer;
    private final LocalDate submittedOnDate;

    // templates
    final Collection<PaymentTypeData> paymentTypeOptions;

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, runningBalance, reversed, transfer, paymentTypeOptions);
    }

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
                                                       final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
                                                       final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
                                                       final AccountTransferData transfer,final LocalDate submittedOnDate) {
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, runningBalance, reversed, transfer, paymentTypeOptions,submittedOnDate);
    }

    public static SavingsAccountTransactionData template(final Long savingsId, final String savingsAccountNo,
            final LocalDate defaultLocalDate, final CurrencyData currency) {
        final Long id = null;
        final SavingsAccountTransactionEnumData transactionType = null;
        final BigDecimal amount = null;
        final BigDecimal runningBalance = null;
        final boolean reversed = false;
        final PaymentDetailData paymentDetailData = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, defaultLocalDate,
                currency, amount, runningBalance, reversed, null, null);
    }

    public static SavingsAccountTransactionData templateOnTop(final SavingsAccountTransactionData savingsAccountTransactionData,
            final Collection<PaymentTypeData> paymentTypeOptions) {
        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, savingsAccountTransactionData.transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, savingsAccountTransactionData.date, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.amount, savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.reversed,
                savingsAccountTransactionData.transfer, paymentTypeOptions);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions) {

        this(id,transactionType,paymentDetailData,savingsId, savingsAccountNo,date,
        currency,amount,runningBalance, reversed,
        transfer, paymentTypeOptions,null);

    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
                                          final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
                                          final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
                                          final AccountTransferData transfer, final Collection<PaymentTypeData> paymentTypeOptions,final LocalDate submittedOnDate) {
        this.id = id;
        this.transactionType = transactionType;
        this.paymentDetailData = paymentDetailData;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.runningBalance = runningBalance;
        this.reversed = reversed;
        this.transfer = transfer;
        this.paymentTypeOptions = paymentTypeOptions;
        this.submittedOnDate = submittedOnDate;
    }

    public static SavingsAccountTransactionData withWithDrawalTransactionDetails(
            final SavingsAccountTransactionData savingsAccountTransactionData) {

        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                .transactionType(SavingsAccountTransactionType.WITHDRAWAL.getValue());

        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, currentDate, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.runningBalance,
                savingsAccountTransactionData.reversed, savingsAccountTransactionData.transfer,
                savingsAccountTransactionData.paymentTypeOptions);
    }
}