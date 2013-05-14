/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;

public class LoanTransactionDTO {

    private final String transactionId;
    private final Date transactionDate;
    private final Long paymentTypeId;
    private final LoanTransactionEnumData transactionType;

    private final BigDecimal amount;

    /*** Breakup of amounts in case of repayments **/
    private final BigDecimal principal;
    private final BigDecimal interest;
    private final BigDecimal fees;
    private final BigDecimal penalties;

    /*** Boolean values determines if the transaction is reversed ***/
    private final boolean reversed;

    public LoanTransactionDTO(final Long paymentTypeId, final String transactionId, final Date transactionDate,
            final LoanTransactionEnumData transactionType, final BigDecimal amount, final BigDecimal principal, final BigDecimal interest,
            final BigDecimal fees, final BigDecimal penalties, final boolean reversed) {
        this.paymentTypeId = paymentTypeId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.principal = principal;
        this.interest = interest;
        this.fees = fees;
        this.penalties = penalties;
        this.reversed = reversed;
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public LoanTransactionEnumData getTransactionType() {
        return this.transactionType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public BigDecimal getFees() {
        return this.fees;
    }

    public BigDecimal getPenalties() {
        return this.penalties;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public Long getPaymentTypeId() {
        return this.paymentTypeId;
    }

}
