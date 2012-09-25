package org.mifosng.platform.loan.domain;

public enum LoanTransactionType {
	
	INVALID(0, "loanTransactionType.invalid"), //
	DISBURSEMENT(1, "loanTransactionType.disbursement"), //
	REPAYMENT(2, "loanTransactionType.repayment"), //
	REVERSAL(3, "loanTransactionType.reversal"), //
	WAIVED(4, "loanTransactionType.waiver"), //
	REPAYMENT_AT_DISBURSEMENT(5, "loanTransactionType.repaymentAtDisbursement");

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
			loanTransactionType = LoanTransactionType.REVERSAL;
			break;
		case 4:
			loanTransactionType = LoanTransactionType.WAIVED;
			break;
		case 5:
			loanTransactionType = LoanTransactionType.REPAYMENT_AT_DISBURSEMENT;
			break;
		default:
			loanTransactionType = LoanTransactionType.INVALID;
			break;
		}
		return loanTransactionType;
	}
}