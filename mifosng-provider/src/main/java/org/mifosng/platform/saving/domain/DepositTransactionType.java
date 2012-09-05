package org.mifosng.platform.saving.domain;


public enum DepositTransactionType {
	
	INVALID(0, "depositTransactionType.invalid"), DEPOSIT(1, "depositTransactionType.deposit"), WITHDRAW(2, "depositTransactionType.withdraw"), REVERSAL(3, "depositTransactionType.reversal");

    private final Integer value;
    private final String code;
    
    private DepositTransactionType(final Integer value, final String code) {
		this.value=value;
		this.code=code;
	}

	public Integer getValue() {
		return value;
	}

	public String getCode() {
		return code;
	}
    
    public static DepositTransactionType fromInt(final Integer transactionType){
    	
    	if (transactionType == null) {
			return DepositTransactionType.INVALID;
		}
    	
    	DepositTransactionType depositTransactionType=null;
    	switch (transactionType) {
		case 1:
			depositTransactionType=DepositTransactionType.DEPOSIT;
			break;
			
		case 2:
			depositTransactionType=DepositTransactionType.WITHDRAW;
			break;
			
		case 3:
			depositTransactionType=DepositTransactionType.REVERSAL;
			break;
			
		default :
			depositTransactionType=DepositTransactionType.INVALID;
			break;

		}
    	
    	return depositTransactionType;
    }

}
