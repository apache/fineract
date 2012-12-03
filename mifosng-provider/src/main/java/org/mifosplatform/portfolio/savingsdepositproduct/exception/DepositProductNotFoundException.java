package org.mifosplatform.portfolio.savingsdepositproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class DepositProductNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public DepositProductNotFoundException(Long id) {
		super("error.msg.depositproduct.id.invalid",
				"Deposit product with identifier " + id + " does not exist", id);
	}
}
