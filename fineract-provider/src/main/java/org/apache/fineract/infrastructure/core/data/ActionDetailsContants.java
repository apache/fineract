package org.apache.fineract.infrastructure.core.data;

public enum ActionDetailsContants {

	CreateAccountTransaction("Create Account Transaction"), RefundBytransfer("Refund By Transfer"), UpadteCalender("Update Calendar"),
	UpadteCollectionSheet("Update Collection Sheet"),SaveIndividualCollectioSheet("Save Individual Collection Sheet"),
	ApproveLoanApplication("Approve loan Application"), MakeRepayment("Make repayment"), MakeRefund("Make Refund"),
	UpdateFutureSchedule("Update Future Schedule"), GenrateLoanScheduleForVariableInsatllment("Genrate Loan Schedule For Variable Installment Request"),
	AddLoanShceduleVariations("Add Loan Schedule Variations"),  ApproveLoanReschedule("Approve Loan Reschedule"),
	SubmitLoanApplication("Submit Loan Application"), ModifyLoanApplication("Modify Loan Application"), RetrieveLoanPrePaymentTemplate("Retrieve Loan Pre Payment Template"),
	ApplyChargeToOverdueLoanInstallmentJob("Apply Charge To Overdue Loan Installment Job"), RECALCULATEINTERESTFORLOANJob("RECALCULATE INTEREST FOR LOAN Job"),
	DisburseLoan("Disburse Loan"), UndoDisbursal("Undo Disbursal"), UndoTransaction("Undo Transaction"), WaiveInterestOnLoan("Waive Interest On Loan"),
	LoanWriteOff("Loan Write Off"), CloseLoan("Close Loan"), AddLoanCharge("Add Loan Charge"),UpdateLoanCharge("Update Loan Charge"), WaiveLoanCharge("Waive Loan Charge"),
	DeleteLoanCharge("Delete Loan Charge"), PayLoanCharge("Pay Loan Charge"), TRANSFERFEECHARGEFORLOANSJob("TRANSFER_FEE_CHARGE_FOR LOANS Job"),
	ApplyOverdueChargesForLoan("Apply Overdue Charges For Loan"), UndoLoanWriteOff("Undo Loan Write Off"), 
	AddAndDeleteLoanDisburseDetails("Add And Delete Loan Disburse Details"), UpdateDisbursementDateAndAmountForTranche("Update Disbursement Date And Amount For Tranche"),
	MakeLoanRefund("Make Loan Refund"), UndoLastDisbursal("Undo Last Disbursal");
	
	
	 private final String value;
	 
	 private ActionDetailsContants(final String value){
		 this.value = value;
	 }

	public String getValue() {
		return this.value;
	}
	 
}
