package org.mifosng.data.command;


public class UndoStateTransitionCommand {

	private Long loanId;
	private String comment;

	protected UndoStateTransitionCommand() {
		//
	}

	public UndoStateTransitionCommand(final Long loanId, final String comment) {
		this.loanId = loanId;
		this.comment = comment;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}