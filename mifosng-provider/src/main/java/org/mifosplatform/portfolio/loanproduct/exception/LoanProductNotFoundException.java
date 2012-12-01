package org.mifosplatform.portfolio.loanproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan product resources are not found.
 */
public class LoanProductNotFoundException extends AbstractPlatformResourceNotFoundException {

	public LoanProductNotFoundException(Long id) {
		super("error.msg.loanproduct.id.invalid", "Loan product with identifier " + id + " does not exist", id);
	}
}