package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UndoLoanDisbursalCommand {

	private Long loanId;

	protected UndoLoanDisbursalCommand() {
		//
	}

	public UndoLoanDisbursalCommand(final Long loanId) {
		this.loanId = loanId;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}
}