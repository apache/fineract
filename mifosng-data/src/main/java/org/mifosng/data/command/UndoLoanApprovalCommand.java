package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UndoLoanApprovalCommand {

	private Long loanId;

	protected UndoLoanApprovalCommand() {
		//
	}

	public UndoLoanApprovalCommand(final Long loanId) {
		this.loanId = loanId;
	}

	public Long getLoanId() {
		return this.loanId;
	}

	public void setLoanId(final Long loanId) {
		this.loanId = loanId;
	}
}