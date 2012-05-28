package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when loan product resources are not found.
 */
public class LoanProductNotFoundException extends PlatformResourceNotFoundException {

	public LoanProductNotFoundException(Long id) {
		super("error.msg.loanproduct.id.invalid", "Loan product with identifier " + id + " does not exist", id);
	}
}