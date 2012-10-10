package org.mifosng.platform.exceptions;

public class SavingAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

	public SavingAccountNotFoundException(final Long id) {
		super("error.msg.saving.account.id.invalid", "Saving account with identifier " + id + " does not exist", id);
	}

}
