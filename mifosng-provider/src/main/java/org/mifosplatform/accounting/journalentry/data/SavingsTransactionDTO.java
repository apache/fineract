/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;

public class SavingsTransactionDTO {

    private final Long officeId;
    private final String transactionId;
    private final Date transactionDate;
    private final Long paymentTypeId;
    private final SavingsAccountTransactionEnumData transactionType;

    private final BigDecimal amount;

    /*** Boolean values determines if the transaction is reversed ***/
    private final boolean reversed;

    public SavingsTransactionDTO(final Long officeId, final Long paymentTypeId, final String transactionId, final Date transactionDate,
            final SavingsAccountTransactionEnumData transactionType, final BigDecimal amount, final boolean reversed) {
        this.paymentTypeId = paymentTypeId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.reversed = reversed;
        this.transactionType = transactionType;
        this.officeId = officeId;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public Long getPaymentTypeId() {
        return this.paymentTypeId;
    }

    public SavingsAccountTransactionEnumData getTransactionType() {
        return this.transactionType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public boolean isReversed() {
        return this.reversed;
    }

}
