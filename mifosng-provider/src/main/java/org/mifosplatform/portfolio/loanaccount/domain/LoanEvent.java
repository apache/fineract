package org.mifosplatform.portfolio.loanaccount.domain;

/**
 *
 */
public enum LoanEvent {

	LOAN_CREATED, // 
	LOAN_REJECTED, // 
	LOAN_WITHDRAWN, // 
	LOAN_APPROVED, // 
	LOAN_APPROVAL_UNDO, // 
	LOAN_DISBURSED, // 
	LOAN_DISBURSAL_UNDO, // 
	LOAN_REPAYMENT_OR_WAIVER, //
	REPAID_IN_FULL, //
	WRITE_OFF_OUTSTANDING, // 
	LOAN_RESCHEDULE, //
	INTERST_REBATE_OWED, // 
	LOAN_OVERPAYMENT;
}
