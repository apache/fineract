package org.mifosng.platform.api.data;

public class DepositPermissionData {
	
	private final boolean rejectAllowed;
	private final boolean withdrawnByApplicantAllowed;
	private final boolean undoApprovalAllowed;
	private final boolean pendingApproval;
	private final boolean isActive;
	
	private final boolean anyActionOnAccountAllowed;
	
	public DepositPermissionData(final boolean rejectAllowed,final boolean withdrawnByApplicantAllowed,final boolean undoApprovalAllowed,final boolean pendingApproval,final boolean isActive) {
		this.rejectAllowed = rejectAllowed;
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
		this.undoApprovalAllowed = undoApprovalAllowed;
		this.pendingApproval = pendingApproval;
		this.isActive=isActive;
		this.anyActionOnAccountAllowed = isRejectAllowed() || isPendingApproval() || isUndoApprovalAllowed() || isWithdrawnByApplicantAllowed();
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

	public boolean isPendingApproval() {
		return pendingApproval;
	}
	
	public boolean isAnyActionOnAccountAllowed() {
		return anyActionOnAccountAllowed;
	}

	public boolean isActive() {
		return isActive;
	}
}