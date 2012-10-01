package org.mifosng.platform.api.data;


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


    public GroupAccountSummaryCollectionData(List<GroupAccountSummaryData> pendingApprovalLoans,
                                             List<GroupAccountSummaryData> awaitingDisbursalLoans,
                                             List<GroupAccountSummaryData> openLoans,
                                             List<GroupAccountSummaryData> closedLoans) {
        this.pendingApprovalLoans = pendingApprovalLoans;
        this.pendingApprovalLoanCount = pendingApprovalLoans.size();

        this.awaitingDisbursalLoans = awaitingDisbursalLoans;
        this.awaitingDisbursalLoanCount = awaitingDisbursalLoans.size();

        this.openLoans = openLoans;
        this.activeLoanCount = openLoans.size();

        this.closedLoans = closedLoans;
        this.closedLoanCount = closedLoans.size();

        this.anyLoanCount = pendingApprovalLoanCount + awaitingDisbursalLoanCount + activeLoanCount + closedLoanCount;
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
