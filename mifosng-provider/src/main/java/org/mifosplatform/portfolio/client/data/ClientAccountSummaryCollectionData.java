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
    private final Integer anyLoanCount;
    private final Integer pendingApprovalLoanCount;
    private final Collection<ClientAccountSummaryData> pendingApprovalLoans;
    private final Integer awaitingDisbursalLoanCount;
    private final Collection<ClientAccountSummaryData> awaitingDisbursalLoans;
    private final Integer activeLoanCount;
    private final Collection<ClientAccountSummaryData> openLoans;
    private final Integer closedLoanCount;
    private final Collection<ClientAccountSummaryData> closedLoans;
    @SuppressWarnings("unused")
    private final Integer pendingApprovalDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> pendingApprovalDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer approvedDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> approvedDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer withdrawnByClientDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> withdrawnByClientDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer closedDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> closedDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer rejectedDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> rejectedDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer preclosedDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> preclosedDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer maturedDepositAccountsCount;
    private final Collection<ClientAccountSummaryData> maturedDepositAccounts;
    @SuppressWarnings("unused")
    private final Integer pendingApprovalSavingAccountsCount;
    private final Collection<ClientAccountSummaryData> pendingApprovalSavingAccounts;
    @SuppressWarnings("unused")
    private final Integer approvedSavingAccountsCount;
    private final Collection<ClientAccountSummaryData> approvedSavingAccounts;
    @SuppressWarnings("unused")
    private final Integer withdrawnByClientSavingAccountsCount;
    private final Collection<ClientAccountSummaryData> withdrawnByClientSavingAccounts;
    @SuppressWarnings("unused")
    private final Integer rejectedSavingAccountsCount;
    private final Collection<ClientAccountSummaryData> rejectedSavingAccounts;
    @SuppressWarnings("unused")
    private final Integer closedSavingAccountsCount;
    private final Collection<ClientAccountSummaryData> closedSavingAccounts;

    public ClientAccountSummaryCollectionData(final Collection<ClientAccountSummaryData> pendingApprovalLoans,
            final Collection<ClientAccountSummaryData> awaitingDisbursalLoans, final Collection<ClientAccountSummaryData> openLoans,
            final Collection<ClientAccountSummaryData> closedLoans,
            final Collection<ClientAccountSummaryData> pendingApprovalDepositAccounts,
            final Collection<ClientAccountSummaryData> approvedDepositAccounts,
            final Collection<ClientAccountSummaryData> withdrawnByClientDepositAccounts,
            final Collection<ClientAccountSummaryData> rejectedDepositAccounts,
            final Collection<ClientAccountSummaryData> closedDepositAccounts,
            final Collection<ClientAccountSummaryData> preclosedDepositAccounts,
            final Collection<ClientAccountSummaryData> maturedDepositAccounts,
            final Collection<ClientAccountSummaryData> pendingApprovalSavingAccounts,
            final Collection<ClientAccountSummaryData> approvedSavingAccounts,
            final Collection<ClientAccountSummaryData> withdrawnByClientSavingAccounts,
            final Collection<ClientAccountSummaryData> rejectedSavingAccounts,
            final Collection<ClientAccountSummaryData> closedSavingAccounts) {

        this.pendingApprovalLoans = defaultIfEmpty(pendingApprovalLoans);
        this.awaitingDisbursalLoans = defaultIfEmpty(awaitingDisbursalLoans);
        this.openLoans = defaultIfEmpty(openLoans);
        this.closedLoans = defaultIfEmpty(closedLoans);

        this.pendingApprovalDepositAccounts = defaultIfEmpty(pendingApprovalDepositAccounts);
        this.approvedDepositAccounts = defaultIfEmpty(approvedDepositAccounts);
        this.withdrawnByClientDepositAccounts = defaultIfEmpty(withdrawnByClientDepositAccounts);
        this.closedDepositAccounts = defaultIfEmpty(closedDepositAccounts);
        this.rejectedDepositAccounts = defaultIfEmpty(rejectedDepositAccounts);
        this.preclosedDepositAccounts = defaultIfEmpty(preclosedDepositAccounts);
        this.maturedDepositAccounts = defaultIfEmpty(maturedDepositAccounts);

        this.pendingApprovalSavingAccounts = defaultIfEmpty(pendingApprovalSavingAccounts);
        this.approvedSavingAccounts = defaultIfEmpty(approvedSavingAccounts);
        this.withdrawnByClientSavingAccounts = defaultIfEmpty(withdrawnByClientSavingAccounts);
        this.rejectedSavingAccounts = defaultIfEmpty(rejectedSavingAccounts);
        this.closedSavingAccounts = defaultIfEmpty(closedSavingAccounts);

        this.pendingApprovalLoanCount = defaultToNullIfEmpty(this.pendingApprovalLoans);
        this.awaitingDisbursalLoanCount = defaultToNullIfEmpty(this.awaitingDisbursalLoans);
        this.activeLoanCount = defaultToNullIfEmpty(this.openLoans);
        this.closedLoanCount = defaultToNullIfEmpty(this.closedLoans);

        this.pendingApprovalDepositAccountsCount = defaultToNullIfEmpty(this.pendingApprovalDepositAccounts);
        this.approvedDepositAccountsCount = defaultToNullIfEmpty(this.approvedDepositAccounts);
        this.withdrawnByClientDepositAccountsCount = defaultToNullIfEmpty(this.withdrawnByClientDepositAccounts);
        this.closedDepositAccountsCount = defaultToNullIfEmpty(this.closedDepositAccounts);
        this.rejectedDepositAccountsCount = defaultToNullIfEmpty(this.rejectedDepositAccounts);
        this.preclosedDepositAccountsCount = defaultToNullIfEmpty(this.preclosedDepositAccounts);
        this.maturedDepositAccountsCount = defaultToNullIfEmpty(this.maturedDepositAccounts);
        this.pendingApprovalSavingAccountsCount = defaultToNullIfEmpty(this.pendingApprovalSavingAccounts);
        this.approvedSavingAccountsCount = defaultToNullIfEmpty(this.approvedSavingAccounts);
        this.withdrawnByClientSavingAccountsCount = defaultToNullIfEmpty(this.withdrawnByClientSavingAccounts);
        this.rejectedSavingAccountsCount = defaultToNullIfEmpty(this.rejectedSavingAccounts);
        this.closedSavingAccountsCount = defaultToNullIfEmpty(this.closedSavingAccounts);

        this.anyLoanCount = countOf(this.pendingApprovalLoanCount, this.awaitingDisbursalLoanCount, this.activeLoanCount,
                this.closedLoanCount);
    }

    private Integer countOf(final Integer... objects) {
        Integer count = Integer.valueOf(0);
        for (Integer value : objects) {
            if (value != null) {
                count = count + value;
            }
        }
        return count;
    }

    private Integer defaultToNullIfEmpty(final Collection<ClientAccountSummaryData> collection) {
        Integer count = null;
        if (defaultIfEmpty(collection) != null) {
            count = collection.size();
        }
        return count;
    }

    private Collection<ClientAccountSummaryData> defaultIfEmpty(final Collection<ClientAccountSummaryData> collection) {
        Collection<ClientAccountSummaryData> returnCollection = null;
        if (collection != null && !collection.isEmpty()) {
            returnCollection = collection;
        }
        return returnCollection;
    }
}