package org.mifosng.platform.api.commands;

import org.joda.time.LocalDate;

/**
 * Immutable data object for reassigning loan officers on loans in bulk.
 */
public class LoanReassignmentCommand {

    private final Long loanId;

	private final Long fromLoanOfficerId;
	private final Long toLoanOfficerId;
    private final LocalDate assignmentDate;

	private final String[] loans;

    public LoanReassignmentCommand(Long loanId, Long fromLoanOfficerId,
                                   Long toLoanOfficerId, LocalDate assignmentDate) {
        this.loanId = loanId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = null;
    }

    public LoanReassignmentCommand(Long fromLoanOfficerId, Long toLoanOfficerId,
                                   LocalDate assignmentDate, String[] loans) {
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.toLoanOfficerId = toLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.loans = loans;
        this.loanId = null;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getFromLoanOfficerId() {
		return fromLoanOfficerId;
	}

	public Long getToLoanOfficerId() {
		return toLoanOfficerId;
	}

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public String[] getLoans() {
		return loans;
	}
}