package org.mifosng.platform.loan.domain;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.Money;

public class LoanTransactionBuilder {

	private Money amount = new MoneyBuilder().build();
	private LocalDate paymentDate = LocalDate.now();

	public LoanTransaction build() {
		return LoanTransaction.repayment(amount, paymentDate);
	}

	public LoanTransactionBuilder with(Money newAmount) {
		this.amount = newAmount;
		return this;
	}

	
}
