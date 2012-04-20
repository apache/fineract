package org.mifosng.platform.loan.domain;

public enum LoanTransactionType {
	UNKNOWN(0), DISBURSEMENT(1), REPAYMENT(2), REVERSAL(3), WAIVED(4);

    private final Integer value;

    private LoanTransactionType(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

	public static LoanTransactionType fromInt(final Integer transactionType) {

		if (transactionType == null) {
			return LoanTransactionType.UNKNOWN;
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
		default:
			loanTransactionType = LoanTransactionType.UNKNOWN;
			break;
		}
		return loanTransactionType;
	}
}