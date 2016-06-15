package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;

public class CompoundInterestValues {

	private BigDecimal interestatoBeCompounded;
	private BigDecimal interestToBeUncompounded;

	public CompoundInterestValues(final BigDecimal interestatoBeCompounded, final BigDecimal interestToBeUncompounded) {
		this.interestatoBeCompounded = interestatoBeCompounded;
		this.interestToBeUncompounded = interestToBeUncompounded;
	}

	public BigDecimal getInterestatoBeCompounded() {
		return this.interestatoBeCompounded;
	}

	public BigDecimal getInterestToBeUncompounded() {
		return this.interestToBeUncompounded;
	}

	public void setInterestatoBeCompounded(BigDecimal interestatoBeCompounded) {
		this.interestatoBeCompounded = interestatoBeCompounded;
	}

	public void setInterestToBeUncompounded(BigDecimal interestToBeUncompounded) {
		this.interestToBeUncompounded = interestToBeUncompounded;
	}

	public void setZeroForInterestToBeUncompounded() {
		this.interestToBeUncompounded = BigDecimal.ZERO;
	}

}
