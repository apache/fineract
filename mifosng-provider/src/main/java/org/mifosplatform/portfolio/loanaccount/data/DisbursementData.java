package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing disbursement information.
 */
public class DisbursementData {

	private final LocalDate expectedDisbursementDate;
	private final LocalDate actualDisbursementDate;
	private final BigDecimal principalDisbursed;

	public DisbursementData(final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate, final BigDecimal principalDisbursed) {
		this.expectedDisbursementDate = expectedDisbursementDate;
		this.actualDisbursementDate = actualDisbursementDate;
		this.principalDisbursed = principalDisbursed;
	}

	public LocalDate disbursementDate() {
		LocalDate disbursementDate = this.expectedDisbursementDate;
		if (actualDisbursementDate != null) {
			disbursementDate = actualDisbursementDate;
		}
		return disbursementDate;
	}

	public BigDecimal amount() {
		return this.principalDisbursed;
	}
	
	public boolean isDisbursed() {
		return this.actualDisbursementDate != null;
	}
}