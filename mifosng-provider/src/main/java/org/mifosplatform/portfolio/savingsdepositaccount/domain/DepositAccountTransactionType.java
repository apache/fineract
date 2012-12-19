package org.mifosplatform.portfolio.savingsdepositaccount.domain;

public enum DepositAccountTransactionType {

    INVALID(0, "depositTransactionType.invalid"), //
    DEPOSIT(1, "depositTransactionType.deposit"), //
    WITHDRAW(2, "depositTransactionType.withdraw"), //
    REVERSAL(3, "depositTransactionType.reversal"), INTEREST_POSTING(4, "depositTransactionType.interestPosting");


    private final Integer value;
    private final String code;

    private DepositAccountTransactionType(final Integer value, final String code) {

		this.value=value;
		this.code=code;
	}

	public Integer getValue() {
		return value;
	}

	public String getCode() {
		return code;
	}
    
    public static DepositAccountTransactionType fromInt(final Integer transactionType){
    	
    	if (transactionType == null) {
			return DepositAccountTransactionType.INVALID;
		}
    	
    	DepositAccountTransactionType depositTransactionType=null;
    	switch (transactionType) {
		case 1:
			depositTransactionType=DepositAccountTransactionType.DEPOSIT;
			break;
			
		case 2:
			depositTransactionType=DepositAccountTransactionType.WITHDRAW;
			break;
			
		case 3:
			depositTransactionType=DepositAccountTransactionType.REVERSAL;
			break;
			
		case 4:
			depositTransactionType=DepositAccountTransactionType.INTEREST_POSTING;
			break;	
			
		default :
			depositTransactionType=DepositAccountTransactionType.INVALID;
			break;

		}
    	
    	return depositTransactionType;
    }

	public boolean isDeposit() {
		return this.value.equals(DepositAccountTransactionType.DEPOSIT.value);
	}

	public boolean isWithdraw() {
		return this.value.equals(DepositAccountTransactionType.WITHDRAW.value);
	}

}
