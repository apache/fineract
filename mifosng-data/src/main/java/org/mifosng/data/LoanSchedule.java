package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "loanSchedule")
public class LoanSchedule implements Serializable {

	private List<ScheduledLoanInstallment> scheduledLoanInstallments = new ArrayList<ScheduledLoanInstallment>();

	private static CurrencyData currencyData(MoneyData moneyData) {
		String code = moneyData.getCurrencyCode();
		String name = moneyData.getDefaultName();
		int decimalPlaces = moneyData.getDigitsAfterDecimal();
		String displaySymbol = moneyData.getDisplaySymbol();
		String nameCode = moneyData.getNameCode();
		return new CurrencyData(code, name, decimalPlaces, displaySymbol, nameCode);
	}
	
	public LoanSchedule() {
		//
	}

    public LoanSchedule(final List<ScheduledLoanInstallment> scheduledLoanInstallments) {
        this.scheduledLoanInstallments = scheduledLoanInstallments;
    }

	public MoneyData getCumulativePrincipal() {
		MoneyData cumulative = MoneyData.zero(currencyData(this.scheduledLoanInstallments.get(0).getPrincipalDue()));
        for (ScheduledLoanInstallment installment : this.scheduledLoanInstallments) {
            cumulative = cumulative.plus(installment.getPrincipalDue());
        }
        return cumulative;
    }

	public MoneyData getCumulativeInterest() {
		MoneyData cumulative = MoneyData.zero(currencyData(this.scheduledLoanInstallments.get(0).getPrincipalDue()));
        for (ScheduledLoanInstallment installment : this.scheduledLoanInstallments) {
            cumulative = cumulative.plus(installment.getInterestDue());
        }
        return cumulative;
    }

	public MoneyData getCumulativeTotal() {
		MoneyData cumulative = MoneyData.zero(currencyData(this.scheduledLoanInstallments.get(0).getPrincipalDue()));
        for (ScheduledLoanInstallment installment : this.scheduledLoanInstallments) {
            cumulative = cumulative.plus(installment.getTotalInstallmentDue());
        }
        return cumulative;
    }

	public List<ScheduledLoanInstallment> getScheduledLoanInstallments() {
		return this.scheduledLoanInstallments;
	}

	public void setScheduledLoanInstallments(
			final List<ScheduledLoanInstallment> scheduledLoanInstallments) {
		this.scheduledLoanInstallments = scheduledLoanInstallments;
	}
}