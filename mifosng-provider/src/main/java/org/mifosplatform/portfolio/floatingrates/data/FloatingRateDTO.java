/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.floatingrates.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;

public class FloatingRateDTO {

	private final boolean isFloatingInterestRate;
	private final LocalDate startDate;
	private BigDecimal interestRateDiff;
	private final Collection<FloatingRatePeriodData> baseLendingRatePeriods;

	public FloatingRateDTO(final boolean isFloatingInterestRate,
			final LocalDate startDate, final BigDecimal interestRateDiff,
			final Collection<FloatingRatePeriodData> baseLendingRatePeriods) {
		this.isFloatingInterestRate = isFloatingInterestRate;
		this.startDate = startDate;
		this.interestRateDiff = interestRateDiff;
		this.baseLendingRatePeriods = baseLendingRatePeriods;
	}

	public BigDecimal fetchBaseRate(LocalDate date) {
		BigDecimal rate = null;
		for (FloatingRatePeriodData periodData : this.baseLendingRatePeriods) {
			final LocalDate periodFromDate = new LocalDate(periodData.getFromDate());
			if (periodFromDate.isBefore(date)
					|| periodFromDate.isEqual(date)) {
				rate = periodData.getInterestRate();
				break;
			}
		}
		return rate;
	}

	public void addInterestRateDiff(final BigDecimal diff) {
		this.interestRateDiff = this.interestRateDiff.add(diff);
	}

	public boolean isFloatingInterestRate() {
		return this.isFloatingInterestRate;
	}

	public LocalDate getStartDate() {
		return this.startDate;
	}

	public BigDecimal getInterestRateDiff() {
		return this.interestRateDiff;
	}

	public Collection<FloatingRatePeriodData> getBaseLendingRatePeriods() {
		return this.baseLendingRatePeriods;
	}

}
