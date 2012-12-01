package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

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
	
	public BigDecimal deriveCumulativePrincipalWrittenOff() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.principalWrittenOff());
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
	
	public BigDecimal deriveCumulativeInterestWrittenOff() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.interestWrittenOff());
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

	public BigDecimal deriveCumulativeFeeChargesToDate() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.feeChargesDue());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeFeeChargesPaid() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.feeChargesPaid());
		}
		return cumulativeValue;
	}
	
	public BigDecimal deriveCumulativeFeeChargesWaived() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.feeChargesWaived());
		}
		return cumulativeValue;
	}
	
	public BigDecimal deriveCumulativeFeeChargesWrittenOff() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.feeChargesWrittenOff());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativeFeeChargesOutstanding() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.feeChargesOutstanding());
		}
		return cumulativeValue;
	}
	
	public BigDecimal deriveCumulativePenaltyChargesToDate() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.penaltyChargesDue());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativePenaltyChargesPaid() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.penaltyChargesPaid());
		}
		return cumulativeValue;
	}
	
	public BigDecimal deriveCumulativePenaltyChargesWaived() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.penaltyChargesWaived());
		}
		return cumulativeValue;
	}
	
	public BigDecimal deriveCumulativePenaltyChargesWrittenOff() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.penaltyChargesWrittenOff());
		}
		return cumulativeValue;
	}

	public BigDecimal deriveCumulativePenaltyChargesOutstanding() {
		BigDecimal cumulativeValue = BigDecimal.ZERO;
		for (LoanSchedulePeriodData period : this.periods) {
			cumulativeValue = cumulativeValue.add(period.penaltyChargesOutstanding());
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