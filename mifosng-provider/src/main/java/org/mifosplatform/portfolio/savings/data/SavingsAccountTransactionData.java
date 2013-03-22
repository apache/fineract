/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

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
    private final BigDecimal amount;

    public static SavingsAccountTransactionData create(final Long id, final SavingsAccountTransactionEnumData transactionType,
            final Long savingsId, final String savingsAccountNo, final LocalDate date, final CurrencyData currency, final BigDecimal amount) {
        return new SavingsAccountTransactionData(id, transactionType, savingsId, savingsAccountNo, date, currency, amount);
    }

    public static SavingsAccountTransactionData template(final Long savingsId, final String savingsAccountNo,
            final LocalDate defaultLocalDate, final CurrencyData currency) {
        final Long id = null;
        final SavingsAccountTransactionEnumData transactionType = null;
        final BigDecimal amount = null;
        return new SavingsAccountTransactionData(id, transactionType, savingsId, savingsAccountNo, defaultLocalDate, currency, amount);
    }

    private SavingsAccountTransactionData(final Long id, final SavingsAccountTransactionEnumData transactionType, final Long savingsId,
            final String savingsAccountNo, final LocalDate date, final CurrencyData currency, final BigDecimal amount) {
        this.id = id;
        this.transactionType = transactionType;
        this.accountId = savingsId;
        this.accountNo = savingsAccountNo;
        this.date = date;
        this.currency = currency;
        this.amount = amount;
    }
}