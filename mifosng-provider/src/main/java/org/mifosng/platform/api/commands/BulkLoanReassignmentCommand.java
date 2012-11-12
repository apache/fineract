package org.mifosng.platform.api.commands;

import org.joda.time.LocalDate;

/**
 * Immutable data object for reassigning loan officers on loans in bulk.
 */
public class BulkLoanReassignmentCommand {

	private final Long fromLoanOfficerId;
	private final Long toLoanOfficerId;
    private final LocalDate assignmentDate;

	private final String[] loans;

	public BulkLoanReassignmentCommand(final Long fromLoanOfficerId,
			final Long toLoanOfficerId, final LocalDate assignmentDate,
			final String[] loans) {
		this.fromLoanOfficerId = fromLoanOfficerId;
		this.toLoanOfficerId = toLoanOfficerId;
		this.assignmentDate = assignmentDate;
		this.loans = loans;
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