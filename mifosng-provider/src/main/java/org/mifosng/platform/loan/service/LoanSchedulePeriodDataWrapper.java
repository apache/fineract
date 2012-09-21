package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosng.platform.api.data.LoanSchedulePeriodData;

public class LoanSchedulePeriodDataWrapper {

	private final Collection<LoanSchedulePeriodData> periods;

	public LoanSchedulePeriodDataWrapper(final Collection<LoanSchedulePeriodData> periods) {
		this.periods = periods;
	}
	
	public BigDecimal deriveCumulativePrincipalDisbursed() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.principalDisbursed());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativePrincipalDue() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.principalDue());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativePrincipalPaid() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.principalPaid());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativePrincipalOutstanding() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.principalOutstanding());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeInterestExpected() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.interestDue());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeInterestPaid() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.interestPaid());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeInterestWaived() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.interestWaived());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeInterestOutstanding() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.interestOutstanding());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeChargesToDate() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.chargesDue());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeChargesPaid() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.chargesPaid());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeChargesOutstanding() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.chargesOutstanding());
		}
		return cumulativeValue;
	}

	public Integer deriveCumulativeLoanTermInDays() {
		Integer cumulativeValue = Integer.valueOf(0);
		for (LoanSchedulePeriodData period : this.periods) {
			if (period.daysInPeriod() != null) {
				cumulativeValue += period.daysInPeriod();
			}
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeTotalOverdue() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.totalOverdue());
		}
		return cumulativeValue;
	}
}