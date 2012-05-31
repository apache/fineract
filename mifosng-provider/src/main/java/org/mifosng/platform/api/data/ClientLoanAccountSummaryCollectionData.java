package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonFilter;

@XmlRootElement
@JsonFilter("myFilter")
public class ClientLoanAccountSummaryCollectionData {

	private List<ClientLoanAccountSummaryData> pendingApprovalLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> awaitingDisbursalLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> openLoans = new ArrayList<ClientLoanAccountSummaryData>();
	private List<ClientLoanAccountSummaryData> closedLoans = new ArrayList<ClientLoanAccountSummaryData>();

	public ClientLoanAccountSummaryCollectionData() {
		//
	}

	public ClientLoanAccountSummaryCollectionData(
			final List<ClientLoanAccountSummaryData> pendingApprovalLoans,
			final List<ClientLoanAccountSummaryData> awaitingDisbursalLoans,
			final List<ClientLoanAccountSummaryData> openLoans,
			final List<ClientLoanAccountSummaryData> closedLoans) {
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

	public List<ClientLoanAccountSummaryData> getPendingApprovalLoans() {
		return pendingApprovalLoans;
	}

	public void setPendingApprovalLoans(
			List<ClientLoanAccountSummaryData> pendingApprovalLoans) {
		this.pendingApprovalLoans = pendingApprovalLoans;
	}

	public List<ClientLoanAccountSummaryData> getAwaitingDisbursalLoans() {
		return awaitingDisbursalLoans;
	}

	public void setAwaitingDisbursalLoans(
			List<ClientLoanAccountSummaryData> awaitingDisbursalLoans) {
		this.awaitingDisbursalLoans = awaitingDisbursalLoans;
	}

	public List<ClientLoanAccountSummaryData> getOpenLoans() {
		return openLoans;
	}

	public void setOpenLoans(List<ClientLoanAccountSummaryData> openLoans) {
		this.openLoans = openLoans;
	}

	public List<ClientLoanAccountSummaryData> getClosedLoans() {
		return closedLoans;
	}

	public void setClosedLoans(List<ClientLoanAccountSummaryData> closedLoans) {
		this.closedLoans = closedLoans;
	}
}