package org.mifosng.platform.api.data;

import java.util.List;

/**
 * Immutable data object representing a summary of a clients various accounts. 
 */
public class ClientAccountSummaryCollectionData {

	private final int anyLoanCount;
	private final int pendingApprovalLoanCount;
	private final List<ClientAccountSummaryData> pendingApprovalLoans;
	private final int awaitingDisbursalLoanCount;
	private final List<ClientAccountSummaryData> awaitingDisbursalLoans;
	private final int activeLoanCount;
	private final List<ClientAccountSummaryData> openLoans;
	private final int closedLoanCount;
	private final List<ClientAccountSummaryData> closedLoans;
	private final int pendingApprovalDespositAccountsCount;
	private final List<ClientAccountSummaryData> pendingApprovalDepositAccounts;
	private final int approvedDespositAccountsCount;
	private final List<ClientAccountSummaryData> approvedDepositAccounts;
	
	public ClientAccountSummaryCollectionData(
			final List<ClientAccountSummaryData> pendingApprovalLoans,
			final List<ClientAccountSummaryData> awaitingDisbursalLoans,
			final List<ClientAccountSummaryData> openLoans,
			final List<ClientAccountSummaryData> closedLoans, 
			final List<ClientAccountSummaryData> pendingApprovalDepositAccounts, 
			final List<ClientAccountSummaryData> approvedDepositAccounts) {
		this.pendingApprovalLoans = pendingApprovalLoans;
		this.awaitingDisbursalLoans = awaitingDisbursalLoans;
		this.openLoans = openLoans;
		this.closedLoans = closedLoans;
		this.pendingApprovalDepositAccounts = pendingApprovalDepositAccounts;
		this.approvedDepositAccounts = approvedDepositAccounts;
		
		this.pendingApprovalLoanCount = this.pendingApprovalLoans.size();
		this.awaitingDisbursalLoanCount = this.awaitingDisbursalLoans.size();
		this.activeLoanCount = this.openLoans.size();
		this.closedLoanCount = this.closedLoans.size();
		this.anyLoanCount = this.pendingApprovalLoanCount + this.awaitingDisbursalLoanCount + this.activeLoanCount + this.closedLoanCount;
		
		this.pendingApprovalDespositAccountsCount = this.pendingApprovalDepositAccounts.size();
		this.approvedDespositAccountsCount = this.approvedDepositAccounts.size();
	}

	public int getAnyLoanCount() {
		return anyLoanCount;
	}

	public int getPendingApprovalLoanCount() {
		return pendingApprovalLoanCount;
	}

	public List<ClientAccountSummaryData> getPendingApprovalLoans() {
		return pendingApprovalLoans;
	}

	public int getAwaitingDisbursalLoanCount() {
		return awaitingDisbursalLoanCount;
	}

	public List<ClientAccountSummaryData> getAwaitingDisbursalLoans() {
		return awaitingDisbursalLoans;
	}

	public int getActiveLoanCount() {
		return activeLoanCount;
	}

	public List<ClientAccountSummaryData> getOpenLoans() {
		return openLoans;
	}

	public int getClosedLoanCount() {
		return closedLoanCount;
	}

	public List<ClientAccountSummaryData> getClosedLoans() {
		return closedLoans;
	}

	public int getPendingApprovalDespositAccountsCount() {
		return pendingApprovalDespositAccountsCount;
	}

	public List<ClientAccountSummaryData> getPendingApprovalDepositAccounts() {
		return pendingApprovalDepositAccounts;
	}

	public int getApprovedDespositAccountsCount() {
		return approvedDespositAccountsCount;
	}

	public List<ClientAccountSummaryData> getApprovedDepositAccounts() {
		return approvedDepositAccounts;
	}
}