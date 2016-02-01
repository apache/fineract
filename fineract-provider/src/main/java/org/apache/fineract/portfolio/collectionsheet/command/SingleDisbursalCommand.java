/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable command for Single loan repayment.
 */
public class SingleDisbursalCommand {

    private final Long loanId;
    private final BigDecimal transactionAmount;
    private final LocalDate transactionDate;

    public SingleDisbursalCommand(final Long loanId, final BigDecimal transactionAmount, final LocalDate transactionDate) {
        this.loanId = loanId;
        this.transactionAmount = transactionAmount;
        this.transactionDate = transactionDate;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

}