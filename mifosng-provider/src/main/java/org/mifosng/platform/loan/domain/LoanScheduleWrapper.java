package org.mifosng.platform.loan.domain;

import java.util.List;

import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

/**
 * A wrapper around loan schedule related data exposing needed behaviour by loan.
 * 
 * This wrapper should be side-effect free. It should not attempt to change state of loan parts passed through to it.
 */
public class LoanScheduleWrapper {

	public Money calculateTotalOutstanding(
			final MonetaryCurrency currency, 
			final List<LoanRepaymentScheduleInstallment> installments) {

		Money cumulativeValue = Money.zero(currency);

		for (LoanRepaymentScheduleInstallment scheduledRepayment : installments) {
			cumulativeValue = cumulativeValue.plus(scheduledRepayment.getTotalOutstanding(currency));
		}

		return cumulativeValue;
	}
}