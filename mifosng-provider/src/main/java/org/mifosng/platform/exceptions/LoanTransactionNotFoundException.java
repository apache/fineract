package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class LoanTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

	public LoanTransactionNotFoundException(Long id) {
		super("error.msg.loan.id.invalid", "Loan with identifier " + id + " does not exist", id);
	}
}