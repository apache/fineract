/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.floatingrates.data;

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
