package org.mifosng.platform.api.data;

import java.util.List;

public class DerivedLoanData {

	private LoanRepaymentScheduleData repaymentSchedule;
	private LoanAccountSummaryData summary;
	private List<LoanTransactionData> loanRepayments;

	public DerivedLoanData() {
		//
	}

	public DerivedLoanData(LoanRepaymentScheduleData repaymentScheduleData, LoanAccountSummaryData summaryData, List<LoanTransactionData> loanRepayments) {
		this.repaymentSchedule = repaymentScheduleData;
		this.summary = summaryData;
		this.loanRepayments = loanRepayments;
    }

	public LoanRepaymentScheduleData getRepaymentSchedule() {
		return repaymentSchedule;
	}

	public void setRepaymentSchedule(LoanRepaymentScheduleData repaymentSchedule) {
		this.repaymentSchedule = repaymentSchedule;
	}

	public LoanAccountSummaryData getSummary() {
		return summary;
	}

	public void setSummary(LoanAccountSummaryData summary) {
		this.summary = summary;
	}

	public List<LoanTransactionData> getLoanRepayments() {
		return loanRepayments;
	}

	public void setLoanRepayments(List<LoanTransactionData> loanRepayments) {
		this.loanRepayments = loanRepayments;
	}
}