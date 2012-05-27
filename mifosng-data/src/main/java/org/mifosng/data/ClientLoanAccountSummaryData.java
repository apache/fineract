package org.mifosng.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "clientLoanAccountSummary")
public class ClientLoanAccountSummaryData implements Serializable {

	private Long id;
	private String externalId;
	private Long loanProductId;
	private String loanProductName;

	private Integer loanStatusId;
	private boolean pendingApproval;
	private boolean waitingForDisbursal;
	private boolean open;
	private boolean closed;

	private String lifeCycleStatusText;
	private LocalDate lifeCycleStatusDate;

	// private LocalDate submittedOnDate;
	// private LocalDate approvedOnDate;
	// private LocalDate expectedDisbursementDate;
	// private LocalDate actualDisbursementDate;
	// private LocalDate expectedFirstRepaymentOnDate;
	// private LocalDate interestCalculatedFromDate;
	// private LocalDate expectedMaturityDate;
	// private LocalDate closedOnDate;

	public ClientLoanAccountSummaryData() {
		//
	}

	public ClientLoanAccountSummaryData(Long id, String externalId,
			Long productId, String loanProductName, Integer loanStatusId,
			String lifeCycleStatusText, boolean pendingApproval,
			boolean waitingForDisbursal, boolean open, boolean closed) {
		this.id = id;
		this.externalId = externalId;
		this.loanProductId = productId;
		this.loanProductName = loanProductName;
		this.loanStatusId = loanStatusId;
		this.lifeCycleStatusText = lifeCycleStatusText;
		this.pendingApproval = pendingApproval;
		this.waitingForDisbursal = waitingForDisbursal;
		this.open = open;
		this.closed = closed;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getLoanProductName() {
		return loanProductName;
	}

	public void setLoanProductName(String loanProductName) {
		this.loanProductName = loanProductName;
	}

	public boolean isPendingApproval() {
		return pendingApproval;
	}

	public void setPendingApproval(boolean pendingApproval) {
		this.pendingApproval = pendingApproval;
	}

	public boolean isWaitingForDisbursal() {
		return waitingForDisbursal;
	}

	public void setWaitingForDisbursal(boolean waitingForDisbursal) {
		this.waitingForDisbursal = waitingForDisbursal;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public String getLifeCycleStatusText() {
		return lifeCycleStatusText;
	}

	public void setLifeCycleStatusText(String lifeCycleStatusText) {
		this.lifeCycleStatusText = lifeCycleStatusText;
	}

	public LocalDate getLifeCycleStatusDate() {
		return lifeCycleStatusDate;
	}

	public void setLifeCycleStatusDate(LocalDate lifeCycleStatusDate) {
		this.lifeCycleStatusDate = lifeCycleStatusDate;
	}

	public Long getLoanProductId() {
		return loanProductId;
	}

	public void setLoanProductId(Long loanProductId) {
		this.loanProductId = loanProductId;
	}

	public Integer getLoanStatusId() {
		return loanStatusId;
	}

	public void setLoanStatusId(Integer loanStatusId) {
		this.loanStatusId = loanStatusId;
	}
}