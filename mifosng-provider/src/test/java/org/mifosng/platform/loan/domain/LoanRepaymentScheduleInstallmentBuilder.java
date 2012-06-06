package org.mifosng.platform.loan.domain;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

public class LoanRepaymentScheduleInstallmentBuilder {

	private Loan loan = null;
	private Integer installmentNumber = Integer.valueOf(1);
//	private LocalDate from = LocalDate.now();
	private LocalDate dueDate = LocalDate.now();
	private MonetaryCurrency currencyDetail = new MonetaryCurrencyBuilder().build();
	private Money principal = new MoneyBuilder().build();
	private Money interest = new MoneyBuilder().build();
	
	public LoanRepaymentScheduleInstallmentBuilder(MonetaryCurrency currencyDetail) {
		this.currencyDetail = currencyDetail;
		this.principal = new MoneyBuilder().with(currencyDetail).build();
		this.interest = new MoneyBuilder().with(currencyDetail).build();
	}

	public LoanRepaymentScheduleInstallment build() {
		return new LoanRepaymentScheduleInstallment(loan, installmentNumber, dueDate, principal.getAmount(), interest.getAmount());
	}

	public LoanRepaymentScheduleInstallmentBuilder withPrincipal(String withPrincipal) {
		this.principal = new MoneyBuilder().with(currencyDetail).with(withPrincipal).build();
		return this;
	}
	
	public LoanRepaymentScheduleInstallmentBuilder withInterest(String withInterest) {
		this.interest = new MoneyBuilder().with(currencyDetail).with(withInterest).build();
		return this;
	}

	public LoanRepaymentScheduleInstallmentBuilder withDueDate(LocalDate withDueDate) {
		this.dueDate = withDueDate;
		return this;
	}
}
