package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoanRepaymentScheduleData {

	private List<LoanRepaymentPeriodData> periods = new ArrayList<LoanRepaymentPeriodData>();

	public LoanRepaymentScheduleData() {
		//
	}

	public LoanRepaymentScheduleData(
			final List<LoanRepaymentPeriodData> periods) {
        this.periods = periods;
    }

	public List<LoanRepaymentPeriodData> getPeriods() {
		return periods;
	}

	public void setPeriods(List<LoanRepaymentPeriodData> periods) {
		this.periods = periods;
	}
}