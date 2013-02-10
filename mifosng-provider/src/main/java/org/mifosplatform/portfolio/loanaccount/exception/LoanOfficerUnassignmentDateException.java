package org.mifosplatform.portfolio.loanaccount.exception;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanOfficerUnassignmentDateException extends AbstractPlatformDomainRuleException {

    public LoanOfficerUnassignmentDateException(final Long loanId, final Long loanofficerId, final LocalDate assignedDate,
            final LocalDate unassignDate) {
        super("error.msg.loan.unassign.loanofficer.invalid.pastedate", "Loan officer Unassign date(" + unassignDate
                + ") can not be less than assign date(" + assignedDate + ") for loan(" + loanId + ") with Loan Officer " + loanofficerId
                + ").", loanId);
    }

    public LoanOfficerUnassignmentDateException(final Long loanId) {
        super("error.msg.loan.unassign.loanofficer.invalid.futuredate", "Unassign loan officer in future date is not supported.", loanId);
    }

}
