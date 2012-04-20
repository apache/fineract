package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClientDataWithAccountsData {

	private ClientData clientInfo;
	private List<LoanAccountData> pendingApprovalLoans = new ArrayList<LoanAccountData>();
	private List<LoanAccountData> awaitingDisbursalLoans = new ArrayList<LoanAccountData>();
	private List<LoanAccountData> openLoans = new ArrayList<LoanAccountData>();
	private List<LoanAccountData> closedLoans = new ArrayList<LoanAccountData>();

	public ClientDataWithAccountsData() {
		//
	}

	public ClientDataWithAccountsData(final ClientData clientInfo,
			final List<LoanAccountData> pendingApprovalLoans,
			final List<LoanAccountData> awaitingDisbursalLoans,
			final List<LoanAccountData> openLoans,
			final List<LoanAccountData> closedLoans) {
		this.clientInfo = clientInfo;
		this.pendingApprovalLoans = pendingApprovalLoans;
		this.awaitingDisbursalLoans = awaitingDisbursalLoans;
		this.openLoans = openLoans;
		this.closedLoans = closedLoans;
	}

	public Integer getAnyLoanCount() {
		return getPendingApprovalLoanCount() + getAwaitingDisbursalLoanCount()
				+ getActiveLoanCount() + getClosedLoanCount();
	}

	public Integer getPendingApprovalLoanCount() {
		return this.pendingApprovalLoans.size();
	}

	public Integer getAwaitingDisbursalLoanCount() {
		return this.awaitingDisbursalLoans.size();
	}

	public Integer getActiveLoanCount() {
		return this.openLoans.size();
	}

	public Integer getClosedLoanCount() {
		return this.closedLoans.size();
	}

	public ClientData getClientInfo() {
		return this.clientInfo;
	}

	public void setClientInfo(final ClientData clientInfo) {
		this.clientInfo = clientInfo;
	}

	public List<LoanAccountData> getPendingApprovalLoans() {
		return this.pendingApprovalLoans;
	}

	public void setPendingApprovalLoans(
			final List<LoanAccountData> pendingApprovalLoans) {
		this.pendingApprovalLoans = pendingApprovalLoans;
	}

	public List<LoanAccountData> getAwaitingDisbursalLoans() {
		return this.awaitingDisbursalLoans;
	}

	public void setAwaitingDisbursalLoans(
			final List<LoanAccountData> awaitingDisbursalLoans) {
		this.awaitingDisbursalLoans = awaitingDisbursalLoans;
	}

	public List<LoanAccountData> getOpenLoans() {
		return this.openLoans;
	}

	public void setOpenLoans(final List<LoanAccountData> openLoans) {
		this.openLoans = openLoans;
	}

	public List<LoanAccountData> getClosedLoans() {
		return this.closedLoans;
	}

	public void setClosedLoans(final List<LoanAccountData> closedLoans) {
		this.closedLoans = closedLoans;
	}

}