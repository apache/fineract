package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanOfficerUnassignmentException extends AbstractPlatformDomainRuleException {

    public LoanOfficerUnassignmentException(final Long loanId) {
        super("error.msg.loan.not.assigned.to.loan.officer", "Loan with identifier " + loanId
                + " is not assigned to any loan officer.", loanId);
    }
}
