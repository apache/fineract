/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class DepositAccountOnHoldTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private EnumOptionData transactionType;
    @SuppressWarnings("unused")
    private final LocalDate transactionDate;
    @SuppressWarnings("unused")
    private final boolean reversed;
    @SuppressWarnings("unused")
    private final Long savingsId;
    @SuppressWarnings("unused")
    private final String savingsAccountNo;
    @SuppressWarnings("unused")
    private final String savingsClientName;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final String loanAccountNo;
    @SuppressWarnings("unused")
    private final String loanClientName;

    private DepositAccountOnHoldTransactionData(final Long id, final BigDecimal amount, final EnumOptionData transactionType,
            final LocalDate transactionDate, final boolean reversed, final Long savingsId, final String savingsAccNo,
            final String savingsClientName, final Long loanId, final String loanAccNo, final String loanClientName) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.reversed = reversed;
        this.savingsId = savingsId;
        this.savingsAccountNo = savingsAccNo;
        this.savingsClientName = savingsClientName;
        this.loanId = loanId;
        this.loanAccountNo = loanAccNo;
        this.loanClientName = loanClientName;
    }

    private DepositAccountOnHoldTransactionData(final Long id, final BigDecimal amount, final EnumOptionData transactionType,
            final LocalDate transactionDate, final boolean reversed) {
        this.id = id;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.reversed = reversed;
        this.savingsAccountNo = null;
        this.savingsId = 0L;
        this.savingsClientName = null;
        this.loanId = 0L;
        this.loanAccountNo = null;
        this.loanClientName = null;
    }

    public static DepositAccountOnHoldTransactionData instance(final Long id, final BigDecimal amount,
            final EnumOptionData transactionType, final LocalDate transactionDate, final boolean reversed, final Long savingsId,
            final String savingsAccountNo, final String savingsClientName, final Long loanId, final String loanAccountNo,
            final String loanClientName) {
        return new DepositAccountOnHoldTransactionData(id, amount, transactionType, transactionDate, reversed, savingsId, savingsAccountNo,
                savingsClientName, loanId, loanAccountNo, loanClientName);
    }

    public static DepositAccountOnHoldTransactionData instance(Long transactionId, BigDecimal amount, EnumOptionData transactionType,
            LocalDate date, boolean transactionReversed) {
        return new DepositAccountOnHoldTransactionData(transactionId, amount, transactionType, date, transactionReversed);
    }
}
