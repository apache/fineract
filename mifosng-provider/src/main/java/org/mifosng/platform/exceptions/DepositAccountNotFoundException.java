package org.mifosng.platform.exceptions;

public class DepositAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

	public DepositAccountNotFoundException(final Long id) {
		super("error.msg.deposit.account.id.invalid", "Deposit account with identifier " + id + " does not exist", id);
	}

}
