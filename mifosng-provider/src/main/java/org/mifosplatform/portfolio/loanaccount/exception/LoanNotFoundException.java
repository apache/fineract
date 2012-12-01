package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class LoanNotFoundException extends AbstractPlatformResourceNotFoundException {

	public LoanNotFoundException(Long id) {
		super("error.msg.loan.id.invalid", "Loan with identifier " + id + " does not exist", id);
	}
}