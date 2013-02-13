package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when trying to modify a
 * loan in an invalid state.
 */
public class LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified extends AbstractPlatformDomainRuleException {

    public LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(final Long id) {
        super("error.msg.loan.cannot.modify.loan.in.its.present.state", "Loan application with identifier " + id
                + " cannot be modified in its current state.", id);
    }

}