/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.command;

import org.joda.time.LocalDate;

/**
 * Immutable command for loan bulk disburse.
 */
public class CollectionSheetBulkDisbursalCommand {

    private final String note;
    private final LocalDate transactionDate;
    private final SingleDisbursalCommand[] disburseTransactions;

    public CollectionSheetBulkDisbursalCommand(final String note, final LocalDate transactionDate,
            final SingleDisbursalCommand[] disburseTransactions) {
        this.note = note;
        this.transactionDate = transactionDate;
        this.disburseTransactions = disburseTransactions;
    }

    public String getNote() {
        return this.note;
    }

    public SingleDisbursalCommand[] getDisburseTransactions() {
        return this.disburseTransactions;
    }

    public LocalDate getTransactionDate() {
        return this.transactionDate;
    }

}