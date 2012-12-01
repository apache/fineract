package org.mifosplatform.portfolio.loanaccount.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanOfficerAssignmentException extends AbstractPlatformDomainRuleException {

    public LoanOfficerAssignmentException(final Long loanId, final Long fromLoanOfficerId) {
        super("error.msg.loan.not.assigned.to.loan.officer", "Loan with identifier " + loanId
                + " is not assigned to Loan Officer with identifier " + fromLoanOfficerId + ".", loanId);
    }

    public LoanOfficerAssignmentException(final Long loanId, final LocalDate date) {
        super("error.msg.loan.assignment.date.is.before.last.assignment.date", "Loan with identifier " + loanId
                + " was already assigned before date " + date.toString());
    }
}
