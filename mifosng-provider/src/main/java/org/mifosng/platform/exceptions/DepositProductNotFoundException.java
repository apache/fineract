package org.mifosng.platform.exceptions;

public class DepositProductNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public DepositProductNotFoundException(Long id) {
		super("error.msg.depositproduct.id.invalid",
				"Deposit product with identifier " + id + " does not exist", id);
	}
}
