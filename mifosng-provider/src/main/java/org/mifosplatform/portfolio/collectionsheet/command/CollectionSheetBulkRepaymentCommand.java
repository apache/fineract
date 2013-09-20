/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.command;

import org.joda.time.LocalDate;

/**
 * Immutable command for loan bulk repayment.
 */
public class CollectionSheetBulkRepaymentCommand {

    private final String note;
    private final LocalDate transactionDate;
    private final SingleRepaymentCommand[] repaymentTransactions;

    public CollectionSheetBulkRepaymentCommand(final String note, final LocalDate transactionDate,
            final SingleRepaymentCommand[] repaymentTransactions) {
        this.note = note;
        this.transactionDate = transactionDate;
        this.repaymentTransactions = repaymentTransactions;
    }

    public String getNote() {
        return this.note;
    }

    public SingleRepaymentCommand[] getLoanTransactions() {
        return this.repaymentTransactions;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

}