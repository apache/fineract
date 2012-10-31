package org.mifosng.platform.exceptions;


public class LoanOfficerAssignmentException extends
        AbstractPlatformDomainRuleException {

    public LoanOfficerAssignmentException(final Long loanId, final Long loanOfficerId) {
        super("error.msg.loan.not.assigned.to.loan.officer", "Loan with identifier " + loanId +
              " is not assigned to Loan Officer with identifier " + loanOfficerId + ".", loanId);
    }
}
