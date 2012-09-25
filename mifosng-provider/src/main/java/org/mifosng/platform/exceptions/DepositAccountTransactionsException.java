package org.mifosng.platform.exceptions;


public class DepositAccountTransactionsException extends AbstractPlatformDomainRuleException {

	public DepositAccountTransactionsException(String remainInterestForWithdrawal) {
		super("deposit.transaction.interest.withdrawal.exception", "You can Withdraw "+remainInterestForWithdrawal+" only", remainInterestForWithdrawal);
	}

}
