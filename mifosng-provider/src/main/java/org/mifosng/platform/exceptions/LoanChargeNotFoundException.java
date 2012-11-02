package org.mifosng.platform.exceptions;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when loan charge does not exist.
 */
public class LoanChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanChargeNotFoundException(final Long id) {
        super("error.msg.loanCharge.id.invalid", "Loan charge with identifier " + id + " does not exist", id);
    }

	public LoanChargeNotFoundException(final Long id, final Long loanId) {
		super("error.msg.loanCharge.id.invalid.for.given.loan", "Loan charge with identifier " + id + " does not exist for loan " + loanId, id, loanId);
	}
}
