package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;

public class CompoundInterestValues {

	private BigDecimal compoundedInterest;
	private BigDecimal uncompoundedInterest;

	public CompoundInterestValues(final BigDecimal compoundedInterest, final BigDecimal uncompoundedInterest) {
		this.compoundedInterest = compoundedInterest;
		this.uncompoundedInterest = uncompoundedInterest;
	}

	public BigDecimal getcompoundedInterest() {
		return this.compoundedInterest;
	}

	public BigDecimal getuncompoundedInterest() {
		return this.uncompoundedInterest;
	}

	public void setcompoundedInterest(BigDecimal interestToBeCompounded) {
		this.compoundedInterest = interestToBeCompounded;
	}

	public void setuncompoundedInterest(BigDecimal interestToBeUncompounded) {
		this.uncompoundedInterest = interestToBeUncompounded;
	}

	public void setZeroForInterestToBeUncompounded() {
		this.uncompoundedInterest = BigDecimal.ZERO;
	}

}
