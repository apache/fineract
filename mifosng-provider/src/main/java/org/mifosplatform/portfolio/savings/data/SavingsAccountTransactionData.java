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
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.accounttransfers.data.AccountTransferData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;

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

    // templates
    final Collection<CodeValueData> paymentTypeOptions;

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer) {
        final Collection<CodeValueData> paymentTypeOptions = null;
        return new SavingsAccountTransactionData(id, transactionType, paymentDetailData, savingsId, savingsAccountNo, date, currency,
                amount, runningBalance, reversed, transfer, paymentTypeOptions);
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
            final Collection<CodeValueData> paymentTypeOptions) {
        return new SavingsAccountTransactionData(savingsAccountTransactionData.id, savingsAccountTransactionData.transactionType,
                savingsAccountTransactionData.paymentDetailData, savingsAccountTransactionData.accountId,
                savingsAccountTransactionData.accountNo, savingsAccountTransactionData.date, savingsAccountTransactionData.currency,
                savingsAccountTransactionData.amount, savingsAccountTransactionData.runningBalance, savingsAccountTransactionData.reversed,
                savingsAccountTransactionData.transfer, paymentTypeOptions);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final PaymentDetailData paymentDetailData, final Long savingsId, final String savingsAccountNo, final LocalDate date,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal runningBalance, final boolean reversed,
            final AccountTransferData transfer, final Collection<CodeValueData> paymentTypeOptions) {
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
    }
}