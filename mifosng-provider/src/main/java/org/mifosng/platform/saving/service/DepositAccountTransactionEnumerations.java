package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.saving.domain.DepositAccountTransactionType;

public class DepositAccountTransactionEnumerations {
	
	public static EnumOptionData depositType(final Integer statusId) {
		return depositType(DepositAccountTransactionType.fromInt(statusId));
	}

	private static EnumOptionData depositType(DepositAccountTransactionType transactionType) {
		
		EnumOptionData optionData=null;
		switch (transactionType) {
		case DEPOSIT:
			optionData=new EnumOptionData(DepositAccountTransactionType.DEPOSIT.getValue().longValue(), DepositAccountTransactionType.DEPOSIT.getCode(), "Deposit");
			break;
			
		case WITHDRAW:
			optionData=new EnumOptionData(DepositAccountTransactionType.WITHDRAW.getValue().longValue(), DepositAccountTransactionType.WITHDRAW.getCode(), "Withdraw");
			break;
			
		case REVERSAL:
			optionData=new EnumOptionData(DepositAccountTransactionType.REVERSAL.getValue().longValue(), DepositAccountTransactionType.REVERSAL.getCode(), "Revarsal");	
			break;
		default:
			optionData=new EnumOptionData(DepositAccountTransactionType.INVALID.getValue().longValue(), DepositAccountTransactionType.INVALID.getCode(), "Invalid Transaction");
			break;
		}
		return optionData;
	}

}
