package org.mifosng.platform.saving.domain;

import java.math.BigDecimal;

import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class DepositApprovalData {

	private final Integer tenureInMonths;
	private final Money deposit;
	private final BigDecimal maturityInterestRate;
	private final Integer interestCompoundedEvery;
	private final PeriodFrequencyType interestCompoundedEveryPeriodType;
	private final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator;

	public DepositApprovalData(
			final Integer tenureInMonths,
			final Money deposit,
			final BigDecimal maturityInterestRate,
			final Integer interestCompoundedEvery,
			final PeriodFrequencyType interestCompoundedEveryPeriodType,
			final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator) {
		this.tenureInMonths = tenureInMonths;
		this.deposit = deposit;
		this.maturityInterestRate = maturityInterestRate;
		this.interestCompoundedEvery = interestCompoundedEvery;
		this.interestCompoundedEveryPeriodType = interestCompoundedEveryPeriodType;
		this.fixedTermDepositInterestCalculator = fixedTermDepositInterestCalculator;
	}

	public Integer getTenureInMonths() {
		return tenureInMonths;
	}

	public Money getDeposit() {
		return deposit;
	}

	public BigDecimal getMaturityInterestRate() {
		return maturityInterestRate;
	}

	public Integer getInterestCompoundedEvery() {
		return interestCompoundedEvery;
	}

	public PeriodFrequencyType getInterestCompoundedEveryPeriodType() {
		return interestCompoundedEveryPeriodType;
	}

	public FixedTermDepositInterestCalculator getFixedTermDepositInterestCalculator() {
		return fixedTermDepositInterestCalculator;
	}
}