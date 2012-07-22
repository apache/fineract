package org.mifosng.platform.api.data;

public class LoanPermissionData {

	private final boolean waiveAllowed;
	private final boolean makeRepaymentAllowed;
	private final boolean rejectAllowed;
	private final boolean withdrawnByApplicantAllowed;
	private final boolean undoApprovalAllowed;
	private final boolean disbursalAllowed;
	private final boolean undoDisbursalAllowed;
		
	private final boolean pendingApproval;
	private final boolean waitingForDisbursal;
	private final boolean anyActionOnLoanAllowed;
	
	public LoanPermissionData(final boolean waiveAllowed,
			final boolean makeRepaymentAllowed, final boolean rejectAllowed,
			final boolean withdrawnByApplicantAllowed, final boolean undoApprovalAllowed,
			final boolean undoDisbursalAllowed, final boolean disbursalAllowed, final boolean pendingApproval, final boolean waitingForDisbursal) {
		this.waiveAllowed = waiveAllowed;
		this.makeRepaymentAllowed = makeRepaymentAllowed;
		this.rejectAllowed = rejectAllowed;
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
		this.undoApprovalAllowed = undoApprovalAllowed;
		this.undoDisbursalAllowed = undoDisbursalAllowed;
		this.disbursalAllowed = disbursalAllowed;
		this.pendingApproval = pendingApproval;
		this.waitingForDisbursal = waitingForDisbursal;
		this.anyActionOnLoanAllowed = isRejectAllowed() || isWithdrawnByApplicantAllowed() || isPendingApproval() || isUndoDisbursalAllowed() || isMakeRepaymentAllowed();
	}

	public boolean isWaiveAllowed() {
		return waiveAllowed;
	}

	public boolean isMakeRepaymentAllowed() {
		return makeRepaymentAllowed;
	}

	public boolean isRejectAllowed() {
		return rejectAllowed;
	}

	public boolean isWithdrawnByApplicantAllowed() {
		return withdrawnByApplicantAllowed;
	}

	public boolean isUndoApprovalAllowed() {
		return undoApprovalAllowed;
	}

	public boolean isDisbursalAllowed() {
		return disbursalAllowed;
	}

	public boolean isUndoDisbursalAllowed() {
		return undoDisbursalAllowed;
	}

	public boolean isPendingApproval() {
		return pendingApproval;
	}

	public boolean isWaitingForDisbursal() {
		return waitingForDisbursal;
	}

	public boolean isAnyActionOnLoanAllowed() {
		return anyActionOnLoanAllowed;
	}
}