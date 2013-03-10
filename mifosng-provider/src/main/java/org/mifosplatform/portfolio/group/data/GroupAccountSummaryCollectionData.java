/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

import java.util.List;

public class GroupAccountSummaryCollectionData {

    private final int anyLoanCount;
    private final int pendingApprovalLoanCount;
    private final List<GroupAccountSummaryData> pendingApprovalLoans;
    private final int awaitingDisbursalLoanCount;
    private final List<GroupAccountSummaryData> awaitingDisbursalLoans;
    private final int activeLoanCount;
    private final List<GroupAccountSummaryData> openLoans;
    private final int closedLoanCount;
    private final List<GroupAccountSummaryData> closedLoans;

    @SuppressWarnings("unused")
    private final int anyIndividualLoanCount;
    private final int pendingApprovalIndividualLoanCount;
    @SuppressWarnings("unused")
    private final List<GroupAccountSummaryData> pendingApprovalIndividualLoans;
    private final int awaitingDisbursalIndividualLoanCount;
    @SuppressWarnings("unused")
    private final List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans;
    private final int activeIndividualLoanCount;
    @SuppressWarnings("unused")
    private final List<GroupAccountSummaryData> openIndividualLoans;
    private final int closedIndividualLoanCount;
    @SuppressWarnings("unused")
    private final List<GroupAccountSummaryData> closedIndividualLoans;

    public GroupAccountSummaryCollectionData(final List<GroupAccountSummaryData> pendingApprovalLoans,
            final List<GroupAccountSummaryData> awaitingDisbursalLoans, final List<GroupAccountSummaryData> openLoans,
            final List<GroupAccountSummaryData> closedLoans, final List<GroupAccountSummaryData> pendingApprovalIndividualLoans,
            final List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans, final List<GroupAccountSummaryData> openIndividualLoans,
            final List<GroupAccountSummaryData> closedIndividualLoans) {
        this.pendingApprovalLoans = pendingApprovalLoans;
        this.pendingApprovalLoanCount = pendingApprovalLoans.size();
        this.awaitingDisbursalLoans = awaitingDisbursalLoans;
        this.awaitingDisbursalLoanCount = awaitingDisbursalLoans.size();
        this.openLoans = openLoans;
        this.activeLoanCount = openLoans.size();
        this.closedLoans = closedLoans;
        this.closedLoanCount = closedLoans.size();

        this.anyLoanCount = this.pendingApprovalLoanCount + this.awaitingDisbursalLoanCount + this.activeLoanCount + this.closedLoanCount;

        this.pendingApprovalIndividualLoans = pendingApprovalIndividualLoans;
        this.pendingApprovalIndividualLoanCount = pendingApprovalIndividualLoans.size();
        this.awaitingDisbursalIndividualLoans = awaitingDisbursalIndividualLoans;
        this.awaitingDisbursalIndividualLoanCount = awaitingDisbursalIndividualLoans.size();
        this.openIndividualLoans = openIndividualLoans;
        this.activeIndividualLoanCount = openIndividualLoans.size();
        this.closedIndividualLoans = closedIndividualLoans;
        this.closedIndividualLoanCount = closedIndividualLoans.size();

        this.anyIndividualLoanCount = this.pendingApprovalIndividualLoanCount + this.awaitingDisbursalIndividualLoanCount
                + this.activeIndividualLoanCount + this.closedIndividualLoanCount;
    }

    public int getAnyLoanCount() {
        return this.anyLoanCount;
    }

    public int getPendingApprovalLoanCount() {
        return this.pendingApprovalLoanCount;
    }

    public List<GroupAccountSummaryData> getPendingApprovalLoans() {
        return this.pendingApprovalLoans;
    }

    public int getAwaitingDisbursalLoanCount() {
        return this.awaitingDisbursalLoanCount;
    }

    public List<GroupAccountSummaryData> getAwaitingDisbursalLoans() {
        return this.awaitingDisbursalLoans;
    }

    public int getActiveLoanCount() {
        return this.activeLoanCount;
    }

    public List<GroupAccountSummaryData> getOpenLoans() {
        return this.openLoans;
    }

    public int getClosedLoanCount() {
        return this.closedLoanCount;
    }

    public List<GroupAccountSummaryData> getClosedLoans() {
        return this.closedLoans;
    }
}
