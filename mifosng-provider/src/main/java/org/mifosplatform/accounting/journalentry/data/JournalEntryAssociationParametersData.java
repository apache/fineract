/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

public class JournalEntryAssociationParametersData {

    private final boolean transactionDetailsRequired;
    private final boolean runningBalanceRequired;

    public JournalEntryAssociationParametersData() {
        this.transactionDetailsRequired = false;
        this.runningBalanceRequired = false;
    }

    public JournalEntryAssociationParametersData(final boolean transactionDetailsRequired, final boolean runningBalanceRequired) {
        this.transactionDetailsRequired = transactionDetailsRequired;
        this.runningBalanceRequired = runningBalanceRequired;
    }

    public boolean isTransactionDetailsRequired() {
        return this.transactionDetailsRequired;
    }

    public boolean isRunningBalanceRequired() {
        return this.runningBalanceRequired;
    }
}
