package org.mifosng.platform.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class DepositAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

	public DepositAccountNotFoundException(final Long id) {
		super("error.msg.deposit.account.id.invalid", "Deposit account with identifier " + id + " does not exist", id);
	}

}
