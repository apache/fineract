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

    public GroupAccountSummaryCollectionData(List<GroupAccountSummaryData> pendingApprovalLoans,
            List<GroupAccountSummaryData> awaitingDisbursalLoans, List<GroupAccountSummaryData> openLoans,
            List<GroupAccountSummaryData> closedLoans, List<GroupAccountSummaryData> pendingApprovalIndividualLoans,
            List<GroupAccountSummaryData> awaitingDisbursalIndividualLoans, List<GroupAccountSummaryData> openIndividualLoans,
            List<GroupAccountSummaryData> closedIndividualLoans) {
        this.pendingApprovalLoans = pendingApprovalLoans;
        this.pendingApprovalLoanCount = pendingApprovalLoans.size();
        this.awaitingDisbursalLoans = awaitingDisbursalLoans;
        this.awaitingDisbursalLoanCount = awaitingDisbursalLoans.size();
        this.openLoans = openLoans;
        this.activeLoanCount = openLoans.size();
        this.closedLoans = closedLoans;
        this.closedLoanCount = closedLoans.size();

        this.anyLoanCount = pendingApprovalLoanCount + awaitingDisbursalLoanCount + activeLoanCount + closedLoanCount;

        this.pendingApprovalIndividualLoans = pendingApprovalIndividualLoans;
        this.pendingApprovalIndividualLoanCount = pendingApprovalIndividualLoans.size();
        this.awaitingDisbursalIndividualLoans = awaitingDisbursalIndividualLoans;
        this.awaitingDisbursalIndividualLoanCount = awaitingDisbursalIndividualLoans.size();
        this.openIndividualLoans = openIndividualLoans;
        this.activeIndividualLoanCount = openIndividualLoans.size();
        this.closedIndividualLoans = closedIndividualLoans;
        this.closedIndividualLoanCount = closedIndividualLoans.size();

        this.anyIndividualLoanCount = pendingApprovalIndividualLoanCount + awaitingDisbursalIndividualLoanCount + activeIndividualLoanCount
                + closedIndividualLoanCount;
    }

    public int getAnyLoanCount() {
        return anyLoanCount;
    }

    public int getPendingApprovalLoanCount() {
        return pendingApprovalLoanCount;
    }

    public List<GroupAccountSummaryData> getPendingApprovalLoans() {
        return pendingApprovalLoans;
    }

    public int getAwaitingDisbursalLoanCount() {
        return awaitingDisbursalLoanCount;
    }

    public List<GroupAccountSummaryData> getAwaitingDisbursalLoans() {
        return awaitingDisbursalLoans;
    }

    public int getActiveLoanCount() {
        return activeLoanCount;
    }

    public List<GroupAccountSummaryData> getOpenLoans() {
        return openLoans;
    }

    public int getClosedLoanCount() {
        return closedLoanCount;
    }

    public List<GroupAccountSummaryData> getClosedLoans() {
        return closedLoans;
    }
}
