package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when loan resources are not found.
 */
public class LoanTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

	public LoanTransactionNotFoundException(Long id) {
		super("error.msg.loan.id.invalid", "Transaction with identifier " + id + " does not exist", id);
	}

	public LoanTransactionNotFoundException(Long id, Long loanId) {
		super("error.msg.loan.id.invalid", "Transaction with identifier " + id + " does not exist for loan with identifier " + loanId + "." , id, loanId);
	}
}