/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.data;

import java.math.BigDecimal;
import java.util.Date;

public class InterestRatePeriodData {

	private Date fromDate;
	private final BigDecimal interestRate;
	private final boolean isDifferentialToBLR;
	private final Date blrFromDate;
	private final BigDecimal blrInterestRate;
	private BigDecimal loanDifferentialInterestRate;
	private BigDecimal loanProductDifferentialInterestRate;
	private BigDecimal effectiveInterestRate;

	public InterestRatePeriodData(Date fromDate, BigDecimal interestRate,
			boolean isDifferentialToBLR, Date blrFromDate,
			BigDecimal blrInterestRate) {
		this.fromDate = fromDate;
		this.interestRate = interestRate;
		this.isDifferentialToBLR = isDifferentialToBLR;
		this.blrFromDate = blrFromDate;
		this.blrInterestRate = blrInterestRate;
	}

	public Date getFromDate() {
		return this.fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public BigDecimal getInterestRate() {
		return this.interestRate;
	}

	public boolean isDifferentialToBLR() {
		return this.isDifferentialToBLR;
	}

	public Date getBlrFromDate() {
		return this.blrFromDate;
	}

	public BigDecimal getBlrInterestRate() {
		return this.blrInterestRate;
	}

	public BigDecimal getLoanDifferentialInterestRate() {
		return this.loanDifferentialInterestRate;
	}

	public void setLoanDifferentialInterestRate(
			BigDecimal loanDifferentialInterestRate) {
		this.loanDifferentialInterestRate = loanDifferentialInterestRate;
	}

	public BigDecimal getLoanProductDifferentialInterestRate() {
		return this.loanProductDifferentialInterestRate;
	}

	public void setLoanProductDifferentialInterestRate(
			BigDecimal loanProductDifferentialInterestRate) {
		this.loanProductDifferentialInterestRate = loanProductDifferentialInterestRate;
	}

	public BigDecimal getEffectiveInterestRate() {
		return this.effectiveInterestRate;
	}

	public void setEffectiveInterestRate(BigDecimal effectiveInterestRate) {
		this.effectiveInterestRate = effectiveInterestRate;
	}
}
