package org.mifosng.platform.api.commands;

public class UndoStateTransitionCommand {

	private Long loanId;
	private String note;

	protected UndoStateTransitionCommand() {
		//
	}

	public UndoStateTransitionCommand(final Long loanId, final String note) {
		this.loanId = loanId;
		this.note = note;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}