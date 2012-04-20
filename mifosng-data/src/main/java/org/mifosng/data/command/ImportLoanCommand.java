package org.mifosng.data.command;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ImportLoanCommand {

	private List<SubmitApproveDisburseLoanCommand> loans = new ArrayList<SubmitApproveDisburseLoanCommand>();

	public ImportLoanCommand() {
		//
	}

	public List<SubmitApproveDisburseLoanCommand> getLoans() {
		return loans;
	}

	public void setLoans(List<SubmitApproveDisburseLoanCommand> loans) {
		this.loans = loans;
	}
}