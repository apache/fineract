package org.mifosplatform.portfolio.loanaccount.domain;

public enum LoanTransactionType {
	
	INVALID(0, "loanTransactionType.invalid"), //
	DISBURSEMENT(1, "loanTransactionType.disbursement"), //
	REPAYMENT(2, "loanTransactionType.repayment"), //
	CONTRA(3, "loanTransactionType.contra"), //
	WAIVE_INTEREST(4, "loanTransactionType.waiver"), //
	REPAYMENT_AT_DISBURSEMENT(5, "loanTransactionType.repaymentAtDisbursement"), //
	WRITEOFF(6, "loanTransactionType.writeOff"),
	MARKED_FOR_RESCHEDULING(7, "loanTransactionType.marked.for.rescheduling"), //
	/**
	 * This type of transactions is allowed on written-off loans where mfi still attempts to recover payments from applicant after writing-off.
	 */
	RECOVERY_REPAYMENT(8, "loanTransactionType.recoveryRepayment"),
	WAIVE_CHARGES(9, "loanTransactionType.waiveCharges");

    private final Integer value;
    private final String code;

    private LoanTransactionType(final Integer value, final String code) {
        this.value = value;
		this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }
    
	public String getCode() {
		return code;
	}

	public static LoanTransactionType fromInt(final Integer transactionType) {

		if (transactionType == null) {
			return LoanTransactionType.INVALID;
		}

		LoanTransactionType loanTransactionType = null;
		switch (transactionType) {
		case 1:
			loanTransactionType = LoanTransactionType.DISBURSEMENT;
			break;
		case 2:
			loanTransactionType = LoanTransactionType.REPAYMENT;
			break;
		case 3:
			loanTransactionType = LoanTransactionType.CONTRA;
			break;
		case 4:
			loanTransactionType = LoanTransactionType.WAIVE_INTEREST;
			break;
		case 5:
			loanTransactionType = LoanTransactionType.REPAYMENT_AT_DISBURSEMENT;
			break;
		case 6:
			loanTransactionType = LoanTransactionType.WRITEOFF;
			break;
		case 7:
			loanTransactionType = LoanTransactionType.MARKED_FOR_RESCHEDULING;
			break;
		case 8:
			loanTransactionType = LoanTransactionType.RECOVERY_REPAYMENT;
			break;
		case 9:
			loanTransactionType = LoanTransactionType.WAIVE_CHARGES;
			break;
		default:
			loanTransactionType = LoanTransactionType.INVALID;
			break;
		}
		return loanTransactionType;
	}
	
	public boolean isWriteOff() {
		return this.value.equals(LoanTransactionType.WRITEOFF.getValue());
	}
}