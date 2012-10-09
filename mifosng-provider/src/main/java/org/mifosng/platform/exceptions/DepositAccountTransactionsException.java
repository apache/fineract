package org.mifosng.platform.exceptions;


public class DepositAccountTransactionsException extends AbstractPlatformDomainRuleException {

	public DepositAccountTransactionsException(String errorcode, String remainInterestForWithdrawal) {
		super(errorcode, "You can Withdraw "+remainInterestForWithdrawal+" only", remainInterestForWithdrawal);
	}

}
