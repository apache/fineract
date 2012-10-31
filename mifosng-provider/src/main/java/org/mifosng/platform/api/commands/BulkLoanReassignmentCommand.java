package org.mifosng.platform.api.commands;

/**
 * Immutable data object for reassigning loan officers on loans in bulk.
 */
public class BulkLoanReassignmentCommand {

	private final Long fromLoanOfficerId;
	private final Long toLoanOfficerId;

	private final String[] loans;

	public BulkLoanReassignmentCommand(Long fromLoanOfficerId,
			Long toLoanOfficerId, String[] loans) {
		this.fromLoanOfficerId = fromLoanOfficerId;
		this.toLoanOfficerId = toLoanOfficerId;
		this.loans = loans;
	}

	public Long getFromLoanOfficerId() {
		return fromLoanOfficerId;
	}

	public Long getToLoanOfficerId() {
		return toLoanOfficerId;
	}

	public String[] getLoans() {
		return loans;
	}
}