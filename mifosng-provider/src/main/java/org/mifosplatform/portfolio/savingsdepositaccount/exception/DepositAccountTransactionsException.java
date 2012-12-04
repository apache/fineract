package org.mifosplatform.portfolio.savingsdepositaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DepositAccountTransactionsException extends AbstractPlatformDomainRuleException {

    public DepositAccountTransactionsException(String errorcode, String remainInterestForWithdrawal) {
        super(errorcode, "You can Withdraw " + remainInterestForWithdrawal + " only", remainInterestForWithdrawal);
    }
}
