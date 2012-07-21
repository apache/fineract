package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

public class ClientLoanAccountSummaryCollectionData {

	private List<ClientLoanAccountSummaryData> pendingApprovalLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> awaitingDisbursalLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> openLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> closedLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private int anyLoanCount;
	private int pendingApprovalLoanCount;
	private int awaitingDisbursalLoanCount;
	private int activeLoanCount;
	private int closedLoanCount;
	
	public ClientLoanAccountSummaryCollectionData(
			final List<ClientLoanAccountSummaryData> pendingApprovalLoans,
			final List<ClientLoanAccountSummaryData> awaitingDisbursalLoans,
			final List<ClientLoanAccountSummaryData> openLoans,
			final List<ClientLoanAccountSummaryData> closedLoans) {
		this.pendingApprovalLoans = pendingApprovalLoans;
		this.awaitingDisbursalLoans = awaitingDisbursalLoans;
		this.openLoans = openLoans;
		this.closedLoans = closedLoans;
		this.pendingApprovalLoanCount = this.pendingApprovalLoans.size();
		this.awaitingDisbursalLoanCount = this.awaitingDisbursalLoans.size();
		this.activeLoanCount = this.openLoans.size();
		this.closedLoanCount = this.closedLoans.size();
		this.anyLoanCount = this.pendingApprovalLoanCount + this.awaitingDisbursalLoanCount + this.activeLoanCount + this.closedLoanCount;
	}

	public List<ClientLoanAccountSummaryData> getPendingApprovalLoans() {
		return pendingApprovalLoans;
	}

	public List<ClientLoanAccountSummaryData> getAwaitingDisbursalLoans() {
		return awaitingDisbursalLoans;
	}

	public List<ClientLoanAccountSummaryData> getOpenLoans() {
		return openLoans;
	}

	public List<ClientLoanAccountSummaryData> getClosedLoans() {
		return closedLoans;
	}

	public int getAnyLoanCount() {
		return anyLoanCount;
	}

	public int getPendingApprovalLoanCount() {
		return pendingApprovalLoanCount;
	}

	public int getAwaitingDisbursalLoanCount() {
		return awaitingDisbursalLoanCount;
	}

	public int getActiveLoanCount() {
		return activeLoanCount;
	}

	public int getClosedLoanCount() {
		return closedLoanCount;
	}
}