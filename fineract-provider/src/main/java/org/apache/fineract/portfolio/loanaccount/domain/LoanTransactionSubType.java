package org.apache.fineract.portfolio.loanaccount.domain;

public enum LoanTransactionSubType {

	INVALID(0, "loanTransactionType.invalid"),
	REALIZATION_SUBSIDY(1, "loanTransactionType.realizationSubsidy");
	
	private final Integer value;
    private final String code;

    private LoanTransactionSubType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static LoanTransactionSubType fromInt(final Integer transactionSubTypeValue) {

        if (transactionSubTypeValue == null) { return LoanTransactionSubType.INVALID; }

        LoanTransactionSubType transactionSubType = null;
        switch (transactionSubTypeValue) {
            case 1:
            	transactionSubType = LoanTransactionSubType.REALIZATION_SUBSIDY;
            break;
            default:
            	transactionSubType = LoanTransactionSubType.INVALID;
            break;
        }
        return transactionSubType;
    }
}
