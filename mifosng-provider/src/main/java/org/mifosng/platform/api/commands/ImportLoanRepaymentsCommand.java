package org.mifosng.platform.api.commands;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ImportLoanRepaymentsCommand {

	private List<LoanTransactionCommand> repayments = new ArrayList<LoanTransactionCommand>();

	public ImportLoanRepaymentsCommand() {
		//
	}

	public List<LoanTransactionCommand> getRepayments() {
		return repayments;
	}

	public void setRepayments(List<LoanTransactionCommand> repayments) {
		this.repayments = repayments;
	}
}