/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.util.Collection;

/**
 * Immutable data object representing a summary of a clients various accounts.
 */
public class ClientAccountSummaryCollectionData {

    @SuppressWarnings("unused")
    private final Collection<ClientLoanAccountSummaryData> loanAccounts;
    @SuppressWarnings("unused")
    private final Collection<ClientSavingsAccountSummaryData> savingsAccounts;

    public ClientAccountSummaryCollectionData(final Collection<ClientLoanAccountSummaryData> loanAccounts,
            final Collection<ClientSavingsAccountSummaryData> savingsAccounts) {
        this.loanAccounts = defaultIfEmpty(loanAccounts);
        this.savingsAccounts = savingsAccounts;
    }

    private Collection<ClientLoanAccountSummaryData> defaultIfEmpty(final Collection<ClientLoanAccountSummaryData> collection) {
        Collection<ClientLoanAccountSummaryData> returnCollection = null;
        if (collection != null && !collection.isEmpty()) {
            returnCollection = collection;
        }
        return returnCollection;
    }
}