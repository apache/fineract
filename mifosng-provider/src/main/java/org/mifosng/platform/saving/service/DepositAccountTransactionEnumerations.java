package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.saving.domain.DepositTransactionType;

public class DepositAccountTransactionEnumerations {
	
	public static EnumOptionData depositType(final Integer statusId) {
		return depositType(DepositTransactionType.fromInt(statusId));
	}

	private static EnumOptionData depositType(DepositTransactionType transactionType) {
		
		EnumOptionData optionData=null;
		switch (transactionType) {
		case DEPOSIT:
			optionData=new EnumOptionData(DepositTransactionType.DEPOSIT.getValue().longValue(), DepositTransactionType.DEPOSIT.getCode(), "Deposit");
			break;
			
		case WITHDRAW:
			optionData=new EnumOptionData(DepositTransactionType.WITHDRAW.getValue().longValue(), DepositTransactionType.WITHDRAW.getCode(), "Withdraw");
			break;
			
		case REVERSAL:
			optionData=new EnumOptionData(DepositTransactionType.REVERSAL.getValue().longValue(), DepositTransactionType.REVERSAL.getCode(), "Revarsal");	
			break;
		default:
			optionData=new EnumOptionData(DepositTransactionType.INVALID.getValue().longValue(), DepositTransactionType.INVALID.getCode(), "Invalid Transaction");
			break;
		}
		return optionData;
	}

}
