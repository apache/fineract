package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when trying to delete a loan in an invalid state.
 */
public class LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted extends
		AbstractPlatformDomainRuleException {

	public LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted(final Long id) {
		super("error.msg.loan.cannot.delete.loan.in.its.present.state", "Loan with identifier " + id + " cannot be deleted in its current state.", id);
	}

}