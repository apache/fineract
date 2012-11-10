package org.mifosplatform.portfolio.savingsdepositaccount.service;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountTransactionType;

public class DepositAccountTransactionEnumerations {

    public static EnumOptionData depositType(final Integer statusId) {
        return depositType(DepositAccountTransactionType.fromInt(statusId));
    }

    private static EnumOptionData depositType(DepositAccountTransactionType transactionType) {

        EnumOptionData optionData = null;
        switch (transactionType) {
            case DEPOSIT:
                optionData = new EnumOptionData(DepositAccountTransactionType.DEPOSIT.getValue().longValue(),
                        DepositAccountTransactionType.DEPOSIT.getCode(), "Deposit");
            break;

            case WITHDRAW:
                optionData = new EnumOptionData(DepositAccountTransactionType.WITHDRAW.getValue().longValue(),
                        DepositAccountTransactionType.WITHDRAW.getCode(), "Withdraw");
            break;

            case REVERSAL:
                optionData = new EnumOptionData(DepositAccountTransactionType.REVERSAL.getValue().longValue(),
                        DepositAccountTransactionType.REVERSAL.getCode(), "Revarsal");
            break;
            
            case INTEREST_POSTING:
    			optionData=new EnumOptionData(DepositAccountTransactionType.INTEREST_POSTING.getValue().longValue(), DepositAccountTransactionType.INTEREST_POSTING.getCode(), "Interest posting");
    			break;
    			
            default:
                optionData = new EnumOptionData(DepositAccountTransactionType.INVALID.getValue().longValue(),
                        DepositAccountTransactionType.INVALID.getCode(), "Invalid Transaction");
            break;
        }
        return optionData;
    }

}
