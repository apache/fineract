package org.mifosplatform.portfolio.loanaccount.domain;

import org.mifosplatform.infrastructure.configuration.domain.Money;

public class TransactionProccessingResult {

	private final Money overPaymentAmount;
	private final boolean overPayment;

	public TransactionProccessingResult(final Money overPaymentAmount, final boolean overPayment) {
		this.overPaymentAmount = overPaymentAmount;
		this.overPayment = overPayment;
	}

	public Money getOverPaymentAmount() {
		return overPaymentAmount;
	}

	public boolean isOverPayment() {
		return overPayment;
	}
}
