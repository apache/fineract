package org.mifosng.platform.api.data;

public class LoanPermissionData {

	private boolean waiveAllowed;
	private boolean makeRepaymentAllowed;
	private boolean rejectAllowed;
	private boolean withdrawnByApplicantAllowed;
	private boolean undoApprovalAllowed;
	private boolean disbursalAllowed;
	private boolean undoDisbursalAllowed;
		
	private boolean pendingApproval;
	private boolean waitingForDisbursal;
	
	protected LoanPermissionData() {
		//
	}
	
	public LoanPermissionData(boolean waiveAllowed,
			boolean makeRepaymentAllowed, boolean rejectAllowed,
			boolean withdrawnByApplicantAllowed, boolean undoApprovalAllowed,
			boolean undoDisbursalAllowed, boolean disbursalAllowed, boolean pendingApproval, boolean waitingForDisbursal) {
		this.waiveAllowed = waiveAllowed;
		this.makeRepaymentAllowed = makeRepaymentAllowed;
		this.rejectAllowed = rejectAllowed;
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
		this.undoApprovalAllowed = undoApprovalAllowed;
		this.undoDisbursalAllowed = undoDisbursalAllowed;
		this.disbursalAllowed = disbursalAllowed;
		this.pendingApproval = pendingApproval;
		this.waitingForDisbursal = waitingForDisbursal;
	}

	public boolean isAnyActionOnLoanAllowed() {
		return isRejectAllowed() || isWithdrawnByApplicantAllowed() || isPendingApproval() || isUndoDisbursalAllowed() || isMakeRepaymentAllowed();
	}

	public boolean isWaiveAllowed() {
		return waiveAllowed;
	}

	public void setWaiveAllowed(boolean waiveAllowed) {
		this.waiveAllowed = waiveAllowed;
	}

	public boolean isMakeRepaymentAllowed() {
		return makeRepaymentAllowed;
	}

	public void setMakeRepaymentAllowed(boolean makeRepaymentAllowed) {
		this.makeRepaymentAllowed = makeRepaymentAllowed;
	}

	public boolean isRejectAllowed() {
		return rejectAllowed;
	}

	public void setRejectAllowed(boolean rejectAllowed) {
		this.rejectAllowed = rejectAllowed;
	}

	public boolean isWithdrawnByApplicantAllowed() {
		return withdrawnByApplicantAllowed;
	}

	public void setWithdrawnByApplicantAllowed(boolean withdrawnByApplicantAllowed) {
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
	}

	public boolean isUndoApprovalAllowed() {
		return undoApprovalAllowed;
	}

	public void setUndoApprovalAllowed(boolean undoApprovalAllowed) {
		this.undoApprovalAllowed = undoApprovalAllowed;
	}

	public boolean isDisbursalAllowed() {
		return disbursalAllowed;
	}

	public void setDisbursalAllowed(boolean disbursalAllowed) {
		this.disbursalAllowed = disbursalAllowed;
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

	public boolean isUndoDisbursalAllowed() {
		return undoDisbursalAllowed;
	}

	public void setUndoDisbursalAllowed(boolean undoDisbursalAllowed) {
		this.undoDisbursalAllowed = undoDisbursalAllowed;
	}
}