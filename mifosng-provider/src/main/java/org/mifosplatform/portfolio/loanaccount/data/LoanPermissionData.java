package org.mifosplatform.portfolio.loanaccount.data;

/**
 * Immutable data object containing information on permissions allowed on a given loan.
 */
@SuppressWarnings("unused")
public class LoanPermissionData {

	// permission for actions on loan
	private final boolean closeLoanAllowed;
	private final boolean closeLoanAsRescheduledAllowed;
	private final boolean addLoanChargeAllowed;
	private final boolean waiveAllowed;
	private final boolean makeRepaymentAllowed;
	private final boolean rejectAllowed;
	private final boolean withdrawnByApplicantAllowed;
	private final boolean undoApprovalAllowed;
	private final boolean disbursalAllowed;
	private final boolean undoDisbursalAllowed;
	private final boolean anyActionOnLoanAllowed;
	
	//permissions for setting/removing guarantors for Loans
	private final boolean setGuarantorAllowed;
	private final boolean editGuarantorAllowed;
	
	// status of loan
	private final boolean pendingApproval;
	private final boolean waitingForDisbursal;
	private final boolean closedObligationsMet;
	
	public LoanPermissionData(
			final boolean closeLoanAllowed,
			final boolean closeLoanAsRescheduledAllowed,
			final boolean addLoanChargeAllowed,
			final boolean waiveAllowed,
			final boolean makeRepaymentAllowed, 
			final boolean rejectAllowed,
			final boolean withdrawnByApplicantAllowed, 
			final boolean undoApprovalAllowed,
			final boolean undoDisbursalAllowed, 
			final boolean disbursalAllowed, 
			final boolean setGuarantorAllowed,
			final boolean editGuarantorAllowed,
			final boolean pendingApproval, 
			final boolean waitingForDisbursal,
			final boolean closedObligationsMet) {
		this.closeLoanAllowed = closeLoanAllowed;
		this.closeLoanAsRescheduledAllowed = closeLoanAsRescheduledAllowed;
		this.addLoanChargeAllowed = addLoanChargeAllowed;
		this.waiveAllowed = waiveAllowed;
		this.makeRepaymentAllowed = makeRepaymentAllowed;
		this.rejectAllowed = rejectAllowed;
		this.withdrawnByApplicantAllowed = withdrawnByApplicantAllowed;
		this.undoApprovalAllowed = undoApprovalAllowed;
		this.undoDisbursalAllowed = undoDisbursalAllowed;
		this.disbursalAllowed = disbursalAllowed;
		this.pendingApproval = pendingApproval;
		this.setGuarantorAllowed = setGuarantorAllowed;
		this.editGuarantorAllowed = editGuarantorAllowed;
		this.waitingForDisbursal = waitingForDisbursal;
		this.closedObligationsMet = closedObligationsMet;
		this.anyActionOnLoanAllowed = closeLoanAllowed || closeLoanAsRescheduledAllowed || rejectAllowed || withdrawnByApplicantAllowed || pendingApproval || undoDisbursalAllowed || makeRepaymentAllowed;
	}
}