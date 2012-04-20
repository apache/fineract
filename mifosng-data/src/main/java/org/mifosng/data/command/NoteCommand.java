package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Command used for add or edit notes.
 */
@XmlRootElement
public class NoteCommand {

	private Long id;
	private Long clientId;
	private Long loanId;
	private Long loanTransactionId;
	private String note;

	public NoteCommand() {
		//
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getLoanId() {
		return loanId;
	}

	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}

	public Long getLoanTransactionId() {
		return loanTransactionId;
	}

	public void setLoanTransactionId(Long loanTransactionId) {
		this.loanTransactionId = loanTransactionId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}