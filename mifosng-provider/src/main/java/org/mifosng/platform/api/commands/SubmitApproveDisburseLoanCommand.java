package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class SubmitApproveDisburseLoanCommand {

	private SubmitLoanApplicationCommand submitLoanApplicationCommand;
	private LoanStateTransitionCommand approveLoanCommand;
	private LoanStateTransitionCommand disburseLoanCommand;

	public SubmitApproveDisburseLoanCommand() {
		//
	}

	public SubmitLoanApplicationCommand getSubmitLoanApplicationCommand() {
		return submitLoanApplicationCommand;
	}

	public void setSubmitLoanApplicationCommand(
			SubmitLoanApplicationCommand submitLoanApplicationCommand) {
		this.submitLoanApplicationCommand = submitLoanApplicationCommand;
	}

	public LoanStateTransitionCommand getApproveLoanCommand() {
		return approveLoanCommand;
	}

	public void setApproveLoanCommand(LoanStateTransitionCommand approveLoanCommand) {
		this.approveLoanCommand = approveLoanCommand;
	}

	public LoanStateTransitionCommand getDisburseLoanCommand() {
		return disburseLoanCommand;
	}

	public void setDisburseLoanCommand(LoanStateTransitionCommand disburseLoanCommand) {
		this.disburseLoanCommand = disburseLoanCommand;
	}
}