package org.mifosng.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DerivedLoanData {

	private LoanRepaymentScheduleData repaymentSchedule;
	private LoanAccountSummaryData summary;
	private List<LoanRepaymentData> loanRepayments;

	public DerivedLoanData() {
		//
	}

	public DerivedLoanData(LoanRepaymentScheduleData repaymentScheduleData, LoanAccountSummaryData summaryData, List<LoanRepaymentData> loanRepayments) {
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

	public List<LoanRepaymentData> getLoanRepayments() {
		return loanRepayments;
	}

	public void setLoanRepayments(List<LoanRepaymentData> loanRepayments) {
		this.loanRepayments = loanRepayments;
	}
}